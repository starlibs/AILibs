package ai.libs.mlplan.multiclass.wekamlplan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.LearnerConfigurationFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.event.IEventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.Component;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;

/**
 * A WEKA classifier wrapping the functionality of ML-Plan where the constructed object is a WEKA classifier.
 *
 * It implements the algorithm interface with itself (with modified state) as an output
 *
 * @author wever, fmohr
 *
 */
@SuppressWarnings("serial")
public class MLPlanWekaClassifier implements Classifier, CapabilitiesHandler, OptionHandler, ILoggingCustomizable, IWekaClassifier, IEventEmitter {

	/* Logger for controlled output. */
	private transient Logger logger = LoggerFactory.getLogger(MLPlanWekaClassifier.class);
	private String loggerName;

	/* MLPlan Builder and the instance of mlplan */
	private final transient MLPlanWekaBuilder builder;

	/* The timeout for the selecting a classifier. */
	private Timeout timeout;

	/* The output of mlplan, i.e., the selected classifier and the internal validation error measured on the given data. */
	private IWekaClassifier classifierFoundByMLPlan;
	private double internalValidationErrorOfSelectedClassifier;

	private final transient List<Object> listeners = new ArrayList<>();

	public MLPlanWekaClassifier(final MLPlanWekaBuilder builder) {
		this.builder = builder;
		this.timeout = builder.getTimeOut();
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		this.fit(new WekaInstances(data));
	}

	public double[] classifyInstances(final Instances instances) throws Exception {
		double[] predictionsAsDoubles = new double[instances.size()];
		List<? extends IPrediction> predictions = this.classifierFoundByMLPlan.predict(new WekaInstances(instances)).getPredictions();
		for (int i = 0; i < instances.size(); i++) {
			predictionsAsDoubles[i] = (double)predictions.get(i).getPrediction();
		}
		return predictionsAsDoubles;
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		if (this.classifierFoundByMLPlan == null) {
			throw new IllegalStateException("Classifier has not been built yet.");
		}
		return this.classifierFoundByMLPlan.getClassifier().classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		if (this.classifierFoundByMLPlan == null) {
			throw new IllegalStateException("Classifier has not been built yet.");
		}
		return this.classifierFoundByMLPlan.getClassifier().distributionForInstance(instance);
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
		/* As there are no options, simply return null. */
		return null;
	}

	@Override
	public void setOptions(final String[] options) throws Exception {
		/* Intentionally left blank. */
	}

	@Override
	public String[] getOptions() {
		/* As there are no options, simply return an empty array. */
		return new String[] {};
	}

	public void setTimeout(final Timeout timeout) {
		this.timeout = timeout;
	}

	public MLPlanClassifierConfig getMLPlanConfig() {
		return this.builder.getAlgorithmConfig();
	}

	public Collection<Component> getComponents() throws IOException {
		return this.builder.getComponents();
	}

	/**
	 * @return An object of the classifier ML-Plan has selected during the build.
	 */
	public Classifier getSelectedWekaClassifier() {
		return this.classifierFoundByMLPlan.getClassifier();
	}

	/**
	 * @return The internal validation error (during selection phase) of the selected classifier.
	 */
	public double getInternalValidationErrorOfSelectedClassifier() {
		return this.internalValidationErrorOfSelectedClassifier;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger.info("Switching logger name to {}", name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched ML-Plan logger to {}", name);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void registerListener(final Object listener) {
		this.listeners.add(listener);
	}

	@Override
	public IPrediction fitAndPredict(final ILabeledDataset<? extends ILabeledInstance> dTrain, final ILabeledInstance xTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(xTest);
	}

	@Override
	public IPredictionBatch fitAndPredict(final ILabeledDataset<? extends ILabeledInstance> dTrain, final ILabeledInstance[] xTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(xTest);
	}

	@Override
	public IPredictionBatch fitAndPredict(final ILabeledDataset<? extends ILabeledInstance> dTrain, final ILabeledDataset<? extends ILabeledInstance> dTest) throws TrainingException, PredictionException, InterruptedException {
		this.fit(dTrain);
		return this.predict(dTest);
	}

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		Objects.requireNonNull(this.timeout, "Timeout must be set before running ML-Plan.");

		MLPlan<IWekaClassifier> mlplan = this.builder.withDataset(dTrain).build();
		this.listeners.forEach(mlplan::registerListener);
		mlplan.setTimeout(this.timeout);
		if (this.loggerName != null) {
			mlplan.setLoggerName(this.loggerName + "." + "mlplan");
		}
		try {
			this.classifierFoundByMLPlan = mlplan.call();
		} catch (AlgorithmTimeoutedException | AlgorithmException | AlgorithmExecutionCanceledException e) {
			throw new TrainingException("Could not finish ML-Plan training.", e);
		}

	}

	@Override
	public IPrediction predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		return this.classifierFoundByMLPlan.predict(xTest);
	}

	@Override
	public IPredictionBatch predict(final ILabeledDataset<? extends ILabeledInstance> dTest) throws PredictionException, InterruptedException {
		return this.classifierFoundByMLPlan.predict(dTest);
	}

	@Override
	public IPredictionBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		return this.classifierFoundByMLPlan.predict(dTest);
	}

	@Override
	public void setConfig(final Map<String, Object> config) throws LearnerConfigurationFailedException, InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getConfig() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Classifier getClassifier() {
		return this.getSelectedWekaClassifier();
	}

}