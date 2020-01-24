package ai.libs.hasco.pcsbasedoptimization.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logging.LoggerUtil;

/**
 */
@javax.annotation.Generated(value = "by gRPC proto compiler (version 1.23.0)", comments = "Source: PCSBasedComponentParameter.proto")
public final class PCSBasedOptimizerServiceGrpc {

	private static final Logger LOGGER = LoggerFactory.getLogger(PCSBasedOptimizerServiceGrpc.class);

	private PCSBasedOptimizerServiceGrpc() {
	}

	public static final String SERVICE_NAME = "pcsbasedoptimization.PCSBasedOptimizerService";

	// Static method descriptors that strictly reflect the proto.
	private static volatile io.grpc.MethodDescriptor<ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto, ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto> getEvaluateMethod;

	@io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
			+ "Evaluate", requestType = ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto.class, responseType = ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
	public static io.grpc.MethodDescriptor<ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto, ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto> getEvaluateMethod() {
		io.grpc.MethodDescriptor<ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto, ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto> getEvaluateMethod;
		if ((getEvaluateMethod = PCSBasedOptimizerServiceGrpc.getEvaluateMethod) == null) {
			synchronized (PCSBasedOptimizerServiceGrpc.class) {
				if ((getEvaluateMethod = PCSBasedOptimizerServiceGrpc.getEvaluateMethod) == null) {
					PCSBasedOptimizerServiceGrpc.getEvaluateMethod = getEvaluateMethod = io.grpc.MethodDescriptor.<ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto, ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto>newBuilder()
							.setType(io.grpc.MethodDescriptor.MethodType.UNARY).setFullMethodName(generateFullMethodName(SERVICE_NAME, "Evaluate")).setSampledToLocalTracing(true)
							.setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto.getDefaultInstance()))
							.setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto.getDefaultInstance()))
							.setSchemaDescriptor(new PCSBasedOptimizerServiceMethodDescriptorSupplier("Evaluate")).build();
				}
			}
		}
		return getEvaluateMethod;
	}

	/**
	 * Creates a new async stub that supports all call types for the service
	 */
	public static PCSBasedOptimizerServiceStub newStub(final io.grpc.Channel channel) {
		return new PCSBasedOptimizerServiceStub(channel);
	}

	/**
	 * Creates a new blocking-style stub that supports unary and streaming output calls on the service
	 */
	public static PCSBasedOptimizerServiceBlockingStub newBlockingStub(final io.grpc.Channel channel) {
		return new PCSBasedOptimizerServiceBlockingStub(channel);
	}

	/**
	 * Creates a new ListenableFuture-style stub that supports unary calls on the service
	 */
	public static PCSBasedOptimizerServiceFutureStub newFutureStub(final io.grpc.Channel channel) {
		return new PCSBasedOptimizerServiceFutureStub(channel);
	}

	/**
	 */
	public static abstract class PCSBasedOptimizerServiceImplBase implements io.grpc.BindableService {

		/**
		 */
		public void evaluate(final ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto request, final io.grpc.stub.StreamObserver<ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto> responseObserver)
				throws InterruptedException, ObjectEvaluationFailedException {
			asyncUnimplementedUnaryCall(getEvaluateMethod(), responseObserver);
		}

		@java.lang.Override
		public final io.grpc.ServerServiceDefinition bindService() {
			return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor()).addMethod(getEvaluateMethod(),
					asyncUnaryCall(new MethodHandlers<ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto, ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto>(this, METHODID_EVALUATE))).build();
		}
	}

	/**
	 */
	public static final class PCSBasedOptimizerServiceStub extends io.grpc.stub.AbstractStub<PCSBasedOptimizerServiceStub> {
		private PCSBasedOptimizerServiceStub(final io.grpc.Channel channel) {
			super(channel);
		}

		private PCSBasedOptimizerServiceStub(final io.grpc.Channel channel, final io.grpc.CallOptions callOptions) {
			super(channel, callOptions);
		}

		@java.lang.Override
		protected PCSBasedOptimizerServiceStub build(final io.grpc.Channel channel, final io.grpc.CallOptions callOptions) {
			return new PCSBasedOptimizerServiceStub(channel, callOptions);
		}

		/**
		 */
		public void evaluate(final ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto request, final io.grpc.stub.StreamObserver<ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto> responseObserver) {
			asyncUnaryCall(this.getChannel().newCall(getEvaluateMethod(), this.getCallOptions()), request, responseObserver);
		}
	}

	/**
	 */
	public static final class PCSBasedOptimizerServiceBlockingStub extends io.grpc.stub.AbstractStub<PCSBasedOptimizerServiceBlockingStub> {
		private PCSBasedOptimizerServiceBlockingStub(final io.grpc.Channel channel) {
			super(channel);
		}

		private PCSBasedOptimizerServiceBlockingStub(final io.grpc.Channel channel, final io.grpc.CallOptions callOptions) {
			super(channel, callOptions);
		}

		@java.lang.Override
		protected PCSBasedOptimizerServiceBlockingStub build(final io.grpc.Channel channel, final io.grpc.CallOptions callOptions) {
			return new PCSBasedOptimizerServiceBlockingStub(channel, callOptions);
		}

		/**
		 */
		public ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto evaluate(final ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto request) {
			return blockingUnaryCall(this.getChannel(), getEvaluateMethod(), this.getCallOptions(), request);
		}
	}

	/**
	 */
	public static final class PCSBasedOptimizerServiceFutureStub extends io.grpc.stub.AbstractStub<PCSBasedOptimizerServiceFutureStub> {
		private PCSBasedOptimizerServiceFutureStub(final io.grpc.Channel channel) {
			super(channel);
		}

		private PCSBasedOptimizerServiceFutureStub(final io.grpc.Channel channel, final io.grpc.CallOptions callOptions) {
			super(channel, callOptions);
		}

		@java.lang.Override
		protected PCSBasedOptimizerServiceFutureStub build(final io.grpc.Channel channel, final io.grpc.CallOptions callOptions) {
			return new PCSBasedOptimizerServiceFutureStub(channel, callOptions);
		}

		/**
		 */
		public com.google.common.util.concurrent.ListenableFuture<ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto> evaluate(final ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto request) {
			return futureUnaryCall(this.getChannel().newCall(getEvaluateMethod(), this.getCallOptions()), request);
		}
	}

	private static final int METHODID_EVALUATE = 0;

	private static final class MethodHandlers<Req, Resp> implements io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>, io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>, io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
	io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
		private final PCSBasedOptimizerServiceImplBase serviceImpl;
		private final int methodId;

		MethodHandlers(final PCSBasedOptimizerServiceImplBase serviceImpl, final int methodId) {
			this.serviceImpl = serviceImpl;
			this.methodId = methodId;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("unchecked")
		public void invoke(final Req request, final io.grpc.stub.StreamObserver<Resp> responseObserver) {
			switch (this.methodId) {
			case METHODID_EVALUATE:
				try {
					this.serviceImpl.evaluate((ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentProto) request,
							(io.grpc.stub.StreamObserver<ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedEvaluationResponseProto>) responseObserver);
				} catch (ObjectEvaluationFailedException e) {
					LOGGER.error(LoggerUtil.getExceptionInfo(e));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				break;
			default:
				throw new AssertionError();
			}
		}

		@java.lang.Override
		public io.grpc.stub.StreamObserver<Req> invoke(final io.grpc.stub.StreamObserver<Resp> responseObserver) {
			switch (this.methodId) {
			default:
				throw new AssertionError();
			}
		}
	}

	private static abstract class PCSBasedOptimizerServiceBaseDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
		PCSBasedOptimizerServiceBaseDescriptorSupplier() {
		}

		@java.lang.Override
		public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
			return ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedComponentParameter.getDescriptor();
		}

		@java.lang.Override
		public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
			return this.getFileDescriptor().findServiceByName("PCSBasedOptimizerService");
		}
	}

	private static final class PCSBasedOptimizerServiceFileDescriptorSupplier extends PCSBasedOptimizerServiceBaseDescriptorSupplier {
		PCSBasedOptimizerServiceFileDescriptorSupplier() {
		}
	}

	private static final class PCSBasedOptimizerServiceMethodDescriptorSupplier extends PCSBasedOptimizerServiceBaseDescriptorSupplier implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
		private final String methodName;

		PCSBasedOptimizerServiceMethodDescriptorSupplier(final String methodName) {
			this.methodName = methodName;
		}

		@java.lang.Override
		public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
			return this.getServiceDescriptor().findMethodByName(this.methodName);
		}
	}

	private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

	public static io.grpc.ServiceDescriptor getServiceDescriptor() {
		io.grpc.ServiceDescriptor result = serviceDescriptor;
		if (result == null) {
			synchronized (PCSBasedOptimizerServiceGrpc.class) {
				result = serviceDescriptor;
				if (result == null) {
					serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME).setSchemaDescriptor(new PCSBasedOptimizerServiceFileDescriptorSupplier()).addMethod(getEvaluateMethod()).build();
				}
			}
		}
		return result;
	}
}
