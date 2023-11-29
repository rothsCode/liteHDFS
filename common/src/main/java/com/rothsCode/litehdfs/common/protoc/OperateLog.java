// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/resources/protoc/namenode.proto

package com.rothsCode.litehdfs.common.protoc;

/**
 * Protobuf type {@code OperateLog}
 */
public final class OperateLog extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:OperateLog)
    OperateLogOrBuilder {

  public static final int OPERATETYPE_FIELD_NUMBER = 1;
  public static final int PATH_FIELD_NUMBER = 2;
  public static final int TXID_FIELD_NUMBER = 3;
  public static final int FILEINFO_FIELD_NUMBER = 4;
  private static final long serialVersionUID = 0L;
  // @@protoc_insertion_point(class_scope:OperateLog)
  private static final OperateLog DEFAULT_INSTANCE;
  private static final com.google.protobuf.Parser<OperateLog>
      PARSER = new com.google.protobuf.AbstractParser<OperateLog>() {
    @Override
    public OperateLog parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  static {
    DEFAULT_INSTANCE = new OperateLog();
  }

  @SuppressWarnings("serial")
  private volatile Object operateType_ = "";
  @SuppressWarnings("serial")
  private volatile Object path_ = "";
  private long txId_ = 0L;
  private ProtoFileInfo fileInfo_;
  private byte memoizedIsInitialized = -1;

  // Use OperateLog.newBuilder() to construct.
  private OperateLog(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private OperateLog() {
    operateType_ = "";
    path_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
  getDescriptor() {
    return Namenode.internal_static_OperateLog_descriptor;
  }

  public static OperateLog parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static OperateLog parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static OperateLog parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static OperateLog parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static OperateLog parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static OperateLog parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static OperateLog parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }

  public static OperateLog parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static OperateLog parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static OperateLog parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }

  public static OperateLog parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }

  public static OperateLog parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(OperateLog prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public static OperateLog getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  public static com.google.protobuf.Parser<OperateLog> parser() {
    return PARSER;
  }

  @Override
  @SuppressWarnings({"unused"})
  protected Object newInstance(
      UnusedPrivateParameter unused) {
    return new OperateLog();
  }

  @Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }

  @Override
  protected FieldAccessorTable
  internalGetFieldAccessorTable() {
    return Namenode.internal_static_OperateLog_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            OperateLog.class, Builder.class);
  }

  /**
   * <code>string operateType = 1;</code>
   *
   * @return The operateType.
   */
  @Override
  public String getOperateType() {
    Object ref = operateType_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs =
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      operateType_ = s;
      return s;
    }
  }

  /**
   * <code>string operateType = 1;</code>
   *
   * @return The bytes for operateType.
   */
  @Override
  public com.google.protobuf.ByteString
  getOperateTypeBytes() {
    Object ref = operateType_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      operateType_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  /**
   * <code>string path = 2;</code>
   *
   * @return The path.
   */
  @Override
  public String getPath() {
    Object ref = path_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs =
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      path_ = s;
      return s;
    }
  }

  /**
   * <code>string path = 2;</code>
   *
   * @return The bytes for path.
   */
  @Override
  public com.google.protobuf.ByteString
  getPathBytes() {
    Object ref = path_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      path_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  /**
   * <code>int64 txId = 3;</code>
   *
   * @return The txId.
   */
  @Override
  public long getTxId() {
    return txId_;
  }

  /**
   * <code>.ProtoFileInfo fileInfo = 4;</code>
   *
   * @return Whether the fileInfo field is set.
   */
  @Override
  public boolean hasFileInfo() {
    return fileInfo_ != null;
  }

  /**
   * <code>.ProtoFileInfo fileInfo = 4;</code>
   *
   * @return The fileInfo.
   */
  @Override
  public ProtoFileInfo getFileInfo() {
    return fileInfo_ == null ? ProtoFileInfo.getDefaultInstance() : fileInfo_;
  }

  /**
   * <code>.ProtoFileInfo fileInfo = 4;</code>
   */
  @Override
  public ProtoFileInfoOrBuilder getFileInfoOrBuilder() {
    return fileInfo_ == null ? ProtoFileInfo.getDefaultInstance() : fileInfo_;
  }

  @Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) {
      return true;
    }
    if (isInitialized == 0) {
      return false;
    }

    memoizedIsInitialized = 1;
    return true;
  }

  @Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
      throws java.io.IOException {
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(operateType_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, operateType_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(path_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, path_);
    }
    if (txId_ != 0L) {
      output.writeInt64(3, txId_);
    }
    if (fileInfo_ != null) {
      output.writeMessage(4, getFileInfo());
    }
    getUnknownFields().writeTo(output);
  }

  @Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) {
      return size;
    }

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(operateType_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, operateType_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(path_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, path_);
    }
    if (txId_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(3, txId_);
    }
    if (fileInfo_ != null) {
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(4, getFileInfo());
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof OperateLog)) {
      return super.equals(obj);
    }
    OperateLog other = (OperateLog) obj;

    if (!getOperateType()
        .equals(other.getOperateType())) {
      return false;
    }
    if (!getPath()
        .equals(other.getPath())) {
      return false;
    }
    if (getTxId()
        != other.getTxId()) {
      return false;
    }
    if (hasFileInfo() != other.hasFileInfo()) {
      return false;
    }
    if (hasFileInfo()) {
      if (!getFileInfo()
          .equals(other.getFileInfo())) {
        return false;
      }
    }
    if (!getUnknownFields().equals(other.getUnknownFields())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + OPERATETYPE_FIELD_NUMBER;
    hash = (53 * hash) + getOperateType().hashCode();
    hash = (37 * hash) + PATH_FIELD_NUMBER;
    hash = (53 * hash) + getPath().hashCode();
    hash = (37 * hash) + TXID_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getTxId());
    if (hasFileInfo()) {
      hash = (37 * hash) + FILEINFO_FIELD_NUMBER;
      hash = (53 * hash) + getFileInfo().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  @Override
  public Builder newBuilderForType() {
    return newBuilder();
  }

  @Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @Override
  protected Builder newBuilderForType(
      BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }

  @Override
  public com.google.protobuf.Parser<OperateLog> getParserForType() {
    return PARSER;
  }

  @Override
  public OperateLog getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

  /**
   * Protobuf type {@code OperateLog}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:OperateLog)
      OperateLogOrBuilder {

    private int bitField0_;
    private Object operateType_ = "";
    private Object path_ = "";
    private long txId_;
    private ProtoFileInfo fileInfo_;
    private com.google.protobuf.SingleFieldBuilderV3<
        ProtoFileInfo, ProtoFileInfo.Builder, ProtoFileInfoOrBuilder> fileInfoBuilder_;

    // Construct using com.rothsCode.litehdfs.common.protoc.OperateLog.newBuilder()
    private Builder() {

    }

    private Builder(
        BuilderParent parent) {
      super(parent);

    }

    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return Namenode.internal_static_OperateLog_descriptor;
    }

    @Override
    protected FieldAccessorTable
    internalGetFieldAccessorTable() {
      return Namenode.internal_static_OperateLog_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              OperateLog.class, Builder.class);
    }

    @Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      operateType_ = "";
      path_ = "";
      txId_ = 0L;
      fileInfo_ = null;
      if (fileInfoBuilder_ != null) {
        fileInfoBuilder_.dispose();
        fileInfoBuilder_ = null;
      }
      return this;
    }

    @Override
    public com.google.protobuf.Descriptors.Descriptor
    getDescriptorForType() {
      return Namenode.internal_static_OperateLog_descriptor;
    }

    @Override
    public OperateLog getDefaultInstanceForType() {
      return OperateLog.getDefaultInstance();
    }

    @Override
    public OperateLog build() {
      OperateLog result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @Override
    public OperateLog buildPartial() {
      OperateLog result = new OperateLog(this);
      if (bitField0_ != 0) {
        buildPartial0(result);
      }
      onBuilt();
      return result;
    }

    private void buildPartial0(OperateLog result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.operateType_ = operateType_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.path_ = path_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.txId_ = txId_;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.fileInfo_ = fileInfoBuilder_ == null
            ? fileInfo_
            : fileInfoBuilder_.build();
      }
    }

    @Override
    public Builder clone() {
      return super.clone();
    }

    @Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.setField(field, value);
    }

    @Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }

    @Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }

    @Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return super.setRepeatedField(field, index, value);
    }

    @Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.addRepeatedField(field, value);
    }

    @Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof OperateLog) {
        return mergeFrom((OperateLog) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(OperateLog other) {
      if (other == OperateLog.getDefaultInstance()) {
        return this;
      }
      if (!other.getOperateType().isEmpty()) {
        operateType_ = other.operateType_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (!other.getPath().isEmpty()) {
        path_ = other.path_;
        bitField0_ |= 0x00000002;
        onChanged();
      }
      if (other.getTxId() != 0L) {
        setTxId(other.getTxId());
      }
      if (other.hasFileInfo()) {
        mergeFileInfo(other.getFileInfo());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @Override
    public final boolean isInitialized() {
      return true;
    }

    @Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              operateType_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              path_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 24: {
              txId_ = input.readInt64();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
            case 34: {
              input.readMessage(
                  getFileInfoFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000008;
              break;
            } // case 34
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }

    /**
     * <code>string operateType = 1;</code>
     *
     * @return The operateType.
     */
    public String getOperateType() {
      Object ref = operateType_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        operateType_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }

    /**
     * <code>string operateType = 1;</code>
     *
     * @param value The operateType to set.
     * @return This builder for chaining.
     */
    public Builder setOperateType(
        String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      operateType_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    /**
     * <code>string operateType = 1;</code>
     *
     * @return The bytes for operateType.
     */
    public com.google.protobuf.ByteString
    getOperateTypeBytes() {
      Object ref = operateType_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        operateType_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    /**
     * <code>string operateType = 1;</code>
     *
     * @param value The bytes for operateType to set.
     * @return This builder for chaining.
     */
    public Builder setOperateTypeBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);
      operateType_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    /**
     * <code>string operateType = 1;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearOperateType() {
      operateType_ = getDefaultInstance().getOperateType();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }

    /**
     * <code>string path = 2;</code>
     *
     * @return The path.
     */
    public String getPath() {
      Object ref = path_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        path_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }

    /**
     * <code>string path = 2;</code>
     *
     * @param value The path to set.
     * @return This builder for chaining.
     */
    public Builder setPath(
        String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      path_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    /**
     * <code>string path = 2;</code>
     *
     * @return The bytes for path.
     */
    public com.google.protobuf.ByteString
    getPathBytes() {
      Object ref = path_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        path_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    /**
     * <code>string path = 2;</code>
     *
     * @param value The bytes for path to set.
     * @return This builder for chaining.
     */
    public Builder setPathBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);
      path_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    /**
     * <code>string path = 2;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearPath() {
      path_ = getDefaultInstance().getPath();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }

    /**
     * <code>int64 txId = 3;</code>
     *
     * @return The txId.
     */
    @Override
    public long getTxId() {
      return txId_;
    }

    /**
     * <code>int64 txId = 3;</code>
     *
     * @param value The txId to set.
     * @return This builder for chaining.
     */
    public Builder setTxId(long value) {

      txId_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }

    /**
     * <code>int64 txId = 3;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearTxId() {
      bitField0_ = (bitField0_ & ~0x00000004);
      txId_ = 0L;
      onChanged();
      return this;
    }

    /**
     * <code>.ProtoFileInfo fileInfo = 4;</code>
     *
     * @return Whether the fileInfo field is set.
     */
    public boolean hasFileInfo() {
      return ((bitField0_ & 0x00000008) != 0);
    }

    /**
     * <code>.ProtoFileInfo fileInfo = 4;</code>
     *
     * @return The fileInfo.
     */
    public ProtoFileInfo getFileInfo() {
      if (fileInfoBuilder_ == null) {
        return fileInfo_ == null ? ProtoFileInfo.getDefaultInstance() : fileInfo_;
      } else {
        return fileInfoBuilder_.getMessage();
      }
    }

    /**
     * <code>.ProtoFileInfo fileInfo = 4;</code>
     */
    public Builder setFileInfo(ProtoFileInfo value) {
      if (fileInfoBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        fileInfo_ = value;
      } else {
        fileInfoBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }

    /**
     * <code>.ProtoFileInfo fileInfo = 4;</code>
     */
    public Builder setFileInfo(
        ProtoFileInfo.Builder builderForValue) {
      if (fileInfoBuilder_ == null) {
        fileInfo_ = builderForValue.build();
      } else {
        fileInfoBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }

    /**
     * <code>.ProtoFileInfo fileInfo = 4;</code>
     */
    public Builder mergeFileInfo(ProtoFileInfo value) {
      if (fileInfoBuilder_ == null) {
        if (((bitField0_ & 0x00000008) != 0) &&
            fileInfo_ != null &&
            fileInfo_ != ProtoFileInfo.getDefaultInstance()) {
          getFileInfoBuilder().mergeFrom(value);
        } else {
          fileInfo_ = value;
        }
      } else {
        fileInfoBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }

    /**
     * <code>.ProtoFileInfo fileInfo = 4;</code>
     */
    public Builder clearFileInfo() {
      bitField0_ = (bitField0_ & ~0x00000008);
      fileInfo_ = null;
      if (fileInfoBuilder_ != null) {
        fileInfoBuilder_.dispose();
        fileInfoBuilder_ = null;
      }
      onChanged();
      return this;
    }

    /**
     * <code>.ProtoFileInfo fileInfo = 4;</code>
     */
    public ProtoFileInfo.Builder getFileInfoBuilder() {
      bitField0_ |= 0x00000008;
      onChanged();
      return getFileInfoFieldBuilder().getBuilder();
    }

    /**
     * <code>.ProtoFileInfo fileInfo = 4;</code>
     */
    public ProtoFileInfoOrBuilder getFileInfoOrBuilder() {
      if (fileInfoBuilder_ != null) {
        return fileInfoBuilder_.getMessageOrBuilder();
      } else {
        return fileInfo_ == null ?
            ProtoFileInfo.getDefaultInstance() : fileInfo_;
      }
    }

    /**
     * <code>.ProtoFileInfo fileInfo = 4;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        ProtoFileInfo, ProtoFileInfo.Builder, ProtoFileInfoOrBuilder>
    getFileInfoFieldBuilder() {
      if (fileInfoBuilder_ == null) {
        fileInfoBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            ProtoFileInfo, ProtoFileInfo.Builder, ProtoFileInfoOrBuilder>(
            getFileInfo(),
            getParentForChildren(),
            isClean());
        fileInfo_ = null;
      }
      return fileInfoBuilder_;
    }

    @Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:OperateLog)
  }

}

