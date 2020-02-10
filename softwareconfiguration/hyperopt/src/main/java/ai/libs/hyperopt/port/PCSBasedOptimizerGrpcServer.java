package ai.libs.hyperopt.port;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedOptimizerService;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hyperopt.event.PCSBasedOptimizerServiceImpl;
import ai.libs.hyperopt.example.automl.weka.ComponentInstanceEvaluator;
import ai.libs.hyperopt.optimizer.pcs.PCSBasedOptimizerConfig;
import ai.libs.hyperopt.optimizer.pcs.PCSBasedOptimizerInput;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineFactory;
import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * For starting a gRPC server with the implementation of
 * {@link PCSBasedOptimizerService}
 *
 * @author kadirayk
 *
 */
public class PCSBasedOptimizerGrpcServer {

	private static final File HASCOFileInput = new File("../mlplan/resources/automl/searchmodels/weka/weka-all-autoweka.json");

	private static Collection<Component> components;
	private static String requestedInterface;
	private static IObjectEvaluator<ComponentInstance, Double> evaluator;

	/**
	 * Starts the server on given port
	 *
	 * @param evaluator an implementation of {@link IObjectEvaluator} with
	 *                  {@link ComponentInstance} and Double
	 * @param input     {@link PCSBasedOptimizerInput}
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void start(final IObjectEvaluator<ComponentInstance, Double> evaluator, final Collection<Component> components, final String requestedInterface) throws IOException, InterruptedException {
		this.components = components;
		this.requestedInterface = requestedInterface;
		PCSBasedOptimizerConfig config = PCSBasedOptimizerConfig.get("conf/smac-optimizer-config.properties");
		Integer port = config.getPort();
		Server server = ServerBuilder.forPort(port).addService(new PCSBasedOptimizerServiceImpl(evaluator, input)).build();

		server.start();
		server.awaitTermination();

	}

	/**
	 * main method (and init()) is not actually needed, but helpful for debugging
	 * purposes
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String args[]) throws Exception {
		init();
		Server server = ServerBuilder.forPort(8080).addService(new PCSBasedOptimizerServiceImpl(evaluator, components, requestedInterface)).build();

		server.start();
		server.awaitTermination();

	}

	private static void init() {
		ComponentLoader cl = null;
		try {
			cl = new ComponentLoader(HASCOFileInput);
		} catch (IOException e) {

		}
		Collection<Component> components = cl.getComponents();
		String requestedInterface = "MLPipeline";
		String requestedComponent = "Pipeline";
		input = new PCSBasedOptimizerInput(components, requestedComponent, requestedInterface);
		WekaPipelineFactory classifierFactory = new WekaPipelineFactory();
		evaluator = new ComponentInstanceEvaluator(classifierFactory, "testrsc/iris.arff", "test");
	}

}
