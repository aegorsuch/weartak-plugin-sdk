/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.weartak.plugin.sdk.v2.aidl;
/**
 * v2 host-implemented control plane. Plugin→host event calls are synchronous and return [V2HostResultParcel].
 * Capability probes remain synchronous.
 */
public interface IWearTakPluginHostControlV2 extends android.os.IInterface
{
  /** Default implementation for IWearTakPluginHostControlV2. */
  public static class Default implements com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2
  {
    @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel applyCepRuleEvent(com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel event) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel publishDetectionEvent(com.weartak.plugin.sdk.v2.model.DetectionEventParcel event) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel submitTaskCommand(com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel request) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel reportTaskCommandProgress(com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel progress) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel reportTaskCommandCompletion(com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel completion) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel getHostCapabilities() throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2 interface,
     * generating a proxy if needed.
     */
    public static com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2 asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2))) {
        return ((com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2)iin);
      }
      return new com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2.Stub.Proxy(obj);
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
        case TRANSACTION_applyCepRuleEvent:
        {
          com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel.CREATOR);
          com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result = this.applyCepRuleEvent(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_publishDetectionEvent:
        {
          com.weartak.plugin.sdk.v2.model.DetectionEventParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.v2.model.DetectionEventParcel.CREATOR);
          com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result = this.publishDetectionEvent(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_submitTaskCommand:
        {
          com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel.CREATOR);
          com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result = this.submitTaskCommand(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_reportTaskCommandProgress:
        {
          com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel.CREATOR);
          com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result = this.reportTaskCommandProgress(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_reportTaskCommandCompletion:
        {
          com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel.CREATOR);
          com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result = this.reportTaskCommandCompletion(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getHostCapabilities:
        {
          com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel _result = this.getHostCapabilities();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2
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
      @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel applyCepRuleEvent(com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel event) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, event, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_applyCepRuleEvent, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.v2.model.V2HostResultParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel publishDetectionEvent(com.weartak.plugin.sdk.v2.model.DetectionEventParcel event) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, event, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_publishDetectionEvent, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.v2.model.V2HostResultParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel submitTaskCommand(com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel request) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, request, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_submitTaskCommand, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.v2.model.V2HostResultParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel reportTaskCommandProgress(com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel progress) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, progress, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_reportTaskCommandProgress, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.v2.model.V2HostResultParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.weartak.plugin.sdk.v2.model.V2HostResultParcel reportTaskCommandCompletion(com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel completion) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.v2.model.V2HostResultParcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, completion, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_reportTaskCommandCompletion, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.v2.model.V2HostResultParcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel getHostCapabilities() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getHostCapabilities, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_applyCepRuleEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_publishDetectionEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_submitTaskCommand = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_reportTaskCommandProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_reportTaskCommandCompletion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_getHostCapabilities = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
  }
  public static final java.lang.String DESCRIPTOR = "com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2";
  public com.weartak.plugin.sdk.v2.model.V2HostResultParcel applyCepRuleEvent(com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel event) throws android.os.RemoteException;
  public com.weartak.plugin.sdk.v2.model.V2HostResultParcel publishDetectionEvent(com.weartak.plugin.sdk.v2.model.DetectionEventParcel event) throws android.os.RemoteException;
  public com.weartak.plugin.sdk.v2.model.V2HostResultParcel submitTaskCommand(com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel request) throws android.os.RemoteException;
  public com.weartak.plugin.sdk.v2.model.V2HostResultParcel reportTaskCommandProgress(com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel progress) throws android.os.RemoteException;
  public com.weartak.plugin.sdk.v2.model.V2HostResultParcel reportTaskCommandCompletion(com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel completion) throws android.os.RemoteException;
  public com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel getHostCapabilities() throws android.os.RemoteException;
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
