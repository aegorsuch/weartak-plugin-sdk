package com.weartak.plugin.sdk.serverpackage

import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/**
 * Decoded, validated server package. Byte-array fields are owned by this object (not copied);
 * call [wipe] once the package reaches a terminal state. [toString] never reveals contents.
 */
class ServerPackage(
    val name: String,
    val host: String,
    val port: Int,
    val trustMode: Int,
    val identityMode: Int,
    val caCertificates: List<ByteArray> = emptyList(),
    val clientPkcs12: ByteArray? = null,
    val clientPkcs12Password: ByteArray? = null,
    val username: String? = null,
    val password: ByteArray? = null,
) {
    /** Derived from present sensitive fields; encoded as tag 7. */
    val sensitiveFlags: Int
        get() = (if (clientPkcs12 != null || clientPkcs12Password != null) ServerPackageSensitiveFlags.CLIENT_IDENTITY else 0) or
            (if (username != null || password != null) ServerPackageSensitiveFlags.SHARED_CREDENTIALS else 0)

    fun wipe() {
        caCertificates.forEach { it.fill(0) }
        clientPkcs12?.fill(0)
        clientPkcs12Password?.fill(0)
        password?.fill(0)
    }

    override fun toString(): String = "ServerPackage(redacted)"
}

/** Content-free rejection reasons; safe to log or display. */
enum class ServerPackageError {
    EMPTY,
    TOO_LARGE,
    BAD_MAGIC,
    UNSUPPORTED_VERSION,
    TRUNCATED,
    UNKNOWN_TAG,
    TAG_ORDER,
    MISSING_FIELD,
    BAD_LENGTH,
    INVALID_NAME,
    INVALID_HOST,
    INVALID_PORT,
    UNSUPPORTED_PROTOCOL,
    INVALID_TRUST_MODE,
    INVALID_IDENTITY_MODE,
    INVALID_FLAGS,
    FLAGS_MISMATCH,
    IDENTITY_MISMATCH,
    CA_NOT_ALLOWED,
    CA_REQUIRED,
    TOO_MANY_CA,
    INVALID_CA,
    DUPLICATE_CA,
    INVALID_CLIENT_PKCS12,
    INVALID_USERNAME,
    INVALID_SECRET,
    SETTINGS_TOO_LARGE,
}

sealed class ServerPackageDecodeResult {
    class Valid(val serverPackage: ServerPackage) : ServerPackageDecodeResult()
    data class Invalid(val error: ServerPackageError) : ServerPackageDecodeResult()
}

class ServerPackageException(val error: ServerPackageError) : IllegalArgumentException(error.name)

/**
 * Canonical ServerPackageV1 binary encoding shared by sender plugin, receiver plugin and host.
 *
 * `"WTSP"` magic, u8 version `1`, then TLV records `u8 tag | u16 big-endian length | value`:
 * required tags 1..7 exactly once in order, tag 8 (CA DER) 0..4 times, then optional sensitive
 * tags 9..12 at most once each, strictly ascending. Unknown tags, duplicates, trailing bytes,
 * flag/presence mismatches and non-canonical values are rejected.
 */
object ServerPackageCodec {
    const val VERSION: Int = 1
    val MAGIC: ByteArray = byteArrayOf(0x57, 0x54, 0x53, 0x50)

    const val TAG_NAME: Int = 1
    const val TAG_HOST: Int = 2
    const val TAG_PORT: Int = 3
    const val TAG_PROTOCOL: Int = 4
    const val TAG_TRUST_MODE: Int = 5
    const val TAG_IDENTITY_MODE: Int = 6
    const val TAG_SENSITIVE_FLAGS: Int = 7
    const val TAG_CA_CERTIFICATE: Int = 8
    const val TAG_CLIENT_PKCS12: Int = 9
    const val TAG_CLIENT_PKCS12_PASSWORD: Int = 10
    const val TAG_USERNAME: Int = 11
    const val TAG_PASSWORD: Int = 12

    const val PROTOCOL_TLS: Int = 1

    private const val HEADER_BYTES = 5
    private const val TLV_HEADER_BYTES = 3

