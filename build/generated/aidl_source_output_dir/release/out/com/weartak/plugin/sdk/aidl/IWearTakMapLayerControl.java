/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.weartak.plugin.sdk.aidl;
/** Map-layer sub-surface. Phase-gated: hosts may return unsupported until Phase 5. */
public interface IWearTakMapLayerControl extends android.os.IInterface
{
  /** Default implementation for IWearTakMapLayerControl. */
  public static class Default implements com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl
  {
    @Override public boolean isMapLayerSupported() throws android.os.RemoteException
    {
      return false;
    }
    @Override public int applyLayerCommand(com.weartak.plugin.sdk.model.MapLayerCommandParcel command) throws android.os.RemoteException
    {
      return 0;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl interface,
     * generating a proxy if needed.
     */
    public static com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl))) {
        return ((com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl)iin);
      }
      return new com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl.Stub.Proxy(obj);
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
        case TRANSACTION_isMapLayerSupported:
        {
          boolean _result = this.isMapLayerSupported();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_applyLayerCommand:
        {
          com.weartak.plugin.sdk.model.MapLayerCommandParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.model.MapLayerCommandParcel.CREATOR);
          int _result = this.applyLayerCommand(_arg0);
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl
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
      @Override public boolean isMapLayerSupported() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isMapLayerSupported, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public int applyLayerCommand(com.weartak.plugin.sdk.model.MapLayerCommandParcel command) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, command, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_applyLayerCommand, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_isMapLayerSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_applyLayerCommand = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
  }
  public static final java.lang.String DESCRIPTOR = "com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl";
  public boolean isMapLayerSupported() throws android.os.RemoteException;
  public int applyLayerCommand(com.weartak.plugin.sdk.model.MapLayerCommandParcel command) throws android.os.RemoteException;
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
