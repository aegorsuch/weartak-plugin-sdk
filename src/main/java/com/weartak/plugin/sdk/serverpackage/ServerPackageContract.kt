package com.weartak.plugin.sdk.serverpackage

/**
 * Optional typed server package capability. Packages carry TLS server settings, optional private
 * CA certificates and, only after explicit host-owned sender opt-in, shared event credentials.
 */
object ServerPackageContract {
    const val CAPABILITY_KEY: String = "com.weartak.plugin.sdk.server-package-stage"
    const val API_VERSION: Int = 1
    const val MAX_PACKAGE_BYTES: Int = 32_700
    const val MAX_STAGE_LIFETIME_MS: Long = 300_000L
    const val MAX_EXPORT_LIFETIME_MS: Long = 300_000L

    const val MAX_NAME_UTF8_BYTES: Int = 128
    const val MAX_HOST_CHARS: Int = 253
    const val MAX_SETTINGS_BYTES: Int = 1_024
    const val MAX_CA_CERTIFICATES: Int = 4
    const val MAX_CA_CERTIFICATE_BYTES: Int = 8_192
    /** No separate cap: bounded by [MAX_PACKAGE_BYTES]; oversize reports TOO_LARGE. */
    const val MAX_CLIENT_PKCS12_BYTES: Int = MAX_PACKAGE_BYTES
    const val MAX_USERNAME_UTF8_BYTES: Int = 128
    const val MAX_SECRET_UTF8_BYTES: Int = 256

    /** Fixed TAK enrollment port used by hosts for RECEIVER_ENROLLMENT; not encoded in packages. */
    const val ENROLLMENT_PORT: Int = 8446
}

object ServerPackageTrustMode {
    /** Platform/public CA trust; package must not contain CA certificates. */
    const val PUBLIC_CA: Int = 1

    /** Private CA; package carries 1..4 DER CA certificates for explicit host review. */
    const val PRIVATE_CA: Int = 2
}

object ServerPackageIdentityMode {
    /**
     * Receiver enrolls with a freshly generated key pair. Enrollment username/password are
     * wearer-entered on the receiver unless the sender explicitly included SHARED_CREDENTIALS.
     */
    const val RECEIVER_ENROLLMENT: Int = 1

    /** Receiver needs an administrator-provided PKCS#12, staged separately via PKCS#12 V1. */
    const val ADMIN_PKCS12_SEPARATE: Int = 2

    /** Event provisioning: package carries the explicitly exported shared client PKCS#12. */
    const val SHARED_IDENTITY: Int = 3
}

/** Tag 7 bitmask; must exactly match the sensitive tags present. */
object ServerPackageSensitiveFlags {
    const val NONE: Int = 0

    /** Tags 9 (client PKCS#12) and 10 (its password) present. */
    const val CLIENT_IDENTITY: Int = 0x01

    /** Tags 11 (username) and 12 (password) present. */
    const val SHARED_CREDENTIALS: Int = 0x02

    const val ALL: Int = CLIENT_IDENTITY or SHARED_CREDENTIALS
}

/** Content-free export refusal reasons reported in export status errorCode. */
object ServerPackageExportReason {
    const val NONE: Int = 0
    const val USER_DECLINED: Int = 1
    const val NONEXPORTABLE_KEY: Int = 2
    const val MANAGED_RESTRICTED: Int = 3
    const val NO_ELIGIBLE_SERVER: Int = 4
    const val TOO_LARGE: Int = 5
    const val INVALID_HANDLE: Int = 6
    const val UNAUTHORIZED: Int = 7
    const val FAILED: Int = 8
}

/** Host-side export flow states. */
object ServerPackageExportState {
    const val PENDING: Int = 1
    const val READY: Int = 2
    const val TAKEN: Int = 3
    const val CANCELLED: Int = 4
    const val EXPIRED: Int = 5
    const val REJECTED: Int = 6
}

object ServerPackageExportCode {
    const val PENDING: Int = 1
    const val UNSUPPORTED: Int = 2
    const val INVALID_REQUEST: Int = 3
    const val REPLAYED_REQUEST: Int = 4
    const val UNAUTHORIZED: Int = 5
    const val HOST_BUSY: Int = 6
    const val FAILED: Int = 7
}

object ServerPackageTakeCode {
    const val OK: Int = 1
    const val NOT_READY: Int = 2
    const val INVALID_HANDLE: Int = 3
    const val UNAUTHORIZED: Int = 4
    const val EXPIRED: Int = 5
    const val TERMINAL: Int = 6
    const val FAILED: Int = 7
}
