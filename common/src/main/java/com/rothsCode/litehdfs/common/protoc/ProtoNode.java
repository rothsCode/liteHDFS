// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/resources/protoc/namenode.proto

package com.rothsCode.litehdfs.common.protoc;

/**
 * Protobuf type {@code ProtoNode}
 */
public final class ProtoNode extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:ProtoNode)
    ProtoNodeOrBuilder {

  public static final int PATH_FIELD_NUMBER = 1;
  public static final int ISFILENODE_FIELD_NUMBER = 2;
  public static final int CHILDNODES_FIELD_NUMBER = 3;
  public static final int FILEINFO_FIELD_NUMBER = 4;
  private static final long serialVersionUID = 0L;
  // @@protoc_insertion_point(class_scope:ProtoNode)
  private static final ProtoNode DEFAULT_INSTANCE;
  private static final com.google.protobuf.Parser<ProtoNode>
      PARSER = new com.google.protobuf.AbstractParser<ProtoNode>() {
    @Override
    public ProtoNode parsePartialFrom(
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
    DEFAULT_INSTANCE = new ProtoNode();
  }

  @SuppressWarnings("serial")
  private volatile Object path_ = "";
  private boolean isFileNode_ = false;
  @SuppressWarnings("serial")
  private com.google.protobuf.MapField<
      String, ProtoNode> childNodes_;
  private ProtoFileInfo fileInfo_;
  private byte memoizedIsInitialized = -1;

  // Use ProtoNode.newBuilder() to construct.
  private ProtoNode(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private ProtoNode() {
    path_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
  getDescriptor() {
    return Namenode.internal_static_ProtoNode_descriptor;
  }

  public static ProtoNode parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static ProtoNode parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static ProtoNode parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static ProtoNode parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static ProtoNode parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static ProtoNode parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static ProtoNode parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }

  public static ProtoNode parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static ProtoNode parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static ProtoNode parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }

  public static ProtoNode parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }

  public static ProtoNode parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(ProtoNode prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public static ProtoNode getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  public static com.google.protobuf.Parser<ProtoNode> parser() {
    return PARSER;
  }

  @Override
  @SuppressWarnings({"unused"})
  protected Object newInstance(
      UnusedPrivateParameter unused) {
    return new ProtoNode();
  }

  @Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }

  @SuppressWarnings({"rawtypes"})
  @Override
  protected com.google.protobuf.MapField internalGetMapField(
      int number) {
    switch (number) {
      case 3:
        return internalGetChildNodes();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }

  @Override
  protected FieldAccessorTable
  internalGetFieldAccessorTable() {
    return Namenode.internal_static_ProtoNode_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            ProtoNode.class, Builder.class);
  }

  /**
   * <code>string path = 1;</code>
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
   * <code>string path = 1;</code>
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
   * <code>bool isFileNode = 2;</code>
   *
   * @return The isFileNode.
   */
  @Override
  public boolean getIsFileNode() {
    return isFileNode_;
  }

  private com.google.protobuf.MapField<String, ProtoNode>
  internalGetChildNodes() {
    if (childNodes_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          ChildNodesDefaultEntryHolder.defaultEntry);
    }
    return childNodes_;
  }

  public int getChildNodesCount() {
    return internalGetChildNodes().getMap().size();
  }

  /**
   * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
   */
  @Override
  public boolean containsChildNodes(
      String key) {
    if (key == null) {
      throw new NullPointerException("map key");
    }
    return internalGetChildNodes().getMap().containsKey(key);
  }

  /**
   * Use {@link #getChildNodesMap()} instead.
   */
  @Override
  @Deprecated
  public java.util.Map<String, ProtoNode> getChildNodes() {
    return getChildNodesMap();
  }

  /**
   * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
   */
  @Override
  public java.util.Map<String, ProtoNode> getChildNodesMap() {
    return internalGetChildNodes().getMap();
  }

  /**
   * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
   */
  @Override
  public /* nullable */
  ProtoNode getChildNodesOrDefault(
      String key,
      /* nullable */
      ProtoNode defaultValue) {
    if (key == null) {
      throw new NullPointerException("map key");
    }
    java.util.Map<String, ProtoNode> map =
        internalGetChildNodes().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }

  /**
   * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
   */
  @Override
  public ProtoNode getChildNodesOrThrow(
      String key) {
    if (key == null) {
      throw new NullPointerException("map key");
    }
    java.util.Map<String, ProtoNode> map =
        internalGetChildNodes().getMap();
    if (!map.containsKey(key)) {
      throw new IllegalArgumentException();
    }
    return map.get(key);
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(path_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, path_);
    }
    if (isFileNode_ != false) {
      output.writeBool(2, isFileNode_);
    }
    com.google.protobuf.GeneratedMessageV3
        .serializeStringMapTo(
            output,
            internalGetChildNodes(),
            ChildNodesDefaultEntryHolder.defaultEntry,
            3);
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(path_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, path_);
    }
    if (isFileNode_ != false) {
      size += com.google.protobuf.CodedOutputStream
          .computeBoolSize(2, isFileNode_);
    }
    for (java.util.Map.Entry<String, ProtoNode> entry
        : internalGetChildNodes().getMap().entrySet()) {
      com.google.protobuf.MapEntry<String, ProtoNode>
          childNodes__ = ChildNodesDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(3, childNodes__);
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
    if (!(obj instanceof ProtoNode)) {
      return super.equals(obj);
    }
    ProtoNode other = (ProtoNode) obj;

    if (!getPath()
        .equals(other.getPath())) {
      return false;
    }
    if (getIsFileNode()
        != other.getIsFileNode()) {
      return false;
    }
    if (!internalGetChildNodes().equals(
        other.internalGetChildNodes())) {
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
    hash = (37 * hash) + PATH_FIELD_NUMBER;
    hash = (53 * hash) + getPath().hashCode();
    hash = (37 * hash) + ISFILENODE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getIsFileNode());
    if (!internalGetChildNodes().getMap().isEmpty()) {
      hash = (37 * hash) + CHILDNODES_FIELD_NUMBER;
      hash = (53 * hash) + internalGetChildNodes().hashCode();
    }
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
  public com.google.protobuf.Parser<ProtoNode> getParserForType() {
    return PARSER;
  }

  @Override
  public ProtoNode getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

  private static final class ChildNodesDefaultEntryHolder {

    static final com.google.protobuf.MapEntry<
        String, ProtoNode> defaultEntry =
        com.google.protobuf.MapEntry
            .<String, ProtoNode>newDefaultInstance(
                Namenode.internal_static_ProtoNode_ChildNodesEntry_descriptor,
                com.google.protobuf.WireFormat.FieldType.STRING,
                "",
                com.google.protobuf.WireFormat.FieldType.MESSAGE,
                ProtoNode.getDefaultInstance());
  }

  /**
   * Protobuf type {@code ProtoNode}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ProtoNode)
      ProtoNodeOrBuilder {

    private int bitField0_;
    private Object path_ = "";
    private boolean isFileNode_;
    private com.google.protobuf.MapField<
        String, ProtoNode> childNodes_;
    private ProtoFileInfo fileInfo_;
    private com.google.protobuf.SingleFieldBuilderV3<
        ProtoFileInfo, ProtoFileInfo.Builder, ProtoFileInfoOrBuilder> fileInfoBuilder_;

    // Construct using com.rothsCode.litehdfs.common.protoc.ProtoNode.newBuilder()
    private Builder() {

    }

    private Builder(
        BuilderParent parent) {
      super(parent);

    }

    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
      return Namenode.internal_static_ProtoNode_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 3:
          return internalGetChildNodes();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(
        int number) {
      switch (number) {
        case 3:
          return internalGetMutableChildNodes();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }

    @Override
    protected FieldAccessorTable
    internalGetFieldAccessorTable() {
      return Namenode.internal_static_ProtoNode_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ProtoNode.class, Builder.class);
    }

    @Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      path_ = "";
      isFileNode_ = false;
      internalGetMutableChildNodes().clear();
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
      return Namenode.internal_static_ProtoNode_descriptor;
    }

    @Override
    public ProtoNode getDefaultInstanceForType() {
      return ProtoNode.getDefaultInstance();
    }

    @Override
    public ProtoNode build() {
      ProtoNode result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @Override
    public ProtoNode buildPartial() {
      ProtoNode result = new ProtoNode(this);
      if (bitField0_ != 0) {
        buildPartial0(result);
      }
      onBuilt();
      return result;
    }

    private void buildPartial0(ProtoNode result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.path_ = path_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.isFileNode_ = isFileNode_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.childNodes_ = internalGetChildNodes();
        result.childNodes_.makeImmutable();
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
      if (other instanceof ProtoNode) {
        return mergeFrom((ProtoNode) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(ProtoNode other) {
      if (other == ProtoNode.getDefaultInstance()) {
        return this;
      }
      if (!other.getPath().isEmpty()) {
        path_ = other.path_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (other.getIsFileNode() != false) {
        setIsFileNode(other.getIsFileNode());
      }
      internalGetMutableChildNodes().mergeFrom(
          other.internalGetChildNodes());
      bitField0_ |= 0x00000004;
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
              path_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              isFileNode_ = input.readBool();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            case 26: {
              com.google.protobuf.MapEntry<String, ProtoNode>
                  childNodes__ = input.readMessage(
                  ChildNodesDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              internalGetMutableChildNodes().getMutableMap().put(
                  childNodes__.getKey(), childNodes__.getValue());
              bitField0_ |= 0x00000004;
              break;
            } // case 26
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
     * <code>string path = 1;</code>
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
     * <code>string path = 1;</code>
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
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    /**
     * <code>string path = 1;</code>
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
     * <code>string path = 1;</code>
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
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    /**
     * <code>string path = 1;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearPath() {
      path_ = getDefaultInstance().getPath();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }

    /**
     * <code>bool isFileNode = 2;</code>
     *
     * @return The isFileNode.
     */
    @Override
    public boolean getIsFileNode() {
      return isFileNode_;
    }

    /**
     * <code>bool isFileNode = 2;</code>
     *
     * @param value The isFileNode to set.
     * @return This builder for chaining.
     */
    public Builder setIsFileNode(boolean value) {

      isFileNode_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    /**
     * <code>bool isFileNode = 2;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearIsFileNode() {
      bitField0_ = (bitField0_ & ~0x00000002);
      isFileNode_ = false;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<String, ProtoNode>
    internalGetChildNodes() {
      if (childNodes_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            ChildNodesDefaultEntryHolder.defaultEntry);
      }
      return childNodes_;
    }

    private com.google.protobuf.MapField<String, ProtoNode>
    internalGetMutableChildNodes() {
      if (childNodes_ == null) {
        childNodes_ = com.google.protobuf.MapField.newMapField(
            ChildNodesDefaultEntryHolder.defaultEntry);
      }
      if (!childNodes_.isMutable()) {
        childNodes_ = childNodes_.copy();
      }
      bitField0_ |= 0x00000004;
      onChanged();
      return childNodes_;
    }

    public int getChildNodesCount() {
      return internalGetChildNodes().getMap().size();
    }

    /**
     * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
     */
    @Override
    public boolean containsChildNodes(
        String key) {
      if (key == null) {
        throw new NullPointerException("map key");
      }
      return internalGetChildNodes().getMap().containsKey(key);
    }

    /**
     * Use {@link #getChildNodesMap()} instead.
     */
    @Override
    @Deprecated
    public java.util.Map<String, ProtoNode> getChildNodes() {
      return getChildNodesMap();
    }

    /**
     * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
     */
    @Override
    public java.util.Map<String, ProtoNode> getChildNodesMap() {
      return internalGetChildNodes().getMap();
    }

    /**
     * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
     */
    @Override
    public /* nullable */
    ProtoNode getChildNodesOrDefault(
        String key,
        /* nullable */
        ProtoNode defaultValue) {
      if (key == null) {
        throw new NullPointerException("map key");
      }
      java.util.Map<String, ProtoNode> map =
          internalGetChildNodes().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }

    /**
     * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
     */
    @Override
    public ProtoNode getChildNodesOrThrow(
        String key) {
      if (key == null) {
        throw new NullPointerException("map key");
      }
      java.util.Map<String, ProtoNode> map =
          internalGetChildNodes().getMap();
      if (!map.containsKey(key)) {
        throw new IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearChildNodes() {
      bitField0_ = (bitField0_ & ~0x00000004);
      internalGetMutableChildNodes().getMutableMap()
          .clear();
      return this;
    }

    /**
     * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
     */
    public Builder removeChildNodes(
        String key) {
      if (key == null) {
        throw new NullPointerException("map key");
      }
      internalGetMutableChildNodes().getMutableMap()
          .remove(key);
      return this;
    }

    /**
     * Use alternate mutation accessors instead.
     */
    @Deprecated
    public java.util.Map<String, ProtoNode>
    getMutableChildNodes() {
      bitField0_ |= 0x00000004;
      return internalGetMutableChildNodes().getMutableMap();
    }

    /**
     * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
     */
    public Builder putChildNodes(
        String key,
        ProtoNode value) {
      if (key == null) {
        throw new NullPointerException("map key");
      }
      if (value == null) {
        throw new NullPointerException("map value");
      }
      internalGetMutableChildNodes().getMutableMap()
          .put(key, value);
      bitField0_ |= 0x00000004;
      return this;
    }

    /**
     * <code>map&lt;string, .ProtoNode&gt; childNodes = 3;</code>
     */
    public Builder putAllChildNodes(
        java.util.Map<String, ProtoNode> values) {
      internalGetMutableChildNodes().getMutableMap()
          .putAll(values);
      bitField0_ |= 0x00000004;
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

    // @@protoc_insertion_point(builder_scope:ProtoNode)
  }

}