    /** Encodes into one exact-size array so no intermediate buffers retain secrets. */
    fun encode(p: ServerPackage): ByteArray {
        validateFields(p)?.let { fail(it) }
        val nameBytes = p.name.toByteArray(StandardCharsets.UTF_8)
        val hostBytes = p.host.toByteArray(StandardCharsets.US_ASCII)
        val usernameBytes = p.username?.toByteArray(StandardCharsets.UTF_8)
        val settingsSize = HEADER_BYTES + TLV_HEADER_BYTES * 7 + nameBytes.size + hostBytes.size + 2 + 4
        if (settingsSize > ServerPackageContract.MAX_SETTINGS_BYTES) fail(ServerPackageError.SETTINGS_TOO_LARGE)
        val optional = listOfNotNull(p.clientPkcs12, p.clientPkcs12Password, usernameBytes, p.password)
        val total = settingsSize + (p.caCertificates + optional).sumOf { TLV_HEADER_BYTES + it.size }
        if (total > ServerPackageContract.MAX_PACKAGE_BYTES) fail(ServerPackageError.TOO_LARGE)

        val out = ByteBuffer.allocate(total)
        out.put(MAGIC).put(VERSION.toByte())
        out.tlv(TAG_NAME, nameBytes)
        out.tlv(TAG_HOST, hostBytes)
        out.tlv(TAG_PORT, byteArrayOf((p.port ushr 8).toByte(), p.port.toByte()))
        out.tlv(TAG_PROTOCOL, byteArrayOf(PROTOCOL_TLS.toByte()))
        out.tlv(TAG_TRUST_MODE, byteArrayOf(p.trustMode.toByte()))
        out.tlv(TAG_IDENTITY_MODE, byteArrayOf(p.identityMode.toByte()))
        out.tlv(TAG_SENSITIVE_FLAGS, byteArrayOf(p.sensitiveFlags.toByte()))
        p.caCertificates.forEach { out.tlv(TAG_CA_CERTIFICATE, it) }
        p.clientPkcs12?.let { out.tlv(TAG_CLIENT_PKCS12, it) }
        p.clientPkcs12Password?.let { out.tlv(TAG_CLIENT_PKCS12_PASSWORD, it) }
        usernameBytes?.let { out.tlv(TAG_USERNAME, it) }
        p.password?.let { out.tlv(TAG_PASSWORD, it) }
        return out.array()
    }

    fun decode(bytes: ByteArray?): ServerPackageDecodeResult = try {
        ServerPackageDecodeResult.Valid(decodeOrThrow(bytes))
    } catch (e: ServerPackageException) {
        ServerPackageDecodeResult.Invalid(e.error)
    }

    /** Validates canonical bytes without retaining decoded copies; null means valid. */
    fun validate(bytes: ByteArray?): ServerPackageError? = when (val r = decode(bytes)) {
        is ServerPackageDecodeResult.Valid -> {
            r.serverPackage.wipe()
            null
        }
        is ServerPackageDecodeResult.Invalid -> r.error
    }

