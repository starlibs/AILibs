package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.core.HASCOSolutionCandidate;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.OptimizingFactory;
import hasco.optimizingfactory.OptimizingFactoryProblem;
import hasco.serialization.ComponentLoader;
import hasco.variants.twophase.TwoPhaseHASCOFactory;
import hasco.variants.twophase.TwoPhaseSoftwareConfigurationProblem;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.BasicMLEvaluator;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
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
 * A WEKA classifier wrapping the functionality of ML-Plan where the constructed object is a WEKA classifier.
 *
 * @author wever, fmohr
 *
 */
public abstract class MLPlanWekaClassifier implements Classifier, CapabilitiesHandler, OptionHandler, ILoggingCustomizable {

	/** Logger for controlled output. */
	private Logger logger = LoggerFactory.getLogger(MLPlanWekaClassifier.class);
	private String loggerName;
	private static final String REQUESTED_INTERFACE = "weka.classifiers.bayes.BayesNet"; // "AbstractClassifier";

	private final File componentFile;
	private final Collection<Component> components;
	private final ClassifierFactory factory;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private final BasicMLEvaluator benchmark;
	private final MLPlanClassifierConfig config;
	private Classifier selectedClassifier;
	private final EventBus eventBus = new EventBus();

	public MLPlanWekaClassifier(final File componentFile, final ClassifierFactory factory, final BasicMLEvaluator benchmark, final MLPlanClassifierConfig config) throws IOException {
		this.componentFile = componentFile;
		this.components = new ComponentLoader(componentFile).getComponents();
		this.factory = factory;
		this.benchmark = benchmark;
		this.config = config;
	}

	protected abstract INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(Instances data);

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		if (this.config.cpus() < 1) {
			throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.config.cpus());
		}

		// // Setup preferred node evaluator
		// this.setPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(super.getComponents(), FileUtil.readFileAsList(this.getConfig().componentsPrecedenceListFile())));
		// // Extend the functionality of the preferred node evaluator and enhance it by some additional knowledge.
		// this.setPreferredNodeEvaluator(new SemanticNodeEvaluator(this.getComponents(), data, this.getPreferredNodeEvaluator()));
		this.logger.info("Starting ML-Plan with {} CPUs and a timeout of {}s, and a portion of {} for the second phase.", this.config.cpus(), this.config.timeout(), this.config.dataPortionForSelection());

		float selectionDataPortion = .05f; // TODO: Configurable machen
		Instances dataForSearch;
		Instances dataPreservedForSelection;
		if (selectionDataPortion > 0) {
			List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(data, new Random(this.config.randomSeed()), selectionDataPortion);
			dataForSearch = selectionSplit.get(1);
			dataPreservedForSelection = selectionSplit.get(0);
		} else {
			dataForSearch = data;
			dataPreservedForSelection = null;
		}

		if (dataForSearch.isEmpty()) {
			throw new IllegalStateException("Cannot search on no data and select on " + dataPreservedForSelection.size() + " data points.");
		}

		/* create HASCO problem */
		IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(this.benchmark, this.config.numberOfMCIterationsDuringSearch(), dataForSearch, selectionDataPortion);
		IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> searchBenchmark.evaluate(this.factory.getComponentInstantiation(c));
		IObjectEvaluator<Classifier, Double> selectionBenchmark = new IObjectEvaluator<Classifier, Double>() {

			@Override
			public Double evaluate(final Classifier object) throws Exception {

				/* first conduct MCCV */
				MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(MLPlanWekaClassifier.this.benchmark, MLPlanWekaClassifier.this.config.numberOfMCIterationsDuringSelection(), data, 1 - selectionDataPortion);
				mccv.evaluate(object);

				/* now retrieve .75-percentile from stats */
				double mean = mccv.getStats().getMean();
				double percentile = mccv.getStats().getPercentile(75f);
				MLPlanWekaClassifier.this.logger.info("Select {} as .75-percentile where {} would have been the mean. Samples size of MCCV was {}", percentile, mean, mccv.getStats().getN());
				return percentile;
			}
		};
		IObjectEvaluator<ComponentInstance, Double> wrappedSelectionBenchmark = c -> selectionBenchmark.evaluate(this.factory.getComponentInstantiation(c));
		TwoPhaseSoftwareConfigurationProblem problem = new TwoPhaseSoftwareConfigurationProblem(this.componentFile, REQUESTED_INTERFACE, wrappedSearchBenchmark, wrappedSelectionBenchmark);

		/* configure and start optimizing factory */
		OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(this.factory, problem);
		TwoPhaseHASCOFactory hascoFactory = new TwoPhaseHASCOFactory();
		hascoFactory.setPreferredNodeEvaluator(new AlternativeNodeEvaluator<TFDNode, Double>(this.getSemanticNodeEvaluator(dataForSearch), this.preferredNodeEvaluator));
		hascoFactory.setConfig(this.config);
		OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, hascoFactory);
		optimizingFactory.setLoggerName(this.loggerName + ".2phasehasco");
		optimizingFactory.setTimeout(this.config.timeout(), TimeUnit.SECONDS);
		optimizingFactory.registerListener(this);
		this.selectedClassifier = optimizingFactory.call();

		/* train the classifier returned by the optimizing factory */
		this.selectedClassifier.buildClassifier(data);
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		if (this.selectedClassifier == null) {
			throw new IllegalStateException("Classifier has not been built yet.");
		}
		return this.selectedClassifier.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		if (this.selectedClassifier == null) {
			throw new IllegalStateException("Classifier has not been built yet.");
		}

		return this.selectedClassifier.distributionForInstance(instance);
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
		this.config.setProperty(MLPlanClassifierConfig.SELECTION_PORTION, String.valueOf(portion));
	}

	public void setTimeout(final int seconds) {
		this.config.setProperty(MLPlanClassifierConfig.K_TIMEOUT, String.valueOf(seconds));
	}

	public void activateVisualization() {
		this.config.setProperty(MLPlanClassifierConfig.K_VISUALIZE, String.valueOf(true));
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return this.preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(final INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}

	public MLPlanClassifierConfig getConfig() {
		return this.config;
	}

	public File getComponentFile() {
		return this.componentFile;
	}

	public void setTimeoutForSingleSolutionEvaluation(final int timeout) {
		this.config.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeout));
	}

	public void setTimeoutForNodeEvaluation(final int timeout) {
		this.config.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeout));
	}

	public Collection<Component> getComponents() {
		return Collections.unmodifiableCollection(this.components);
	}

	public void setRandomSeed(final int seed) {
		this.config.setProperty(MLPlanClassifierConfig.K_RANDOM_SEED, String.valueOf(seed));
	}

	public void setNumCPUs(final int num) {
		if (num < 1) {
			throw new IllegalArgumentException("Need to work with at least one CPU");
		}
		if (num > Runtime.getRuntime().availableProcessors()) {
			this.logger.warn("Warning, configuring {} CPUs where the system has only {}", num, Runtime.getRuntime().availableProcessors());
		}
		this.config.setProperty(MLPlanClassifierConfig.K_CPUS, String.valueOf(num));
	}

	@Subscribe
	public void receiveSolutionEvent(final SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> event) {
		this.eventBus.post(event);
	}

	public void registerListenerForSolutionEvaluations(final Object listener) {
		this.eventBus.register(listener);
	}

	public Classifier getSelectedClassifier() {
		return this.selectedClassifier;
	}
}
