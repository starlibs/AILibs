package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.core.MLPlan;
import de.upb.crc901.mlplan.multiclass.core.MLPlanBuilder;
import hasco.model.Component;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

/**
 * A WEKA classifier wrapping the functionality of ML-Plan where the constructed
 * object is a WEKA classifier.
 *
 * It implements the algorithm interface with itself (with modified state) as an
 * output
 *
 * @author wever, fmohr
 *
 */
public class MLPlanWekaClassifier implements Classifier, CapabilitiesHandler, OptionHandler, ILoggingCustomizable {

	/** Logger for controlled output. */
	private Logger logger = LoggerFactory.getLogger(MLPlanWekaClassifier.class);
	private String loggerName;

	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private double internalValidationErrorOfSelectedClassifier;
	private final MLPlanBuilder builder;
	private TimeOut timeout;
	private MLPlan mlplan;
	private Classifier classifierFoundByMLPlan;

	public MLPlanWekaClassifier(MLPlanBuilder builder) throws IOException {
		this.builder = builder;
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		mlplan = new MLPlan(builder, data);
		mlplan.setTimeout(timeout);
		classifierFoundByMLPlan = mlplan.call();
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		if (this.classifierFoundByMLPlan == null) {
			throw new IllegalStateException("Classifier has not been built yet.");
		}
		return this.classifierFoundByMLPlan.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		if (this.classifierFoundByMLPlan == null) {
			throw new IllegalStateException("Classifier has not been built yet.");
		}
		return this.classifierFoundByMLPlan.distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		Capabilities result = new Capabilities(this);
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.DATE_ATTRIBUTES);
		result.enable(Capability.STRING_ATTRIBUTES);
		result.enable(Capability.RELATIONAL_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.NUMERIC_CLASS);
		result.enable(Capability.DATE_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		// instances
		result.setMinimumNumberInstances(1);
		return result;
	}

	@Override
	public Enumeration<Option> listOptions() {
		return null;
	}

	@Override
	public void setOptions(final String[] options) throws Exception {
		// for (int i = 0; i < options.length; i++) {
		// switch (options[i].toLowerCase()) {
		// case "-t": {
		// this.setTimeout(Integer.parseInt(options[++i]));
		// break;
		// }
		// case "-r": {
		// this.setRandom(Integer.parseInt(options[++i]));
		// break;
		// }
		// default: {
		// throw new IllegalArgumentException("Unknown option " + options[i] + ".");
		// }
		// }
		// }
	}

	@Override
	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger.info("Switching logger name to {}", name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched ML-Plan logger to {}", name);
	}

	public void setPortionOfDataForPhase2(final float portion) {
		getMLPlanConfig().setProperty(MLPlanClassifierConfig.SELECTION_PORTION, String.valueOf(portion));
	}

	public void activateVisualization() {
		getMLPlanConfig().setProperty(MLPlanClassifierConfig.K_VISUALIZE, String.valueOf(true));
	}

	public void deactivateVisualization() {
		getMLPlanConfig().setProperty(MLPlanClassifierConfig.K_VISUALIZE, String.valueOf(false));
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return this.preferredNodeEvaluator;
	}
	
	public void setTimeout(final TimeOut timeout) {
		this.timeout = timeout;
	}

	public void setPreferredNodeEvaluator(final INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}

	public MLPlanClassifierConfig getMLPlanConfig() {
		return builder.getAlgorithmConfig();
	}

	public File getComponentFile() {
		return builder.getSearchSpaceConfigFile();
	}
	
	public Collection<Component> getComponents() {
		return builder.getComponents();
	}

	public void setTimeoutForSingleSolutionEvaluation(final int timeout) {
		getMLPlanConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeout * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeout) {
		getMLPlanConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeout * 1000));
	}

	public void setRandomSeed(final int seed) {
		getMLPlanConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_SEED, String.valueOf(seed));
	}

	public Classifier getSelectedClassifier() {
		return this.classifierFoundByMLPlan;
	}
	
	public double getInternalValidationErrorOfSelectedClassifier() {
		return this.internalValidationErrorOfSelectedClassifier;
	}
}