    private fun decodeOrThrow(bytes: ByteArray?): ServerPackage {
        if (bytes == null || bytes.isEmpty()) fail(ServerPackageError.EMPTY)
        if (bytes.size > ServerPackageContract.MAX_PACKAGE_BYTES) fail(ServerPackageError.TOO_LARGE)
        if (bytes.size < HEADER_BYTES) fail(ServerPackageError.TRUNCATED)
        for (i in MAGIC.indices) if (bytes[i] != MAGIC[i]) fail(ServerPackageError.BAD_MAGIC)
        if (bytes[4].toInt() and 0xff != VERSION) fail(ServerPackageError.UNSUPPORTED_VERSION)

        val fields = arrayOfNulls<ByteArray>(TAG_PASSWORD + 1)
        val cas = mutableListOf<ByteArray>()
        try {
            var lastTag = 0
            var offset = HEADER_BYTES
            var settingsEnd = -1
            while (offset < bytes.size) {
                if (bytes.size - offset < TLV_HEADER_BYTES) fail(ServerPackageError.TRUNCATED)
                val tag = bytes[offset].toInt() and 0xff
                val length = ((bytes[offset + 1].toInt() and 0xff) shl 8) or (bytes[offset + 2].toInt() and 0xff)
                if (tag !in TAG_NAME..TAG_PASSWORD) fail(ServerPackageError.UNKNOWN_TAG)
                if (tag < lastTag || (tag == lastTag && tag != TAG_CA_CERTIFICATE)) fail(ServerPackageError.TAG_ORDER)
                if (tag <= TAG_SENSITIVE_FLAGS && tag != lastTag + 1) fail(ServerPackageError.MISSING_FIELD)
                if (tag > TAG_SENSITIVE_FLAGS && lastTag < TAG_SENSITIVE_FLAGS) fail(ServerPackageError.MISSING_FIELD)
                if (tag > TAG_SENSITIVE_FLAGS && settingsEnd < 0) settingsEnd = offset
                offset += TLV_HEADER_BYTES
                if (length > bytes.size - offset) fail(ServerPackageError.TRUNCATED)
                val value = bytes.copyOfRange(offset, offset + length)
                offset += length
                if (tag == TAG_CA_CERTIFICATE) {
                    if (cas.size == ServerPackageContract.MAX_CA_CERTIFICATES) {
                        value.fill(0)
                        fail(ServerPackageError.TOO_MANY_CA)
                    }
                    cas += value
                } else {
                    fields[tag] = value
                }
                lastTag = tag
            }
            if (lastTag < TAG_SENSITIVE_FLAGS) fail(ServerPackageError.MISSING_FIELD)
            if (settingsEnd < 0) settingsEnd = bytes.size
            if (settingsEnd > ServerPackageContract.MAX_SETTINGS_BYTES) fail(ServerPackageError.SETTINGS_TOO_LARGE)

            val name = decodeUtf8(fields[TAG_NAME]!!) ?: fail(ServerPackageError.INVALID_NAME)
            val hostBytes = fields[TAG_HOST]!!
            if (hostBytes.any { it < 0 }) fail(ServerPackageError.INVALID_HOST)
            val host = String(hostBytes, StandardCharsets.US_ASCII)
            val portBytes = fields[TAG_PORT]!!
            if (portBytes.size != 2) fail(ServerPackageError.BAD_LENGTH)
            val port = ((portBytes[0].toInt() and 0xff) shl 8) or (portBytes[1].toInt() and 0xff)
            if (singleByte(fields[TAG_PROTOCOL]!!) != PROTOCOL_TLS) fail(ServerPackageError.UNSUPPORTED_PROTOCOL)
            val trustMode = singleByte(fields[TAG_TRUST_MODE]!!)
            val identityMode = singleByte(fields[TAG_IDENTITY_MODE]!!)
            val flags = singleByte(fields[TAG_SENSITIVE_FLAGS]!!)
            if (flags and ServerPackageSensitiveFlags.ALL.inv() != 0) fail(ServerPackageError.INVALID_FLAGS)
            val usernameBytes = fields[TAG_USERNAME]
            val username = usernameBytes?.let { decodeUtf8(it) ?: fail(ServerPackageError.INVALID_USERNAME) }

            val p = ServerPackage(
                name, host, port, trustMode, identityMode, cas,
                clientPkcs12 = fields[TAG_CLIENT_PKCS12],
                clientPkcs12Password = fields[TAG_CLIENT_PKCS12_PASSWORD],
                username = username,
                password = fields[TAG_PASSWORD],
            )
            if (p.sensitiveFlags != flags) fail(ServerPackageError.FLAGS_MISMATCH)
            validateFields(p)?.let { fail(it) }
            fields.forEachIndexed { tag, v -> if (tag <= TAG_SENSITIVE_FLAGS || tag == TAG_USERNAME) v?.fill(0) }
            return p
        } catch (e: ServerPackageException) {
            fields.forEach { it?.fill(0) }
            cas.forEach { it.fill(0) }
            throw e
        }
    }

