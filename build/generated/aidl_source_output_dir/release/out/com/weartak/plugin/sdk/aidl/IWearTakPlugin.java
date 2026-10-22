/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.weartak.plugin.sdk.aidl;
/** Plugin-owned binder surface. Host binds with {@link com.weartak.plugin.sdk.WearTakPluginContract#PERMISSION_BIND_PLUGIN}. */
public interface IWearTakPlugin extends android.os.IInterface
{
  /** Default implementation for IWearTakPlugin. */
  public static class Default implements com.weartak.plugin.sdk.aidl.IWearTakPlugin
  {
    @Override public com.weartak.plugin.sdk.model.PluginInfoParcel getPluginInfo() throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.weartak.plugin.sdk.model.PluginCapabilitiesParcel getCapabilities() throws android.os.RemoteException
    {
      return null;
    }
    @Override public int getApiVersion() throws android.os.RemoteException
    {
      return 0;
    }
    @Override public com.weartak.plugin.sdk.model.PluginStatusParcel getStatus() throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Legacy compatibility init. The host will no longer call this path in the consolidated
     * init flow; new plugins should implement [initializePluginV2] as the canonical startup
     * entry point.
     */
    @Override public void initializePlugin(com.weartak.plugin.sdk.model.PluginInitParcel init, com.weartak.plugin.sdk.aidl.IWearTakPluginControl control) throws android.os.RemoteException
    {
    }
    /**
     * Canonical host-called initialization entry point.
     * 
     * `controlV2 == null` means the host does not currently provide v2 event-family support.
     * The plugin should treat that as v1 runtime only and continue on [IWearTakPluginControl].
     */
    @Override public void initializePluginV2(com.weartak.plugin.sdk.model.PluginInitParcel init, com.weartak.plugin.sdk.aidl.IWearTakPluginControl control, com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2 controlV2) throws android.os.RemoteException
    {
    }
    @Override public void startPlugin() throws android.os.RemoteException
    {
    }
    @Override public void stopPlugin() throws android.os.RemoteException
    {
    }
    @Override public void shutdownPlugin() throws android.os.RemoteException
    {
    }
    @Override public void registerHostCallback(com.weartak.plugin.sdk.aidl.IWearTakHostCallback callback) throws android.os.RemoteException
    {
    }
    @Override public void unregisterHostCallback(com.weartak.plugin.sdk.aidl.IWearTakHostCallback callback) throws android.os.RemoteException
    {
    }
    @Override public com.weartak.plugin.sdk.model.SettingsSchemaParcel getSettingsSchema() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.os.Bundle getSettingsValues() throws android.os.RemoteException
    {
      return null;
    }
    /** Legacy apply; prefer [applySettingsWithResult] for structured outcomes. */
    @Override public void applySettings(android.os.Bundle values) throws android.os.RemoteException
    {
    }
    /**
     * Structured apply with explicit [applyMode] ([com.weartak.plugin.sdk.model.SettingsApplyMode]).
     * Host may pass a host snapshot via [onHostSettingsChanged] before apply.
     */
    @Override public com.weartak.plugin.sdk.model.SettingsApplyResultParcel applySettingsWithResult(android.os.Bundle values, int applyMode) throws android.os.RemoteException
    {
      return null;
    }
    /** Host pushes an updated settings snapshot (additive; no-op on legacy plugins). */
    @Override public void onHostSettingsChanged(com.weartak.plugin.sdk.model.HostSettingsParcel hostSettings) throws android.os.RemoteException
    {
    }
    @Override public com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel getMainUiSchema() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.os.Bundle getMainUiState() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void invokeMainUiAction(java.lang.String actionId, android.os.Bundle args) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.weartak.plugin.sdk.aidl.IWearTakPlugin
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.weartak.plugin.sdk.aidl.IWearTakPlugin interface,
     * generating a proxy if needed.
     */
    public static com.weartak.plugin.sdk.aidl.IWearTakPlugin asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.weartak.plugin.sdk.aidl.IWearTakPlugin))) {
        return ((com.weartak.plugin.sdk.aidl.IWearTakPlugin)iin);
      }
      return new com.weartak.plugin.sdk.aidl.IWearTakPlugin.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_getPluginInfo:
        {
          com.weartak.plugin.sdk.model.PluginInfoParcel _result = this.getPluginInfo();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getCapabilities:
        {
          com.weartak.plugin.sdk.model.PluginCapabilitiesParcel _result = this.getCapabilities();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getApiVersion:
        {
          int _result = this.getApiVersion();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getStatus:
        {
          com.weartak.plugin.sdk.model.PluginStatusParcel _result = this.getStatus();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_initializePlugin:
        {
          com.weartak.plugin.sdk.model.PluginInitParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.PluginInitParcel.CREATOR);
          com.weartak.plugin.sdk.aidl.IWearTakPluginControl _arg1;
          _arg1 = com.weartak.plugin.sdk.aidl.IWearTakPluginControl.Stub.asInterface(data.readStrongBinder());
          this.initializePlugin(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_initializePluginV2:
        {
          com.weartak.plugin.sdk.model.PluginInitParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.PluginInitParcel.CREATOR);
          com.weartak.plugin.sdk.aidl.IWearTakPluginControl _arg1;
          _arg1 = com.weartak.plugin.sdk.aidl.IWearTakPluginControl.Stub.asInterface(data.readStrongBinder());
          com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2 _arg2;
          _arg2 = com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2.Stub.asInterface(data.readStrongBinder());
          this.initializePluginV2(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_startPlugin:
        {
          this.startPlugin();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_stopPlugin:
        {
          this.stopPlugin();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_shutdownPlugin:
        {
          this.shutdownPlugin();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_registerHostCallback:
        {
          com.weartak.plugin.sdk.aidl.IWearTakHostCallback _arg0;
          _arg0 = com.weartak.plugin.sdk.aidl.IWearTakHostCallback.Stub.asInterface(data.readStrongBinder());
          this.registerHostCallback(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_unregisterHostCallback:
        {
          com.weartak.plugin.sdk.aidl.IWearTakHostCallback _arg0;
          _arg0 = com.weartak.plugin.sdk.aidl.IWearTakHostCallback.Stub.asInterface(data.readStrongBinder());
          this.unregisterHostCallback(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getSettingsSchema:
        {
          com.weartak.plugin.sdk.model.SettingsSchemaParcel _result = this.getSettingsSchema();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getSettingsValues:
        {
          android.os.Bundle _result = this.getSettingsValues();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_applySettings:
        {
          android.os.Bundle _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          this.applySettings(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_applySettingsWithResult:
        {
          android.os.Bundle _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          int _arg1;
          _arg1 = data.readInt();
          com.weartak.plugin.sdk.model.SettingsApplyResultParcel _result = this.applySettingsWithResult(_arg0, _arg1);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_onHostSettingsChanged:
        {
          com.weartak.plugin.sdk.model.HostSettingsParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.HostSettingsParcel.CREATOR);
          this.onHostSettingsChanged(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getMainUiSchema:
        {
          com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel _result = this.getMainUiSchema();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getMainUiState:
        {
          android.os.Bundle _result = this.getMainUiState();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_invokeMainUiAction:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.os.Bundle _arg1;
          _arg1 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          this.invokeMainUiAction(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.weartak.plugin.sdk.aidl.IWearTakPlugin
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public com.weartak.plugin.sdk.model.PluginInfoParcel getPluginInfo() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.model.PluginInfoParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPluginInfo, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.model.PluginInfoParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.weartak.plugin.sdk.model.PluginCapabilitiesParcel getCapabilities() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.model.PluginCapabilitiesParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getCapabilities, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.model.PluginCapabilitiesParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public int getApiVersion() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getApiVersion, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.weartak.plugin.sdk.model.PluginStatusParcel getStatus() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.model.PluginStatusParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getStatus, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.model.PluginStatusParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Legacy compatibility init. The host will no longer call this path in the consolidated
       * init flow; new plugins should implement [initializePluginV2] as the canonical startup
       * entry point.
       */
      @Override public void initializePlugin(com.weartak.plugin.sdk.model.PluginInitParcel init, com.weartak.plugin.sdk.aidl.IWearTakPluginControl control) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, init, 0);
          _data.writeStrongInterface(control);
          boolean _status = mRemote.transact(Stub.TRANSACTION_initializePlugin, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * Canonical host-called initialization entry point.
       * 
       * `controlV2 == null` means the host does not currently provide v2 event-family support.
       * The plugin should treat that as v1 runtime only and continue on [IWearTakPluginControl].
       */
      @Override public void initializePluginV2(com.weartak.plugin.sdk.model.PluginInitParcel init, com.weartak.plugin.sdk.aidl.IWearTakPluginControl control, com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2 controlV2) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, init, 0);
          _data.writeStrongInterface(control);
          _data.writeStrongInterface(controlV2);
          boolean _status = mRemote.transact(Stub.TRANSACTION_initializePluginV2, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void startPlugin() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startPlugin, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void stopPlugin() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopPlugin, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void shutdownPlugin() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_shutdownPlugin, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void registerHostCallback(com.weartak.plugin.sdk.aidl.IWearTakHostCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerHostCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void unregisterHostCallback(com.weartak.plugin.sdk.aidl.IWearTakHostCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterHostCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public com.weartak.plugin.sdk.model.SettingsSchemaParcel getSettingsSchema() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.model.SettingsSchemaParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSettingsSchema, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.model.SettingsSchemaParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public android.os.Bundle getSettingsValues() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSettingsValues, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.os.Bundle.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Legacy apply; prefer [applySettingsWithResult] for structured outcomes. */
      @Override public void applySettings(android.os.Bundle values) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, values, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_applySettings, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * Structured apply with explicit [applyMode] ([com.weartak.plugin.sdk.model.SettingsApplyMode]).
       * Host may pass a host snapshot via [onHostSettingsChanged] before apply.
       */
      @Override public com.weartak.plugin.sdk.model.SettingsApplyResultParcel applySettingsWithResult(android.os.Bundle values, int applyMode) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.model.SettingsApplyResultParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, values, 0);
          _data.writeInt(applyMode);
          boolean _status = mRemote.transact(Stub.TRANSACTION_applySettingsWithResult, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.model.SettingsApplyResultParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Host pushes an updated settings snapshot (additive; no-op on legacy plugins). */
      @Override public void onHostSettingsChanged(com.weartak.plugin.sdk.model.HostSettingsParcel hostSettings) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, hostSettings, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onHostSettingsChanged, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel getMainUiSchema() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getMainUiSchema, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public android.os.Bundle getMainUiState() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getMainUiState, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.os.Bundle.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void invokeMainUiAction(java.lang.String actionId, android.os.Bundle args) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(actionId);
          _Parcel.writeTypedObject(_data, args, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_invokeMainUiAction, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_getPluginInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getCapabilities = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getApiVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_initializePlugin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_initializePluginV2 = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_startPlugin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_stopPlugin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_shutdownPlugin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_registerHostCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_unregisterHostCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_getSettingsSchema = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_getSettingsValues = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    static final int TRANSACTION_applySettings = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
    static final int TRANSACTION_applySettingsWithResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
    static final int TRANSACTION_onHostSettingsChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
    static final int TRANSACTION_getMainUiSchema = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
    static final int TRANSACTION_getMainUiState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
    static final int TRANSACTION_invokeMainUiAction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
  }
  public static final java.lang.String DESCRIPTOR = "com.weartak.plugin.sdk.aidl.IWearTakPlugin";
  public com.weartak.plugin.sdk.model.PluginInfoParcel getPluginInfo() throws android.os.RemoteException;
  public com.weartak.plugin.sdk.model.PluginCapabilitiesParcel getCapabilities() throws android.os.RemoteException;
  public int getApiVersion() throws android.os.RemoteException;
  public com.weartak.plugin.sdk.model.PluginStatusParcel getStatus() throws android.os.RemoteException;
  /**
   * Legacy compatibility init. The host will no longer call this path in the consolidated
   * init flow; new plugins should implement [initializePluginV2] as the canonical startup
   * entry point.
   */
  public void initializePlugin(com.weartak.plugin.sdk.model.PluginInitParcel init, com.weartak.plugin.sdk.aidl.IWearTakPluginControl control) throws android.os.RemoteException;
  /**
   * Canonical host-called initialization entry point.
   * 
   * `controlV2 == null` means the host does not currently provide v2 event-family support.
   * The plugin should treat that as v1 runtime only and continue on [IWearTakPluginControl].
   */
  public void initializePluginV2(com.weartak.plugin.sdk.model.PluginInitParcel init, com.weartak.plugin.sdk.aidl.IWearTakPluginControl control, com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2 controlV2) throws android.os.RemoteException;
  public void startPlugin() throws android.os.RemoteException;
  public void stopPlugin() throws android.os.RemoteException;
  public void shutdownPlugin() throws android.os.RemoteException;
  public void registerHostCallback(com.weartak.plugin.sdk.aidl.IWearTakHostCallback callback) throws android.os.RemoteException;
  public void unregisterHostCallback(com.weartak.plugin.sdk.aidl.IWearTakHostCallback callback) throws android.os.RemoteException;
  public com.weartak.plugin.sdk.model.SettingsSchemaParcel getSettingsSchema() throws android.os.RemoteException;
  public android.os.Bundle getSettingsValues() throws android.os.RemoteException;
  /** Legacy apply; prefer [applySettingsWithResult] for structured outcomes. */
  public void applySettings(android.os.Bundle values) throws android.os.RemoteException;
  /**
   * Structured apply with explicit [applyMode] ([com.weartak.plugin.sdk.model.SettingsApplyMode]).
   * Host may pass a host snapshot via [onHostSettingsChanged] before apply.
   */
  public com.weartak.plugin.sdk.model.SettingsApplyResultParcel applySettingsWithResult(android.os.Bundle values, int applyMode) throws android.os.RemoteException;
  /** Host pushes an updated settings snapshot (additive; no-op on legacy plugins). */
  public void onHostSettingsChanged(com.weartak.plugin.sdk.model.HostSettingsParcel hostSettings) throws android.os.RemoteException;
  public com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel getMainUiSchema() throws android.os.RemoteException;
  public android.os.Bundle getMainUiState() throws android.os.RemoteException;
  public void invokeMainUiAction(java.lang.String actionId, android.os.Bundle args) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
