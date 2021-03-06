// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: witness_v1.proto

package org.apache.zookeeper.server.quorum.witness.generated;

/**
 * Protobuf type {@code WriteRequest}
 */
public  final class WriteRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:WriteRequest)
    WriteRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use WriteRequest.newBuilder() to construct.
  private WriteRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private WriteRequest() {
    metadata_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new WriteRequest();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private WriteRequest(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 8: {

            version_ = input.readUInt64();
            break;
          }
          case 18: {

            metadata_ = input.readBytes();
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.apache.zookeeper.server.quorum.witness.generated.WitnessRegister.internal_static_WriteRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.apache.zookeeper.server.quorum.witness.generated.WitnessRegister.internal_static_WriteRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.apache.zookeeper.server.quorum.witness.generated.WriteRequest.class, org.apache.zookeeper.server.quorum.witness.generated.WriteRequest.Builder.class);
  }

  public static final int VERSION_FIELD_NUMBER = 1;
  private long version_;
  /**
   * <code>uint64 version = 1;</code>
   */
  public long getVersion() {
    return version_;
  }

  public static final int METADATA_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString metadata_;
  /**
   * <code>bytes metadata = 2;</code>
   */
  public com.google.protobuf.ByteString getMetadata() {
    return metadata_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (version_ != 0L) {
      output.writeUInt64(1, version_);
    }
    if (!metadata_.isEmpty()) {
      output.writeBytes(2, metadata_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (version_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeUInt64Size(1, version_);
    }
    if (!metadata_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(2, metadata_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof org.apache.zookeeper.server.quorum.witness.generated.WriteRequest)) {
      return super.equals(obj);
    }
    org.apache.zookeeper.server.quorum.witness.generated.WriteRequest other = (org.apache.zookeeper.server.quorum.witness.generated.WriteRequest) obj;

    if (getVersion()
        != other.getVersion()) return false;
    if (!getMetadata()
        .equals(other.getMetadata())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getVersion());
    hash = (37 * hash) + METADATA_FIELD_NUMBER;
    hash = (53 * hash) + getMetadata().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(org.apache.zookeeper.server.quorum.witness.generated.WriteRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code WriteRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:WriteRequest)
      org.apache.zookeeper.server.quorum.witness.generated.WriteRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.apache.zookeeper.server.quorum.witness.generated.WitnessRegister.internal_static_WriteRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.apache.zookeeper.server.quorum.witness.generated.WitnessRegister.internal_static_WriteRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.apache.zookeeper.server.quorum.witness.generated.WriteRequest.class, org.apache.zookeeper.server.quorum.witness.generated.WriteRequest.Builder.class);
    }

    // Construct using org.apache.zookeeper.server.quorum.witness.generated.WriteRequest.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      version_ = 0L;

      metadata_ = com.google.protobuf.ByteString.EMPTY;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.apache.zookeeper.server.quorum.witness.generated.WitnessRegister.internal_static_WriteRequest_descriptor;
    }

    @java.lang.Override
    public org.apache.zookeeper.server.quorum.witness.generated.WriteRequest getDefaultInstanceForType() {
      return org.apache.zookeeper.server.quorum.witness.generated.WriteRequest.getDefaultInstance();
    }

    @java.lang.Override
    public org.apache.zookeeper.server.quorum.witness.generated.WriteRequest build() {
      org.apache.zookeeper.server.quorum.witness.generated.WriteRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.apache.zookeeper.server.quorum.witness.generated.WriteRequest buildPartial() {
      org.apache.zookeeper.server.quorum.witness.generated.WriteRequest result = new org.apache.zookeeper.server.quorum.witness.generated.WriteRequest(this);
      result.version_ = version_;
      result.metadata_ = metadata_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof org.apache.zookeeper.server.quorum.witness.generated.WriteRequest) {
        return mergeFrom((org.apache.zookeeper.server.quorum.witness.generated.WriteRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.apache.zookeeper.server.quorum.witness.generated.WriteRequest other) {
      if (other == org.apache.zookeeper.server.quorum.witness.generated.WriteRequest.getDefaultInstance()) return this;
      if (other.getVersion() != 0L) {
        setVersion(other.getVersion());
      }
      if (other.getMetadata() != com.google.protobuf.ByteString.EMPTY) {
        setMetadata(other.getMetadata());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.apache.zookeeper.server.quorum.witness.generated.WriteRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.apache.zookeeper.server.quorum.witness.generated.WriteRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private long version_ ;
    /**
     * <code>uint64 version = 1;</code>
     */
    public long getVersion() {
      return version_;
    }
    /**
     * <code>uint64 version = 1;</code>
     */
    public Builder setVersion(long value) {
      
      version_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>uint64 version = 1;</code>
     */
    public Builder clearVersion() {
      
      version_ = 0L;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString metadata_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes metadata = 2;</code>
     */
    public com.google.protobuf.ByteString getMetadata() {
      return metadata_;
    }
    /**
     * <code>bytes metadata = 2;</code>
     */
    public Builder setMetadata(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      metadata_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bytes metadata = 2;</code>
     */
    public Builder clearMetadata() {
      
      metadata_ = getDefaultInstance().getMetadata();
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:WriteRequest)
  }

  // @@protoc_insertion_point(class_scope:WriteRequest)
  private static final org.apache.zookeeper.server.quorum.witness.generated.WriteRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.apache.zookeeper.server.quorum.witness.generated.WriteRequest();
  }

  public static org.apache.zookeeper.server.quorum.witness.generated.WriteRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<WriteRequest>
      PARSER = new com.google.protobuf.AbstractParser<WriteRequest>() {
    @java.lang.Override
    public WriteRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new WriteRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<WriteRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<WriteRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.apache.zookeeper.server.quorum.witness.generated.WriteRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