    /** Returns the first field-level violation, or null when the package is acceptable. */
    fun validateFields(p: ServerPackage): ServerPackageError? {
        if (!isValidLabel(p.name, ServerPackageContract.MAX_NAME_UTF8_BYTES)) return ServerPackageError.INVALID_NAME
        if (!ServerHostValidator.isValidHost(p.host)) return ServerPackageError.INVALID_HOST
        if (p.port !in 1..65_535) return ServerPackageError.INVALID_PORT

        val hasIdentity = p.clientPkcs12 != null || p.clientPkcs12Password != null
        val hasCredentials = p.username != null || p.password != null
        if (hasIdentity && (p.clientPkcs12 == null || p.clientPkcs12Password == null)) return ServerPackageError.FLAGS_MISMATCH
        if (hasCredentials && (p.username == null || p.password == null)) return ServerPackageError.FLAGS_MISMATCH
        when (p.identityMode) {
            ServerPackageIdentityMode.SHARED_IDENTITY -> if (!hasIdentity) return ServerPackageError.IDENTITY_MISMATCH
            ServerPackageIdentityMode.RECEIVER_ENROLLMENT, ServerPackageIdentityMode.ADMIN_PKCS12_SEPARATE ->
                if (hasIdentity) return ServerPackageError.IDENTITY_MISMATCH
            else -> return ServerPackageError.INVALID_IDENTITY_MODE
        }
        p.clientPkcs12?.let {
            if (it.isEmpty() || it.size > ServerPackageContract.MAX_CLIENT_PKCS12_BYTES || it[0] != 0x30.toByte()) {
                return ServerPackageError.INVALID_CLIENT_PKCS12
            }
        }
        p.clientPkcs12Password?.let { if (!isValidSecret(it)) return ServerPackageError.INVALID_SECRET }
        p.username?.let { if (!isValidLabel(it, ServerPackageContract.MAX_USERNAME_UTF8_BYTES)) return ServerPackageError.INVALID_USERNAME }
        p.password?.let { if (!isValidSecret(it)) return ServerPackageError.INVALID_SECRET }

        when (p.trustMode) {
            ServerPackageTrustMode.PUBLIC_CA ->
                if (p.caCertificates.isNotEmpty()) return ServerPackageError.CA_NOT_ALLOWED
            ServerPackageTrustMode.PRIVATE_CA -> {
                if (p.caCertificates.isEmpty()) return ServerPackageError.CA_REQUIRED
                if (p.caCertificates.size > ServerPackageContract.MAX_CA_CERTIFICATES) return ServerPackageError.TOO_MANY_CA
                p.caCertificates.forEachIndexed { i, der ->
                    if (der.size > ServerPackageContract.MAX_CA_CERTIFICATE_BYTES || !isDerSequence(der) || !isX509Ca(der)) {
                        return ServerPackageError.INVALID_CA
                    }
                    for (j in 0 until i) if (p.caCertificates[j].contentEquals(der)) return ServerPackageError.DUPLICATE_CA
                }
            }
            else -> return ServerPackageError.INVALID_TRUST_MODE
        }
        return null
    }

    private fun isValidLabel(value: String, maxUtf8Bytes: Int): Boolean {
        val size = value.toByteArray(StandardCharsets.UTF_8).size
        if (size == 0 || size > maxUtf8Bytes || value.isBlank() || value != value.trim()) return false
        var i = 0
        while (i < value.length) {
            val cp = value.codePointAt(i)
            val type = Character.getType(cp)
            if (Character.isISOControl(cp) ||
                type == Character.FORMAT.toInt() ||
                type == Character.SURROGATE.toInt() ||
                type == Character.UNASSIGNED.toInt() ||
                type == Character.LINE_SEPARATOR.toInt() ||
                type == Character.PARAGRAPH_SEPARATOR.toInt()
            ) {
                return false
            }
            i += Character.charCount(cp)
        }
        return true
    }

