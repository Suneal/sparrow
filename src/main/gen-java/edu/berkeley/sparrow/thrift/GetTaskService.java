/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package edu.berkeley.sparrow.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTaskService {

  public interface Iface {

    public List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> getTask(String requestId, edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress) throws org.apache.thrift.TException;

  }

  public interface AsyncIface {

    public void getTask(String requestId, edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.getTask_call> resultHandler) throws org.apache.thrift.TException;

  }

  public static class Client extends org.apache.thrift.TServiceClient implements Iface {
    public static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {
      public Factory() {}
      public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
        return new Client(prot);
      }
      public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
        return new Client(iprot, oprot);
      }
    }

    public Client(org.apache.thrift.protocol.TProtocol prot)
    {
      super(prot, prot);
    }

    public Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
      super(iprot, oprot);
    }

    public List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> getTask(String requestId, edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress) throws org.apache.thrift.TException
    {
      send_getTask(requestId, nodeMonitorAddress);
      return recv_getTask();
    }

    public void send_getTask(String requestId, edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress) throws org.apache.thrift.TException
    {
      getTask_args args = new getTask_args();
      args.setRequestId(requestId);
      args.setNodeMonitorAddress(nodeMonitorAddress);
      sendBase("getTask", args);
    }

    public List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> recv_getTask() throws org.apache.thrift.TException
    {
      getTask_result result = new getTask_result();
      receiveBase(result, "getTask");
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getTask failed: unknown result");
    }

  }
  public static class AsyncClient extends org.apache.thrift.async.TAsyncClient implements AsyncIface {
    public static class Factory implements org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {
      private org.apache.thrift.async.TAsyncClientManager clientManager;
      private org.apache.thrift.protocol.TProtocolFactory protocolFactory;
      public Factory(org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
        this.clientManager = clientManager;
        this.protocolFactory = protocolFactory;
      }
      public AsyncClient getAsyncClient(org.apache.thrift.transport.TNonblockingTransport transport) {
        return new AsyncClient(protocolFactory, clientManager, transport);
      }
    }

    public AsyncClient(org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.transport.TNonblockingTransport transport) {
      super(protocolFactory, clientManager, transport);
    }

    public void getTask(String requestId, edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress, org.apache.thrift.async.AsyncMethodCallback<getTask_call> resultHandler) throws org.apache.thrift.TException {
      checkReady();
      getTask_call method_call = new getTask_call(requestId, nodeMonitorAddress, resultHandler, this, ___protocolFactory, ___transport);
      this.___currentMethod = method_call;
      ___manager.call(method_call);
    }

    public static class getTask_call extends org.apache.thrift.async.TAsyncMethodCall {
      private String requestId;
      private edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress;
      public getTask_call(String requestId, edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress, org.apache.thrift.async.AsyncMethodCallback<getTask_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.requestId = requestId;
        this.nodeMonitorAddress = nodeMonitorAddress;
      }

      public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getTask", org.apache.thrift.protocol.TMessageType.CALL, 0));
        getTask_args args = new getTask_args();
        args.setRequestId(requestId);
        args.setNodeMonitorAddress(nodeMonitorAddress);
        args.write(prot);
        prot.writeMessageEnd();
      }

      public List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> getResult() throws org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_getTask();
      }
    }

  }

  public static class Processor<I extends Iface> extends org.apache.thrift.TBaseProcessor<I> implements org.apache.thrift.TProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());
    public Processor(I iface) {
      super(iface, getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
    }

    protected Processor(I iface, Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      super(iface, getProcessMap(processMap));
    }

    private static <I extends Iface> Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> getProcessMap(Map<String,  org.apache.thrift.ProcessFunction<I, ? extends  org.apache.thrift.TBase>> processMap) {
      processMap.put("getTask", new getTask());
      return processMap;
    }

    public static class getTask<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getTask_args> {
      public getTask() {
        super("getTask");
      }

      public getTask_args getEmptyArgsInstance() {
        return new getTask_args();
      }

      public getTask_result getResult(I iface, getTask_args args) throws org.apache.thrift.TException {
        getTask_result result = new getTask_result();
        result.success = iface.getTask(args.requestId, args.nodeMonitorAddress);
        return result;
      }
    }

  }

  public static class getTask_args implements org.apache.thrift.TBase<getTask_args, getTask_args._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getTask_args");

    private static final org.apache.thrift.protocol.TField REQUEST_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("requestId", org.apache.thrift.protocol.TType.STRING, (short)1);
    private static final org.apache.thrift.protocol.TField NODE_MONITOR_ADDRESS_FIELD_DESC = new org.apache.thrift.protocol.TField("nodeMonitorAddress", org.apache.thrift.protocol.TType.STRUCT, (short)2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new getTask_argsStandardSchemeFactory());
      schemes.put(TupleScheme.class, new getTask_argsTupleSchemeFactory());
    }

    public String requestId; // required
    public edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      REQUEST_ID((short)1, "requestId"),
      NODE_MONITOR_ADDRESS((short)2, "nodeMonitorAddress");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: // REQUEST_ID
            return REQUEST_ID;
          case 2: // NODE_MONITOR_ADDRESS
            return NODE_MONITOR_ADDRESS;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments
    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.REQUEST_ID, new org.apache.thrift.meta_data.FieldMetaData("requestId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
      tmpMap.put(_Fields.NODE_MONITOR_ADDRESS, new org.apache.thrift.meta_data.FieldMetaData("nodeMonitorAddress", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, edu.berkeley.sparrow.thrift.THostPort.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getTask_args.class, metaDataMap);
    }

    public getTask_args() {
    }

    public getTask_args(
      String requestId,
      edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress)
    {
      this();
      this.requestId = requestId;
      this.nodeMonitorAddress = nodeMonitorAddress;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public getTask_args(getTask_args other) {
      if (other.isSetRequestId()) {
        this.requestId = other.requestId;
      }
      if (other.isSetNodeMonitorAddress()) {
        this.nodeMonitorAddress = new edu.berkeley.sparrow.thrift.THostPort(other.nodeMonitorAddress);
      }
    }

    public getTask_args deepCopy() {
      return new getTask_args(this);
    }

    public void clear() {
      this.requestId = null;
      this.nodeMonitorAddress = null;
    }

    public String getRequestId() {
      return this.requestId;
    }

    public getTask_args setRequestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    public void unsetRequestId() {
      this.requestId = null;
    }

    /** Returns true if field requestId is set (has been assigned a value) and false otherwise */
    public boolean isSetRequestId() {
      return this.requestId != null;
    }

    public void setRequestIdIsSet(boolean value) {
      if (!value) {
        this.requestId = null;
      }
    }

    public edu.berkeley.sparrow.thrift.THostPort getNodeMonitorAddress() {
      return this.nodeMonitorAddress;
    }

    public getTask_args setNodeMonitorAddress(edu.berkeley.sparrow.thrift.THostPort nodeMonitorAddress) {
      this.nodeMonitorAddress = nodeMonitorAddress;
      return this;
    }

    public void unsetNodeMonitorAddress() {
      this.nodeMonitorAddress = null;
    }

    /** Returns true if field nodeMonitorAddress is set (has been assigned a value) and false otherwise */
    public boolean isSetNodeMonitorAddress() {
      return this.nodeMonitorAddress != null;
    }

    public void setNodeMonitorAddressIsSet(boolean value) {
      if (!value) {
        this.nodeMonitorAddress = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case REQUEST_ID:
        if (value == null) {
          unsetRequestId();
        } else {
          setRequestId((String)value);
        }
        break;

      case NODE_MONITOR_ADDRESS:
        if (value == null) {
          unsetNodeMonitorAddress();
        } else {
          setNodeMonitorAddress((edu.berkeley.sparrow.thrift.THostPort)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case REQUEST_ID:
        return getRequestId();

      case NODE_MONITOR_ADDRESS:
        return getNodeMonitorAddress();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case REQUEST_ID:
        return isSetRequestId();
      case NODE_MONITOR_ADDRESS:
        return isSetNodeMonitorAddress();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof getTask_args)
        return this.equals((getTask_args)that);
      return false;
    }

    public boolean equals(getTask_args that) {
      if (that == null)
        return false;

      boolean this_present_requestId = true && this.isSetRequestId();
      boolean that_present_requestId = true && that.isSetRequestId();
      if (this_present_requestId || that_present_requestId) {
        if (!(this_present_requestId && that_present_requestId))
          return false;
        if (!this.requestId.equals(that.requestId))
          return false;
      }

      boolean this_present_nodeMonitorAddress = true && this.isSetNodeMonitorAddress();
      boolean that_present_nodeMonitorAddress = true && that.isSetNodeMonitorAddress();
      if (this_present_nodeMonitorAddress || that_present_nodeMonitorAddress) {
        if (!(this_present_nodeMonitorAddress && that_present_nodeMonitorAddress))
          return false;
        if (!this.nodeMonitorAddress.equals(that.nodeMonitorAddress))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(getTask_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      getTask_args typedOther = (getTask_args)other;

      lastComparison = Boolean.valueOf(isSetRequestId()).compareTo(typedOther.isSetRequestId());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetRequestId()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.requestId, typedOther.requestId);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetNodeMonitorAddress()).compareTo(typedOther.isSetNodeMonitorAddress());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetNodeMonitorAddress()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.nodeMonitorAddress, typedOther.nodeMonitorAddress);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("getTask_args(");
      boolean first = true;

      sb.append("requestId:");
      if (this.requestId == null) {
        sb.append("null");
      } else {
        sb.append(this.requestId);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("nodeMonitorAddress:");
      if (this.nodeMonitorAddress == null) {
        sb.append("null");
      } else {
        sb.append(this.nodeMonitorAddress);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te.getMessage());
      }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te.getMessage());
      }
    }

    private static class getTask_argsStandardSchemeFactory implements SchemeFactory {
      public getTask_argsStandardScheme getScheme() {
        return new getTask_argsStandardScheme();
      }
    }

    private static class getTask_argsStandardScheme extends StandardScheme<getTask_args> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, getTask_args struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 1: // REQUEST_ID
              if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                struct.requestId = iprot.readString();
                struct.setRequestIdIsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            case 2: // NODE_MONITOR_ADDRESS
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.nodeMonitorAddress = new edu.berkeley.sparrow.thrift.THostPort();
                struct.nodeMonitorAddress.read(iprot);
                struct.setNodeMonitorAddressIsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            default:
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
          }
          iprot.readFieldEnd();
        }
        iprot.readStructEnd();

        // check for required fields of primitive type, which can't be checked in the validate method
        struct.validate();
      }

      public void write(org.apache.thrift.protocol.TProtocol oprot, getTask_args struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.requestId != null) {
          oprot.writeFieldBegin(REQUEST_ID_FIELD_DESC);
          oprot.writeString(struct.requestId);
          oprot.writeFieldEnd();
        }
        if (struct.nodeMonitorAddress != null) {
          oprot.writeFieldBegin(NODE_MONITOR_ADDRESS_FIELD_DESC);
          struct.nodeMonitorAddress.write(oprot);
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class getTask_argsTupleSchemeFactory implements SchemeFactory {
      public getTask_argsTupleScheme getScheme() {
        return new getTask_argsTupleScheme();
      }
    }

    private static class getTask_argsTupleScheme extends TupleScheme<getTask_args> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, getTask_args struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetRequestId()) {
          optionals.set(0);
        }
        if (struct.isSetNodeMonitorAddress()) {
          optionals.set(1);
        }
        oprot.writeBitSet(optionals, 2);
        if (struct.isSetRequestId()) {
          oprot.writeString(struct.requestId);
        }
        if (struct.isSetNodeMonitorAddress()) {
          struct.nodeMonitorAddress.write(oprot);
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, getTask_args struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(2);
        if (incoming.get(0)) {
          struct.requestId = iprot.readString();
          struct.setRequestIdIsSet(true);
        }
        if (incoming.get(1)) {
          struct.nodeMonitorAddress = new edu.berkeley.sparrow.thrift.THostPort();
          struct.nodeMonitorAddress.read(iprot);
          struct.setNodeMonitorAddressIsSet(true);
        }
      }
    }

  }

  public static class getTask_result implements org.apache.thrift.TBase<getTask_result, getTask_result._Fields>, java.io.Serializable, Cloneable   {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getTask_result");

    private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.LIST, (short)0);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
    static {
      schemes.put(StandardScheme.class, new getTask_resultStandardSchemeFactory());
      schemes.put(TupleScheme.class, new getTask_resultTupleSchemeFactory());
    }

    public List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> success; // required

    /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
      SUCCESS((short)0, "success");

      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, or null if its not found.
       */
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: // SUCCESS
            return SUCCESS;
          default:
            return null;
        }
      }

      /**
       * Find the _Fields constant that matches fieldId, throwing an exception
       * if it is not found.
       */
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }

      /**
       * Find the _Fields constant that matches name, or null if its not found.
       */
      public static _Fields findByName(String name) {
        return byName.get(name);
      }

      private final short _thriftId;
      private final String _fieldName;

      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }

      public short getThriftFieldId() {
        return _thriftId;
      }

      public String getFieldName() {
        return _fieldName;
      }
    }

    // isset id assignments
    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
    static {
      Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, 
          new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
              new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, edu.berkeley.sparrow.thrift.TTaskLaunchSpec.class))));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getTask_result.class, metaDataMap);
    }

    public getTask_result() {
    }

    public getTask_result(
      List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> success)
    {
      this();
      this.success = success;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public getTask_result(getTask_result other) {
      if (other.isSetSuccess()) {
        List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> __this__success = new ArrayList<edu.berkeley.sparrow.thrift.TTaskLaunchSpec>();
        for (edu.berkeley.sparrow.thrift.TTaskLaunchSpec other_element : other.success) {
          __this__success.add(new edu.berkeley.sparrow.thrift.TTaskLaunchSpec(other_element));
        }
        this.success = __this__success;
      }
    }

    public getTask_result deepCopy() {
      return new getTask_result(this);
    }

    public void clear() {
      this.success = null;
    }

    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }

    public java.util.Iterator<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }

    public void addToSuccess(edu.berkeley.sparrow.thrift.TTaskLaunchSpec elem) {
      if (this.success == null) {
        this.success = new ArrayList<edu.berkeley.sparrow.thrift.TTaskLaunchSpec>();
      }
      this.success.add(elem);
    }

    public List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> getSuccess() {
      return this.success;
    }

    public getTask_result setSuccess(List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec> success) {
      this.success = success;
      return this;
    }

    public void unsetSuccess() {
      this.success = null;
    }

    /** Returns true if field success is set (has been assigned a value) and false otherwise */
    public boolean isSetSuccess() {
      return this.success != null;
    }

    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }

    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<edu.berkeley.sparrow.thrift.TTaskLaunchSpec>)value);
        }
        break;

      }
    }

    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();

      }
      throw new IllegalStateException();
    }

    /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      }
      throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof getTask_result)
        return this.equals((getTask_result)that);
      return false;
    }

    public boolean equals(getTask_result that) {
      if (that == null)
        return false;

      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(getTask_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      getTask_result typedOther = (getTask_result)other;

      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }

    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
      }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("getTask_result(");
      boolean first = true;

      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
      // check for required fields
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te.getMessage());
      }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te.getMessage());
      }
    }

    private static class getTask_resultStandardSchemeFactory implements SchemeFactory {
      public getTask_resultStandardScheme getScheme() {
        return new getTask_resultStandardScheme();
      }
    }

    private static class getTask_resultStandardScheme extends StandardScheme<getTask_result> {

      public void read(org.apache.thrift.protocol.TProtocol iprot, getTask_result struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 0: // SUCCESS
              if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                {
                  org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                  struct.success = new ArrayList<edu.berkeley.sparrow.thrift.TTaskLaunchSpec>(_list0.size);
                  for (int _i1 = 0; _i1 < _list0.size; ++_i1)
                  {
                    edu.berkeley.sparrow.thrift.TTaskLaunchSpec _elem2; // required
                    _elem2 = new edu.berkeley.sparrow.thrift.TTaskLaunchSpec();
                    _elem2.read(iprot);
                    struct.success.add(_elem2);
                  }
                  iprot.readListEnd();
                }
                struct.setSuccessIsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            default:
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
          }
          iprot.readFieldEnd();
        }
        iprot.readStructEnd();

        // check for required fields of primitive type, which can't be checked in the validate method
        struct.validate();
      }

      public void write(org.apache.thrift.protocol.TProtocol oprot, getTask_result struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.success != null) {
          oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.success.size()));
            for (edu.berkeley.sparrow.thrift.TTaskLaunchSpec _iter3 : struct.success)
            {
              _iter3.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }

    }

    private static class getTask_resultTupleSchemeFactory implements SchemeFactory {
      public getTask_resultTupleScheme getScheme() {
        return new getTask_resultTupleScheme();
      }
    }

    private static class getTask_resultTupleScheme extends TupleScheme<getTask_result> {

      @Override
      public void write(org.apache.thrift.protocol.TProtocol prot, getTask_result struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetSuccess()) {
          optionals.set(0);
        }
        oprot.writeBitSet(optionals, 1);
        if (struct.isSetSuccess()) {
          {
            oprot.writeI32(struct.success.size());
            for (edu.berkeley.sparrow.thrift.TTaskLaunchSpec _iter4 : struct.success)
            {
              _iter4.write(oprot);
            }
          }
        }
      }

      @Override
      public void read(org.apache.thrift.protocol.TProtocol prot, getTask_result struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(1);
        if (incoming.get(0)) {
          {
            org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
            struct.success = new ArrayList<edu.berkeley.sparrow.thrift.TTaskLaunchSpec>(_list5.size);
            for (int _i6 = 0; _i6 < _list5.size; ++_i6)
            {
              edu.berkeley.sparrow.thrift.TTaskLaunchSpec _elem7; // required
              _elem7 = new edu.berkeley.sparrow.thrift.TTaskLaunchSpec();
              _elem7.read(iprot);
              struct.success.add(_elem7);
            }
          }
          struct.setSuccessIsSet(true);
        }
      }
    }

  }

}
