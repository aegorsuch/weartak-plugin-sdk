/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.weartak.plugin.sdk.aidl;
/** Host-implemented control plane passed to the plugin during initialization. */
public interface IWearTakPluginControl extends android.os.IInterface
{
  /** Default implementation for IWearTakPluginControl. */
  public static class Default implements com.weartak.plugin.sdk.aidl.IWearTakPluginControl
  {
    @Override public void requestCotDispatch(com.weartak.plugin.sdk.model.CotRequestParcel request) throws android.os.RemoteException
    {
    }
    @Override public void requestAlert(com.weartak.plugin.sdk.model.AlertRequestParcel request) throws android.os.RemoteException
    {
    }
    @Override public void requestToast(com.weartak.plugin.sdk.model.ToastRequestParcel request) throws android.os.RemoteException
    {
    }
    /** Create/drop a marker; stable [MarkerParcel.markerId] is the CoT uid. */
    @Override public void requestMarkerPublish(com.weartak.plugin.sdk.model.MarkerParcel marker) throws android.os.RemoteException
    {
    }
    /** Update an existing marker by [MarkerParcel.markerId]; no-op if uid unknown. */
    @Override public void requestMarkerUpdate(com.weartak.plugin.sdk.model.MarkerParcel marker) throws android.os.RemoteException
    {
    }
    /** Clear/delete a marker by [MarkerParcel.markerId]. */
    @Override public void requestMarkerClear(com.weartak.plugin.sdk.model.MarkerParcel marker) throws android.os.RemoteException
    {
    }
    @Override public com.weartak.plugin.sdk.model.DeviceStatusParcel getDeviceStatus() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void registerCepRules(java.lang.String pluginId, java.lang.String sensorKey, java.lang.String rulesJson) throws android.os.RemoteException
    {
    }
    @Override public void unregisterCepRules(java.lang.String pluginId, java.lang.String sensorKey) throws android.os.RemoteException
    {
    }
    /** v1.x streaming path; host orders by [CepRuleEventParcel.sequence] per plugin + sensor key. */
    @Override public void applyCepRuleEvent(com.weartak.plugin.sdk.model.CepRuleEventParcel event) throws android.os.RemoteException
    {
    }
    @Override public boolean isCotConnected() throws android.os.RemoteException
    {
      return false;
    }
    @Override public com.weartak.plugin.sdk.model.HostCapabilitiesParcel getHostCapabilities() throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl getMapLayerControl() throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.weartak.plugin.sdk.aidl.IWearTakPluginControl
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.weartak.plugin.sdk.aidl.IWearTakPluginControl interface,
     * generating a proxy if needed.
     */
    public static com.weartak.plugin.sdk.aidl.IWearTakPluginControl asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.weartak.plugin.sdk.aidl.IWearTakPluginControl))) {
        return ((com.weartak.plugin.sdk.aidl.IWearTakPluginControl)iin);
      }
      return new com.weartak.plugin.sdk.aidl.IWearTakPluginControl.Stub.Proxy(obj);
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
        case TRANSACTION_requestCotDispatch:
        {
          com.weartak.plugin.sdk.model.CotRequestParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.CotRequestParcel.CREATOR);
          this.requestCotDispatch(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_requestAlert:
        {
          com.weartak.plugin.sdk.model.AlertRequestParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.AlertRequestParcel.CREATOR);
          this.requestAlert(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_requestToast:
        {
          com.weartak.plugin.sdk.model.ToastRequestParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.ToastRequestParcel.CREATOR);
          this.requestToast(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_requestMarkerPublish:
        {
          com.weartak.plugin.sdk.model.MarkerParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.MarkerParcel.CREATOR);
          this.requestMarkerPublish(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_requestMarkerUpdate:
        {
          com.weartak.plugin.sdk.model.MarkerParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.MarkerParcel.CREATOR);
          this.requestMarkerUpdate(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_requestMarkerClear:
        {
          com.weartak.plugin.sdk.model.MarkerParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.MarkerParcel.CREATOR);
          this.requestMarkerClear(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getDeviceStatus:
        {
          com.weartak.plugin.sdk.model.DeviceStatusParcel _result = this.getDeviceStatus();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_registerCepRules:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          this.registerCepRules(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_unregisterCepRules:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          this.unregisterCepRules(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_applyCepRuleEvent:
        {
          com.weartak.plugin.sdk.model.CepRuleEventParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.CepRuleEventParcel.CREATOR);
          this.applyCepRuleEvent(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_isCotConnected:
        {
          boolean _result = this.isCotConnected();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_getHostCapabilities:
        {
          com.weartak.plugin.sdk.model.HostCapabilitiesParcel _result = this.getHostCapabilities();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getMapLayerControl:
        {
          com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl _result = this.getMapLayerControl();
          reply.writeNoException();
          reply.writeStrongInterface(_result);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.weartak.plugin.sdk.aidl.IWearTakPluginControl
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
      @Override public void requestCotDispatch(com.weartak.plugin.sdk.model.CotRequestParcel request) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, request, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_requestCotDispatch, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void requestAlert(com.weartak.plugin.sdk.model.AlertRequestParcel request) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, request, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_requestAlert, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void requestToast(com.weartak.plugin.sdk.model.ToastRequestParcel request) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, request, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_requestToast, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Create/drop a marker; stable [MarkerParcel.markerId] is the CoT uid. */
      @Override public void requestMarkerPublish(com.weartak.plugin.sdk.model.MarkerParcel marker) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, marker, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_requestMarkerPublish, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Update an existing marker by [MarkerParcel.markerId]; no-op if uid unknown. */
      @Override public void requestMarkerUpdate(com.weartak.plugin.sdk.model.MarkerParcel marker) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, marker, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_requestMarkerUpdate, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Clear/delete a marker by [MarkerParcel.markerId]. */
      @Override public void requestMarkerClear(com.weartak.plugin.sdk.model.MarkerParcel marker) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, marker, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_requestMarkerClear, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public com.weartak.plugin.sdk.model.DeviceStatusParcel getDeviceStatus() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.model.DeviceStatusParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getDeviceStatus, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.model.DeviceStatusParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void registerCepRules(java.lang.String pluginId, java.lang.String sensorKey, java.lang.String rulesJson) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(pluginId);
          _data.writeString(sensorKey);
          _data.writeString(rulesJson);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerCepRules, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void unregisterCepRules(java.lang.String pluginId, java.lang.String sensorKey) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(pluginId);
          _data.writeString(sensorKey);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterCepRules, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** v1.x streaming path; host orders by [CepRuleEventParcel.sequence] per plugin + sensor key. */
      @Override public void applyCepRuleEvent(com.weartak.plugin.sdk.model.CepRuleEventParcel event) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, event, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_applyCepRuleEvent, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public boolean isCotConnected() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isCotConnected, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.weartak.plugin.sdk.model.HostCapabilitiesParcel getHostCapabilities() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.model.HostCapabilitiesParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getHostCapabilities, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.model.HostCapabilitiesParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl getMapLayerControl() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getMapLayerControl, _data, _reply, 0);
          _reply.readException();
          _result = com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl.Stub.asInterface(_reply.readStrongBinder());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_requestCotDispatch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_requestAlert = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_requestToast = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_requestMarkerPublish = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_requestMarkerUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_requestMarkerClear = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_getDeviceStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_registerCepRules = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_unregisterCepRules = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_applyCepRuleEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_isCotConnected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_getHostCapabilities = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_getMapLayerControl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
  }
  public static final java.lang.String DESCRIPTOR = "com.weartak.plugin.sdk.aidl.IWearTakPluginControl";
  public void requestCotDispatch(com.weartak.plugin.sdk.model.CotRequestParcel request) throws android.os.RemoteException;
  public void requestAlert(com.weartak.plugin.sdk.model.AlertRequestParcel request) throws android.os.RemoteException;
  public void requestToast(com.weartak.plugin.sdk.model.ToastRequestParcel request) throws android.os.RemoteException;
  /** Create/drop a marker; stable [MarkerParcel.markerId] is the CoT uid. */
  public void requestMarkerPublish(com.weartak.plugin.sdk.model.MarkerParcel marker) throws android.os.RemoteException;
  /** Update an existing marker by [MarkerParcel.markerId]; no-op if uid unknown. */
  public void requestMarkerUpdate(com.weartak.plugin.sdk.model.MarkerParcel marker) throws android.os.RemoteException;
  /** Clear/delete a marker by [MarkerParcel.markerId]. */
  public void requestMarkerClear(com.weartak.plugin.sdk.model.MarkerParcel marker) throws android.os.RemoteException;
  public com.weartak.plugin.sdk.model.DeviceStatusParcel getDeviceStatus() throws android.os.RemoteException;
  public void registerCepRules(java.lang.String pluginId, java.lang.String sensorKey, java.lang.String rulesJson) throws android.os.RemoteException;
  public void unregisterCepRules(java.lang.String pluginId, java.lang.String sensorKey) throws android.os.RemoteException;
  /** v1.x streaming path; host orders by [CepRuleEventParcel.sequence] per plugin + sensor key. */
  public void applyCepRuleEvent(com.weartak.plugin.sdk.model.CepRuleEventParcel event) throws android.os.RemoteException;
  public boolean isCotConnected() throws android.os.RemoteException;
  public com.weartak.plugin.sdk.model.HostCapabilitiesParcel getHostCapabilities() throws android.os.RemoteException;
  public com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl getMapLayerControl() throws android.os.RemoteException;
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
