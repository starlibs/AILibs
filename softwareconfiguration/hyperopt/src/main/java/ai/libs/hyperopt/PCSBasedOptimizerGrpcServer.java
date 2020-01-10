package ai.libs.hyperopt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.pcsbasedoptimization.proto.PCSBasedOptimizerService;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hyperopt.optimizer.PCSBasedOptimizerConfig;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.core.ILearnerFactory;
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

	private static final File HASCOFileInput = new File("../mlplan/resources/automl/searchmodels/weka/autoweka.json");

	private static PCSBasedOptimizerInput input;
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
	public static void start(final IObjectEvaluator<ComponentInstance, Double> evaluator, final PCSBasedOptimizerInput input) throws IOException, InterruptedException {
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
	public static void main(final String[] args) throws Exception {
		init();
		Server server = ServerBuilder.forPort(8080).addService(new PCSBasedOptimizerServiceImpl(evaluator, input)).build();

		server.start();
		server.awaitTermination();

	}

	private static void init() throws SplitFailedException {
		ComponentLoader cl;
		try {
			cl = new ComponentLoader(HASCOFileInput);
			Collection<Component> components = cl.getComponents();
			String requestedInterface = "BaseClassifier";
			input = new PCSBasedOptimizerInput(components, requestedInterface);
			ILearnerFactory<IWekaClassifier> classifierFactory = new WekaPipelineFactory();
			evaluator = new WekaComponentInstanceEvaluator(classifierFactory, "iris.arff", "algoID");
		} catch (IOException e) {
			throw new SplitFailedException(e);
		}
	}

}
