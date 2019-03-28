package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import hasco.optimizingfactory.OptimizingFactory;
import hasco.optimizingfactory.OptimizingFactoryProblem;
import hasco.serialization.ComponentLoader;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCO;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOFactory;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseSoftwareConfigurationProblem;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.MathExt;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
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
public abstract class MLPlanWekaClassifier implements Classifier, CapabilitiesHandler, OptionHandler, ILoggingCustomizable, IAlgorithm<Instances, Classifier> {

	/** Logger for controlled output. */
	private Logger logger = LoggerFactory.getLogger(MLPlanWekaClassifier.class);
	private String loggerName;

	private final File componentFile;
	private final Collection<Component> components;
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> componentParamRefinements;
	private final ClassifierFactory factory;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private final ADecomposableDoubleMeasure<Double> performanceMeasure;
	private final MLPlanClassifierConfig config;
	private Classifier selectedClassifier;
	private double internalValidationErrorOfSelectedClassifier;
	private final EventBus eventBus = new EventBus();
	private TwoPhaseHASCOFactory hascoFactory;
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, Classifier, HASCOSolutionCandidate<Double>, Double> optimizingFactory;

	private AlgorithmState state = AlgorithmState.created;
	private Instances dataShownToSearch = null;
	private Instances data = null;

	public MLPlanWekaClassifier(final File componentFile, final ClassifierFactory factory, final ADecomposableDoubleMeasure<Double> performanceMeasure, final MLPlanClassifierConfig config) throws IOException {
		this.componentFile = componentFile;
		ComponentLoader loader = new ComponentLoader(componentFile);
		this.components = loader.getComponents();
		this.componentParamRefinements = loader.getParamConfigs();
		this.factory = factory;
		this.performanceMeasure = performanceMeasure;
		this.config = config;
	}

