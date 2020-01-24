package ai.libs.hyperopt;

import java.io.File;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineFactory;

public class PCSBasedOptimizationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(PCSBasedOptimizationRunner.class);

	public static void main(final String[] args) throws Exception {
		// initialize
		File HASCOFileInput = new File("../mlplan/resources/automl/searchmodels/weka/autoweka.json");
		ComponentLoader cl = new ComponentLoader(HASCOFileInput);
		Collection<Component> components = cl.getComponents();
		String requestedInterface = "BaseClassifier";
		PCSBasedOptimizerInput input = new PCSBasedOptimizerInput(components, requestedInterface);
		ILearnerFactory<IWekaClassifier> classifierFactory = new WekaPipelineFactory();
		WekaComponentInstanceEvaluator evaluator = new WekaComponentInstanceEvaluator(classifierFactory, "testrsc/iris.arff", "HyperBandOptimizer");

		// generate PCS files
		HASCOToPCSConverter.generatePCSFile(input, "PCSBasedOptimizerScripts/HyperBandOptimizer/");

		// optimization
		throw new UnsupportedOperationException("Felix removed tremenduos code duplicates here. Please use AlgorithmVisualizationWindow");
	}

}
