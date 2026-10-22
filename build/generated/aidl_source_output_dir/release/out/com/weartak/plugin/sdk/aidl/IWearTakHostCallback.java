/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.weartak.plugin.sdk.aidl;
/** Plugin-to-host callbacks for streaming data and contract notifications. */
public interface IWearTakHostCallback extends android.os.IInterface
{
  /** Default implementation for IWearTakHostCallback. */
  public static class Default implements com.weartak.plugin.sdk.aidl.IWearTakHostCallback
  {
    @Override public void onSensorEvent(com.weartak.plugin.sdk.model.SensorEventParcel event) throws android.os.RemoteException
    {
    }
    @Override public void onSensorBatchEvent(java.util.List<com.weartak.plugin.sdk.model.SensorEventParcel> events) throws android.os.RemoteException
    {
    }
    @Override public void onCotReadyEvent(com.weartak.plugin.sdk.model.CotReadyEventParcel event) throws android.os.RemoteException
    {
    }
    @Override public void onPluginSettingsSchemaChanged() throws android.os.RemoteException
    {
    }
    @Override public void onPluginSettingsValuesChanged() throws android.os.RemoteException
    {
    }
    @Override public void onPluginMainUiSchemaChanged() throws android.os.RemoteException
    {
    }
    @Override public void onPluginMainUiStateChanged() throws android.os.RemoteException
    {
    }
    @Override public void onPluginStatusChanged(com.weartak.plugin.sdk.model.PluginStatusParcel status) throws android.os.RemoteException
    {
    }
    @Override public void onPluginError(com.weartak.plugin.sdk.model.PluginErrorParcel error) throws android.os.RemoteException
    {
    }
    @Override public void onSharedBufferAvailable(com.weartak.plugin.sdk.model.SharedBufferParcel buffer) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.weartak.plugin.sdk.aidl.IWearTakHostCallback
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.weartak.plugin.sdk.aidl.IWearTakHostCallback interface,
     * generating a proxy if needed.
     */
    public static com.weartak.plugin.sdk.aidl.IWearTakHostCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.weartak.plugin.sdk.aidl.IWearTakHostCallback))) {
        return ((com.weartak.plugin.sdk.aidl.IWearTakHostCallback)iin);
      }
      return new com.weartak.plugin.sdk.aidl.IWearTakHostCallback.Stub.Proxy(obj);
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
        case TRANSACTION_onSensorEvent:
        {
          com.weartak.plugin.sdk.model.SensorEventParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.SensorEventParcel.CREATOR);
          this.onSensorEvent(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onSensorBatchEvent:
        {
          java.util.List<com.weartak.plugin.sdk.model.SensorEventParcel> _arg0;
          _arg0 = data.createTypedArrayList(com.weartak.plugin.sdk.model.SensorEventParcel.CREATOR);
          this.onSensorBatchEvent(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onCotReadyEvent:
        {
          com.weartak.plugin.sdk.model.CotReadyEventParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.CotReadyEventParcel.CREATOR);
          this.onCotReadyEvent(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onPluginSettingsSchemaChanged:
        {
          this.onPluginSettingsSchemaChanged();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onPluginSettingsValuesChanged:
        {
          this.onPluginSettingsValuesChanged();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onPluginMainUiSchemaChanged:
        {
          this.onPluginMainUiSchemaChanged();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onPluginMainUiStateChanged:
        {
          this.onPluginMainUiStateChanged();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onPluginStatusChanged:
        {
          com.weartak.plugin.sdk.model.PluginStatusParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.PluginStatusParcel.CREATOR);
          this.onPluginStatusChanged(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onPluginError:
        {
          com.weartak.plugin.sdk.model.PluginErrorParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.PluginErrorParcel.CREATOR);
          this.onPluginError(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_onSharedBufferAvailable:
        {
          com.weartak.plugin.sdk.model.SharedBufferParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.SharedBufferParcel.CREATOR);
          this.onSharedBufferAvailable(_arg0);
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
    private static class Proxy implements com.weartak.plugin.sdk.aidl.IWearTakHostCallback
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
      @Override public void onSensorEvent(com.weartak.plugin.sdk.model.SensorEventParcel event) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, event, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSensorEvent, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onSensorBatchEvent(java.util.List<com.weartak.plugin.sdk.model.SensorEventParcel> events) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedList(_data, events, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSensorBatchEvent, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onCotReadyEvent(com.weartak.plugin.sdk.model.CotReadyEventParcel event) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, event, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onCotReadyEvent, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onPluginSettingsSchemaChanged() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPluginSettingsSchemaChanged, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onPluginSettingsValuesChanged() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPluginSettingsValuesChanged, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onPluginMainUiSchemaChanged() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPluginMainUiSchemaChanged, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onPluginMainUiStateChanged() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPluginMainUiStateChanged, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onPluginStatusChanged(com.weartak.plugin.sdk.model.PluginStatusParcel status) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, status, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPluginStatusChanged, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onPluginError(com.weartak.plugin.sdk.model.PluginErrorParcel error) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, error, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPluginError, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void onSharedBufferAvailable(com.weartak.plugin.sdk.model.SharedBufferParcel buffer) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, buffer, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSharedBufferAvailable, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_onSensorEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onSensorBatchEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_onCotReadyEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_onPluginSettingsSchemaChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_onPluginSettingsValuesChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_onPluginMainUiSchemaChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_onPluginMainUiStateChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_onPluginStatusChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_onPluginError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_onSharedBufferAvailable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
  }
  public static final java.lang.String DESCRIPTOR = "com.weartak.plugin.sdk.aidl.IWearTakHostCallback";
  public void onSensorEvent(com.weartak.plugin.sdk.model.SensorEventParcel event) throws android.os.RemoteException;
  public void onSensorBatchEvent(java.util.List<com.weartak.plugin.sdk.model.SensorEventParcel> events) throws android.os.RemoteException;
  public void onCotReadyEvent(com.weartak.plugin.sdk.model.CotReadyEventParcel event) throws android.os.RemoteException;
  public void onPluginSettingsSchemaChanged() throws android.os.RemoteException;
  public void onPluginSettingsValuesChanged() throws android.os.RemoteException;
  public void onPluginMainUiSchemaChanged() throws android.os.RemoteException;
  public void onPluginMainUiStateChanged() throws android.os.RemoteException;
  public void onPluginStatusChanged(com.weartak.plugin.sdk.model.PluginStatusParcel status) throws android.os.RemoteException;
  public void onPluginError(com.weartak.plugin.sdk.model.PluginErrorParcel error) throws android.os.RemoteException;
  public void onSharedBufferAvailable(com.weartak.plugin.sdk.model.SharedBufferParcel buffer) throws android.os.RemoteException;
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
    static private <T extends android.os.Parcelable> void writeTypedList(
        android.os.Parcel parcel, java.util.List<T> value, int parcelableFlags) {
      if (value == null) {
        parcel.writeInt(-1);
      } else {
        int N = value.size();
        int i = 0;
        parcel.writeInt(N);
        while (i < N) {
    writeTypedObject(parcel, value.get(i), parcelableFlags);
          i++;
        }
      }
    }
  }
}