	@Override
	public boolean hasNext() {
		return this.state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return this.nextWithException();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.state) {
		case created: {

			/* check whether data has been set */
			if (this.data == null) {
				throw new IllegalArgumentException("Data to work on is still NULL");
			}

			/* check number of CPUs assigned */
			if (this.config.cpus() < 1) {
				throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.config.cpus());
			}

			/* set up exact splits */
			float selectionDataPortion = this.config.dataPortionForSelection();
			if (selectionDataPortion > 0) {
				List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(this.data, new Random(this.config.randomSeed()), selectionDataPortion);
				this.dataShownToSearch = selectionSplit.get(1);
			} else {
				this.dataShownToSearch = this.data;
			}
			if (this.dataShownToSearch.isEmpty()) {
				throw new IllegalStateException("Cannot search on no data.");
			}

			/* dynamically compute blow-ups */
			double blowUpInSelectionPhase = MathExt.round(1f / this.config.getMCCVTrainFoldSizeDuringSearch() * this.config.numberOfMCIterationsDuringSelection() / this.config.numberOfMCIterationsDuringSearch(), 2);
			double blowUpInPostprocessing = MathExt.round((1 / (1 - this.config.dataPortionForSelection())) / this.config.numberOfMCIterationsDuringSelection(), 2);
			this.config.setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
			this.config.setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));

			/* communicate the parameters with which ML-Plan will run */
			this.logger.info(
					"Starting ML-Plan with the following setup:\n\tDataset: {}\n\tTarget: {}\n\tCPUs: {}\n\tTimeout: {}s\n\tTimeout for single candidate evaluation: {}s\n\tTimeout for node evaluation: {}s\n\tRandom Completions per node evaluation: {}\n\tPortion of data for selection phase: {}%\n\tMCCV for search: {} iterations with {}% for training\n\tMCCV for select: {} iterations with {}% for training\n\tBlow-ups are {} for selection phase and {} for post-processing phase.",
					this.data.relationName(), MultiClassPerformanceMeasure.ERRORRATE, this.config.cpus(), this.config.timeout(), this.config.timeoutForCandidateEvaluation() / 1000, this.config.timeoutForNodeEvaluation() / 1000,
					this.config.randomCompletions(), MathExt.round(this.config.dataPortionForSelection() * 100, 2), this.config.numberOfMCIterationsDuringSearch(), (int) (100 * this.config.getMCCVTrainFoldSizeDuringSearch()),
					this.config.numberOfMCIterationsDuringSelection(), (int) (100 * this.config.getMCCVTrainFoldSizeDuringSelection()), this.config.expectedBlowupInSelection(), this.config.expectedBlowupInPostprocessing());
			this.logger.info("Using the following preferred node evaluator: {}", this.preferredNodeEvaluator);

			/* create HASCO problem */
			IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(this.performanceMeasure, this.config.numberOfMCIterationsDuringSearch(), this.dataShownToSearch,
					this.config.getMCCVTrainFoldSizeDuringSearch(), this.config.randomSeed());
			IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> searchBenchmark.evaluate(this.factory.getComponentInstantiation(c));
			IObjectEvaluator<Classifier, Double> selectionBenchmark = new IObjectEvaluator<Classifier, Double>() {

				@Override
				public Double evaluate(final Classifier object) throws Exception {

					/* first conduct MCCV */
					MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(MLPlanWekaClassifier.this.performanceMeasure, MLPlanWekaClassifier.this.config.numberOfMCIterationsDuringSelection(),
							MLPlanWekaClassifier.this.data, MLPlanWekaClassifier.this.config.getMCCVTrainFoldSizeDuringSelection(), config.randomSeed());
					mccv.evaluate(object);

					/* now retrieve .75-percentile from stats */
					double mean = mccv.getStats().getMean();
					double percentile = mccv.getStats().getPercentile(75f);
					MLPlanWekaClassifier.this.logger.info("Select {} as .75-percentile where {} would have been the mean. Samples size of MCCV was {}", percentile, mean, mccv.getStats().getN());
					return percentile;
				}
			};
			IObjectEvaluator<ComponentInstance, Double> wrappedSelectionBenchmark = c -> selectionBenchmark.evaluate(this.factory.getComponentInstantiation(c));
			TwoPhaseSoftwareConfigurationProblem problem = new TwoPhaseSoftwareConfigurationProblem(this.componentFile, "AbstractClassifier", wrappedSearchBenchmark, wrappedSelectionBenchmark);

			/* configure and start optimizing factory */
			OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(this.factory, problem);
			this.hascoFactory = new TwoPhaseHASCOFactory();
			this.hascoFactory.setPreferredNodeEvaluator(new AlternativeNodeEvaluator<TFDNode, Double>(this.getSemanticNodeEvaluator(this.dataShownToSearch), this.preferredNodeEvaluator));
			this.hascoFactory.setConfig(this.config);
			this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, this.hascoFactory);
			this.optimizingFactory.setLoggerName(this.loggerName + ".2phasehasco");
			this.optimizingFactory.setTimeout(this.config.timeout(), TimeUnit.SECONDS);
			this.optimizingFactory.registerListener(this);
			this.optimizingFactory.init();

			/* set state to active */
			this.state = AlgorithmState.active;
			return new AlgorithmInitializedEvent();
		}
		case active: {

			/* train the classifier returned by the optimizing factory */
			long startOptimizationTime = System.currentTimeMillis();
			this.selectedClassifier = this.optimizingFactory.call();
			this.internalValidationErrorOfSelectedClassifier = this.optimizingFactory.getPerformanceOfObject();
			long startBuildTime = System.currentTimeMillis();
			this.selectedClassifier.buildClassifier(this.data);
			long endBuildTime = System.currentTimeMillis();
			this.logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms", endBuildTime - startBuildTime, endBuildTime - startOptimizationTime);
			this.state = AlgorithmState.inactive;
			return new AlgorithmFinishedEvent();
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.state);
		}
	}

	@Override
	public Classifier call() throws Exception {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this;
	}

	@Override
	public Instances getInput() {
		return this.data;
	}

	@Override
	public void registerListener(final Object listener) {
		this.optimizingFactory.registerListener(listener);
	}

	@Override
	public int getNumCPUs() {
		return 0;
	}

	@Override
	public void setTimeout(final int timeout, final TimeUnit timeUnit) {

	}
	
	@Override
	public void setTimeout(final TimeOut timeout) {

	}

	@Override
	public TimeOut getTimeout() {
		return null;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public void cancel() {
		
	}

	protected abstract INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(Instances data);

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		this.setData(data);
		this.call();
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
	
	public void deactivateVisualization() {
		this.config.setProperty(MLPlanClassifierConfig.K_VISUALIZE, String.valueOf(false));
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
		this.config.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeout * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeout) {
		this.config.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeout * 1000));
	}

	public Collection<Component> getComponents() {
		return Collections.unmodifiableCollection(this.components);
	}

	public void setRandomSeed(final int seed) {
		this.config.setProperty(MLPlanClassifierConfig.K_RANDOM_SEED, String.valueOf(seed));
	}

	@Override
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
		HASCOSolutionCandidate<Double> solution = event.getSolutionCandidate();
		try {
			this.logger.info("Received new solution {} with score {} and evaluation time {}ms", this.factory.getComponentInstantiation(solution.getComponentInstance()), solution.getScore(), solution.getTimeToEvaluateCandidate());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.eventBus.post(event);
	}

	public void registerListenerForSolutionEvaluations(final Object listener) {
		this.eventBus.register(listener);
	}

	public Classifier getSelectedClassifier() {
		return this.selectedClassifier;
	}

	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		if (this.state == AlgorithmState.created) {
			this.init();
		}
		TwoPhaseHASCO twoPhaseHASCO = ((TwoPhaseHASCO) this.optimizingFactory.getOptimizer());
		return twoPhaseHASCO.getGraphGenerator();
	}

	public void setData(final Instances data) {
		this.data = data;
	}

	public Instances getData() {
		return this.data;
	}

	public AlgorithmInitializedEvent init() {
		AlgorithmEvent e = null;
		while (this.hasNext()) {
			e = this.next();
			if (e instanceof AlgorithmInitializedEvent) {
				return (AlgorithmInitializedEvent) e;
			}
		}
		throw new IllegalStateException("Could not complete initialization");
	}

	public double getInternalValidationErrorOfSelectedClassifier() {
		return this.internalValidationErrorOfSelectedClassifier;
	}

	public Map<Component, Map<Parameter, ParameterRefinementConfiguration>> getComponentParamRefinements() {
		return componentParamRefinements;
	}
}