package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import hasco.model.Component;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
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

	/* Logger for controlled output. */
	private Logger logger = LoggerFactory.getLogger(MLPlanWekaClassifier.class);
	private String loggerName;

	private static final boolean visualizationActive = false;

	/* MLPlan Builder and the instance of mlplan */
	private final MLPlanBuilder builder;
	private MLPlan mlplan;

	/* The timeout for the selecting a classifier. */
	private TimeOut timeout;

	/* The output of mlplan, i.e., the selected classifier and the internal validation error measured on the given data. */
	private Classifier classifierFoundByMLPlan;
	private double internalValidationErrorOfSelectedClassifier;

	public MLPlanWekaClassifier(final MLPlanBuilder builder) throws IOException {
		this.builder = builder;
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		Objects.requireNonNull(this.timeout, "Timeout must be set before running ML-Plan.");

		this.mlplan = new MLPlan(this.builder, data);
		this.mlplan.setTimeout(this.timeout);
		if (this.loggerName != null) {
			this.mlplan.setLoggerName(this.loggerName + "." + "mlplan");
		}

		if (visualizationActive) {
			new JFXPanel();
			AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(this.mlplan, new GraphViewPlugin(), new NodeInfoGUIPlugin<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())),
					new SearchRolloutHistogramPlugin<>(), new SolutionPerformanceTimelinePlugin(), new HASCOModelStatisticsPlugin());
			Platform.runLater(window);
		}

		this.classifierFoundByMLPlan = this.mlplan.call();
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
		/* As there are no options, simply return null. */
		return null;
	}

	@Override
	public void setOptions(final String[] options) throws Exception {
		/* Intentionally left blank. */
	}

	@Override
	public String[] getOptions() {
		/* As there are no options, simply return null. */
		return null;
	}

	public void setTimeout(final TimeOut timeout) {
		this.timeout = timeout;
	}

	public MLPlanClassifierConfig getMLPlanConfig() {
		return this.builder.getAlgorithmConfig();
	}

	public Collection<Component> getComponents() {
		return this.builder.getComponents();
	}

	/**
	 * @return An object of the classifier ML-Plan has selected during the build.
	 */
	public Classifier getSelectedClassifier() {
		return this.classifierFoundByMLPlan;
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
}