    /** Strict UTF-8, 1..256 bytes, no C0/DEL controls; decoded chars are wiped immediately. */
    private fun isValidSecret(bytes: ByteArray): Boolean {
        if (bytes.isEmpty() || bytes.size > ServerPackageContract.MAX_SECRET_UTF8_BYTES) return false
        if (bytes.any { (it.toInt() and 0xff) < 0x20 || it.toInt() == 0x7f }) return false
        val chars = CharBuffer.allocate(bytes.size)
        return try {
            val result = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes), chars, true)
            !result.isError
        } finally {
            chars.array().fill('\u0000')
        }
    }

    /** Structural DER check: one definite-length SEQUENCE spanning the whole value. */
    private fun isDerSequence(der: ByteArray): Boolean {
        if (der.size < 2 || der[0] != 0x30.toByte()) return false
        val first = der[1].toInt() and 0xff
        val (length, headerBytes) = when {
            first < 0x80 -> first to 2
            first == 0x81 -> {
                if (der.size < 3) return false
                val l = der[2].toInt() and 0xff
                if (l < 0x80) return false
                l to 3
            }
            first == 0x82 -> {
                if (der.size < 4) return false
                val l = ((der[2].toInt() and 0xff) shl 8) or (der[3].toInt() and 0xff)
                if (l < 0x100) return false
                l to 4
            }
            else -> return false
        }
        return headerBytes + length == der.size
    }

    /** Single X.509 certificate with CA basic constraints whose DER re-encodes exactly. */
    private fun isX509Ca(der: ByteArray): Boolean = try {
        val cert = CertificateFactory.getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(der)) as X509Certificate
        cert.basicConstraints >= 0 && cert.encoded.contentEquals(der)
    } catch (_: Exception) {
        false
    }

    private fun decodeUtf8(bytes: ByteArray): String? = try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (_: CharacterCodingException) {
        null
    }

    private fun singleByte(value: ByteArray): Int {
        if (value.size != 1) fail(ServerPackageError.BAD_LENGTH)
        return value[0].toInt() and 0xff
    }

    private fun ByteBuffer.tlv(tag: Int, value: ByteArray) {
        put(tag.toByte()).put((value.size ushr 8).toByte()).put(value.size.toByte()).put(value)
    }

    private fun fail(error: ServerPackageError): Nothing = throw ServerPackageException(error)
}

/** Hostname or IP literal only: no scheme, port, path, userinfo, query, brackets or whitespace. */
object ServerHostValidator {
    private val label = Regex("[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?")
    private val ipv4Octet = Regex("0|[1-9][0-9]{0,2}")
    private val hexGroup = Regex("[0-9a-f]{1,4}")

    /** Hostnames must be lowercase ASCII LDH; IPv6 lowercase without brackets or zone IDs. */
    fun isValidHost(host: String): Boolean {
        if (host.isEmpty() || host.length > ServerPackageContract.MAX_HOST_CHARS) return false
        if (host.contains(':')) return isIpv6(host)
        if (host.all { it.isDigit() || it == '.' }) return isIpv4(host)
        val labels = host.split('.')
        if (labels.any { !label.matches(it) }) return false
        return !labels.last().all { it.isDigit() }
    }

    fun isIpv4(host: String): Boolean {
        val parts = host.split('.')
        return parts.size == 4 && parts.all { ipv4Octet.matches(it) && it.toInt() <= 255 }
    }

    fun isIpv6(host: String): Boolean {
        if (host.length > 45) return false
        val doubleColon = host.indexOf("::")
        if (doubleColon >= 0 && host.indexOf("::", doubleColon + 1) >= 0) return false
        fun groupCount(part: String): Int? {
            if (part.isEmpty()) return 0
            val groups = part.split(':')
            var count = 0
            groups.forEachIndexed { i, s ->
                when {
                    hexGroup.matches(s) -> count++
                    i == groups.lastIndex && isIpv4(s) -> count += 2
                    else -> return null
                }
            }
            return count
        }
        return if (doubleColon >= 0) {
            val headPart = host.substring(0, doubleColon)
            if (headPart.contains('.')) return false
            val head = groupCount(headPart) ?: return false
            val tail = groupCount(host.substring(doubleColon + 2)) ?: return false
            head + tail <= 7
        } else {
            groupCount(host) == 8
        }
    }
}
