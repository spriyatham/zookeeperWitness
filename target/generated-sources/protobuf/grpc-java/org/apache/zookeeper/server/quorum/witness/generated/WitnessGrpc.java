package org.apache.zookeeper.server.quorum.witness.generated;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.23.0)",
    comments = "Source: witness_v1.proto")
public final class WitnessGrpc {

  private WitnessGrpc() {}

  public static final String SERVICE_NAME = "Witness";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.apache.zookeeper.server.quorum.witness.generated.ReadRequest,
      org.apache.zookeeper.server.quorum.witness.generated.ReadResponse> getReadMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "read",
      requestType = org.apache.zookeeper.server.quorum.witness.generated.ReadRequest.class,
      responseType = org.apache.zookeeper.server.quorum.witness.generated.ReadResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.apache.zookeeper.server.quorum.witness.generated.ReadRequest,
      org.apache.zookeeper.server.quorum.witness.generated.ReadResponse> getReadMethod() {
    io.grpc.MethodDescriptor<org.apache.zookeeper.server.quorum.witness.generated.ReadRequest, org.apache.zookeeper.server.quorum.witness.generated.ReadResponse> getReadMethod;
    if ((getReadMethod = WitnessGrpc.getReadMethod) == null) {
      synchronized (WitnessGrpc.class) {
        if ((getReadMethod = WitnessGrpc.getReadMethod) == null) {
          WitnessGrpc.getReadMethod = getReadMethod =
              io.grpc.MethodDescriptor.<org.apache.zookeeper.server.quorum.witness.generated.ReadRequest, org.apache.zookeeper.server.quorum.witness.generated.ReadResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "read"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.zookeeper.server.quorum.witness.generated.ReadRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.zookeeper.server.quorum.witness.generated.ReadResponse.getDefaultInstance()))
              .setSchemaDescriptor(new WitnessMethodDescriptorSupplier("read"))
              .build();
        }
      }
    }
    return getReadMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.apache.zookeeper.server.quorum.witness.generated.WriteRequest,
      org.apache.zookeeper.server.quorum.witness.generated.WriteResponse> getWriteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "write",
      requestType = org.apache.zookeeper.server.quorum.witness.generated.WriteRequest.class,
      responseType = org.apache.zookeeper.server.quorum.witness.generated.WriteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.apache.zookeeper.server.quorum.witness.generated.WriteRequest,
      org.apache.zookeeper.server.quorum.witness.generated.WriteResponse> getWriteMethod() {
    io.grpc.MethodDescriptor<org.apache.zookeeper.server.quorum.witness.generated.WriteRequest, org.apache.zookeeper.server.quorum.witness.generated.WriteResponse> getWriteMethod;
    if ((getWriteMethod = WitnessGrpc.getWriteMethod) == null) {
      synchronized (WitnessGrpc.class) {
        if ((getWriteMethod = WitnessGrpc.getWriteMethod) == null) {
          WitnessGrpc.getWriteMethod = getWriteMethod =
              io.grpc.MethodDescriptor.<org.apache.zookeeper.server.quorum.witness.generated.WriteRequest, org.apache.zookeeper.server.quorum.witness.generated.WriteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "write"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.zookeeper.server.quorum.witness.generated.WriteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.apache.zookeeper.server.quorum.witness.generated.WriteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new WitnessMethodDescriptorSupplier("write"))
              .build();
        }
      }
    }
    return getWriteMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static WitnessStub newStub(io.grpc.Channel channel) {
    return new WitnessStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static WitnessBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new WitnessBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static WitnessFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new WitnessFutureStub(channel);
  }

  /**
   */
  public static abstract class WitnessImplBase implements io.grpc.BindableService {

    /**
     */
    public void read(org.apache.zookeeper.server.quorum.witness.generated.ReadRequest request,
        io.grpc.stub.StreamObserver<org.apache.zookeeper.server.quorum.witness.generated.ReadResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getReadMethod(), responseObserver);
    }

    /**
     */
    public void write(org.apache.zookeeper.server.quorum.witness.generated.WriteRequest request,
        io.grpc.stub.StreamObserver<org.apache.zookeeper.server.quorum.witness.generated.WriteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getWriteMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getReadMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.apache.zookeeper.server.quorum.witness.generated.ReadRequest,
                org.apache.zookeeper.server.quorum.witness.generated.ReadResponse>(
                  this, METHODID_READ)))
          .addMethod(
            getWriteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.apache.zookeeper.server.quorum.witness.generated.WriteRequest,
                org.apache.zookeeper.server.quorum.witness.generated.WriteResponse>(
                  this, METHODID_WRITE)))
          .build();
    }
  }

  /**
   */
  public static final class WitnessStub extends io.grpc.stub.AbstractStub<WitnessStub> {
    private WitnessStub(io.grpc.Channel channel) {
      super(channel);
    }

    private WitnessStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WitnessStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new WitnessStub(channel, callOptions);
    }

    /**
     */
    public void read(org.apache.zookeeper.server.quorum.witness.generated.ReadRequest request,
        io.grpc.stub.StreamObserver<org.apache.zookeeper.server.quorum.witness.generated.ReadResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getReadMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void write(org.apache.zookeeper.server.quorum.witness.generated.WriteRequest request,
        io.grpc.stub.StreamObserver<org.apache.zookeeper.server.quorum.witness.generated.WriteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getWriteMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class WitnessBlockingStub extends io.grpc.stub.AbstractStub<WitnessBlockingStub> {
    private WitnessBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private WitnessBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WitnessBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new WitnessBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.apache.zookeeper.server.quorum.witness.generated.ReadResponse read(org.apache.zookeeper.server.quorum.witness.generated.ReadRequest request) {
      return blockingUnaryCall(
          getChannel(), getReadMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.apache.zookeeper.server.quorum.witness.generated.WriteResponse write(org.apache.zookeeper.server.quorum.witness.generated.WriteRequest request) {
      return blockingUnaryCall(
          getChannel(), getWriteMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class WitnessFutureStub extends io.grpc.stub.AbstractStub<WitnessFutureStub> {
    private WitnessFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private WitnessFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WitnessFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new WitnessFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.apache.zookeeper.server.quorum.witness.generated.ReadResponse> read(
        org.apache.zookeeper.server.quorum.witness.generated.ReadRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getReadMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.apache.zookeeper.server.quorum.witness.generated.WriteResponse> write(
        org.apache.zookeeper.server.quorum.witness.generated.WriteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getWriteMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_READ = 0;
  private static final int METHODID_WRITE = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final WitnessImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(WitnessImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_READ:
          serviceImpl.read((org.apache.zookeeper.server.quorum.witness.generated.ReadRequest) request,
              (io.grpc.stub.StreamObserver<org.apache.zookeeper.server.quorum.witness.generated.ReadResponse>) responseObserver);
          break;
        case METHODID_WRITE:
          serviceImpl.write((org.apache.zookeeper.server.quorum.witness.generated.WriteRequest) request,
              (io.grpc.stub.StreamObserver<org.apache.zookeeper.server.quorum.witness.generated.WriteResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class WitnessBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    WitnessBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.apache.zookeeper.server.quorum.witness.generated.WitnessRegister.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Witness");
    }
  }

  private static final class WitnessFileDescriptorSupplier
      extends WitnessBaseDescriptorSupplier {
    WitnessFileDescriptorSupplier() {}
  }

  private static final class WitnessMethodDescriptorSupplier
      extends WitnessBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    WitnessMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (WitnessGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new WitnessFileDescriptorSupplier())
              .addMethod(getReadMethod())
              .addMethod(getWriteMethod())
              .build();
        }
      }
    }
    return result;
  }
}
