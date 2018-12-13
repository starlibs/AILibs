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

import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.core.HASCOSolutionCandidate;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.OptimizingFactory;
import hasco.optimizingfactory.OptimizingFactoryProblem;
import hasco.serialization.ComponentLoader;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCO;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseHASCOFactory;
import hasco.variants.forwarddecomposition.twophase.TwoPhaseSoftwareConfigurationProblem;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.MathExt;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
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
public abstract class MLPlanWekaClassifier extends AAlgorithm<Instances, Classifier> implements Classifier, CapabilitiesHandler, OptionHandler {

	/* logging */
	private Logger logger = LoggerFactory.getLogger(MLPlanWekaClassifier.class);
	private String loggerName;

	private final File componentFile;
	private final Collection<Component> components;
	private final ClassifierFactory factory;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private final ADecomposableDoubleMeasure<Double> performanceMeasure;

	private Classifier selectedClassifier;
	private double internalValidationErrorOfSelectedClassifier;
	private TwoPhaseHASCOFactory hascoFactory;
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, Classifier, HASCOSolutionCandidate<Double>, Double> optimizingFactory;

	private Instances dataShownToSearch = null;

	public MLPlanWekaClassifier(final File componentFile, final ClassifierFactory factory, final ADecomposableDoubleMeasure<Double> performanceMeasure, final MLPlanClassifierConfig config) throws IOException {
		super(config);
		this.componentFile = componentFile;
		this.components = new ComponentLoader(componentFile).getComponents();
		this.factory = factory;
		this.performanceMeasure = performanceMeasure;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created: {

			/* check whether data has been set */
			if (this.getInput() == null) {
				throw new IllegalArgumentException("Data to work on is still NULL");
			}

			/* check number of CPUs assigned */
			if (this.getConfig().cpus() < 1) {
				throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.getConfig().cpus());
			}

			/* set up exact splits */
			float selectionDataPortion = this.getConfig().dataPortionForSelection();
			if (selectionDataPortion > 0) {
				List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(this.getInput(), new Random(this.getConfig().randomSeed()), selectionDataPortion);
				this.dataShownToSearch = selectionSplit.get(1);
			} else {
				this.dataShownToSearch = this.getInput();
			}
			if (this.dataShownToSearch.isEmpty()) {
				throw new IllegalStateException("Cannot search on no data.");
			}

			/* dynamically compute blow-ups */
			double blowUpInSelectionPhase = MathExt.round(1f / this.getConfig().getMCCVTrainFoldSizeDuringSearch() * this.getConfig().numberOfMCIterationsDuringSelection() / this.getConfig().numberOfMCIterationsDuringSearch(), 2);
			double blowUpInPostprocessing = MathExt.round((1 / (1 - this.getConfig().dataPortionForSelection())) / this.getConfig().numberOfMCIterationsDuringSelection(), 2);
			this.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
			this.getConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));

			/* communicate the parameters with which ML-Plan will run */
			this.logger.info(
					"Starting ML-Plan with the following setup:\n\tDataset: {}\n\tTarget: {}\n\tCPUs: {}\n\tTimeout: {}s\n\tTimeout for single candidate evaluation: {}s\n\tTimeout for node evaluation: {}s\n\tRandom Completions per node evaluation: {}\n\tPortion of data for selection phase: {}%\n\tMCCV for search: {} iterations with {}% for training\n\tMCCV for select: {} iterations with {}% for training\n\tBlow-ups are {} for selection phase and {} for post-processing phase.",
					this.getInput().relationName(), MultiClassPerformanceMeasure.ERRORRATE, this.getConfig().cpus(), this.getConfig().timeout(), this.getConfig().timeoutForCandidateEvaluation() / 1000,
					this.getConfig().timeoutForNodeEvaluation() / 1000, this.getConfig().randomCompletions(), MathExt.round(this.getConfig().dataPortionForSelection() * 100, 2), this.getConfig().numberOfMCIterationsDuringSearch(),
					(int) (100 * this.getConfig().getMCCVTrainFoldSizeDuringSearch()), this.getConfig().numberOfMCIterationsDuringSelection(), (int) (100 * this.getConfig().getMCCVTrainFoldSizeDuringSelection()),
					this.getConfig().expectedBlowupInSelection(), this.getConfig().expectedBlowupInPostprocessing());
			this.logger.info("Using the following preferred node evaluator: {}", this.preferredNodeEvaluator);

			/* create HASCO problem */
			IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(this.performanceMeasure, this.getConfig().numberOfMCIterationsDuringSearch(), this.dataShownToSearch,
					this.getConfig().getMCCVTrainFoldSizeDuringSearch(), this.getConfig().randomSeed());
			IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> searchBenchmark.evaluate(this.factory.getComponentInstantiation(c));
			IObjectEvaluator<Classifier, Double> selectionBenchmark = new IObjectEvaluator<Classifier, Double>() {

				@Override
				public Double evaluate(final Classifier object) throws Exception {

					/* first conduct MCCV */
					MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(MLPlanWekaClassifier.this.performanceMeasure, MLPlanWekaClassifier.this.getConfig().numberOfMCIterationsDuringSelection(),
							MLPlanWekaClassifier.this.getInput(), MLPlanWekaClassifier.this.getConfig().getMCCVTrainFoldSizeDuringSelection(), MLPlanWekaClassifier.this.getConfig().randomSeed());
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
			this.hascoFactory.setConfig(this.getConfig());
			this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, this.hascoFactory);
			this.optimizingFactory.setLoggerName(this.loggerName + ".2phasehasco");
			this.optimizingFactory.setTimeout(this.getConfig().timeout(), TimeUnit.SECONDS);
			this.optimizingFactory.registerListener(this);
			this.optimizingFactory.init();

			/* set state to active */
			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		}
		case active: {

			/* train the classifier returned by the optimizing factory */
			long startOptimizationTime = System.currentTimeMillis();
			this.selectedClassifier = this.optimizingFactory.call();
			this.internalValidationErrorOfSelectedClassifier = this.optimizingFactory.getPerformanceOfObject();
			long startBuildTime = System.currentTimeMillis();
			this.selectedClassifier.buildClassifier(this.getInput());
			long endBuildTime = System.currentTimeMillis();
			this.logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms", endBuildTime - startBuildTime, endBuildTime - startOptimizationTime);
			this.setState(AlgorithmState.inactive);
			return new AlgorithmFinishedEvent();
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
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
	public void registerListener(final Object listener) {
		this.optimizingFactory.registerListener(listener);
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

	public void setPortionOfDataForPhase2(final float portion) {
		this.getConfig().setProperty(MLPlanClassifierConfig.SELECTION_PORTION, String.valueOf(portion));
	}

	public void activateVisualization() {
		this.getConfig().setProperty(MLPlanClassifierConfig.K_VISUALIZE, String.valueOf(true));
	}

	public void deactivateVisualization() {
		this.getConfig().setProperty(MLPlanClassifierConfig.K_VISUALIZE, String.valueOf(false));
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return this.preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(final INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}

	@Override
	public MLPlanClassifierConfig getConfig() {
		return (MLPlanClassifierConfig) super.getConfig();
	}

	public File getComponentFile() {
		return this.componentFile;
	}

	public void setTimeoutForSingleSolutionEvaluation(final int timeout) {
		this.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeout * 1000));
	}

	public void setTimeoutForNodeEvaluation(final int timeout) {
		this.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeout * 1000));
	}

	public Collection<Component> getComponents() {
		return Collections.unmodifiableCollection(this.components);
	}

	public void setRandomSeed(final int seed) {
		this.getConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_SEED, String.valueOf(seed));
	}

	@Subscribe
	public void receiveSolutionEvent(final SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> event) {
		HASCOSolutionCandidate<Double> solution = event.getSolutionCandidate();
		try {
			this.logger.info("Received new solution {} with score {} and evaluation time {}ms", this.factory.getComponentInstantiation(solution.getComponentInstance()), solution.getScore(), solution.getTimeToEvaluateCandidate());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.post(event);
	}

	public void registerListenerForSolutionEvaluations(final Object listener) {
		super.registerListener(listener);
	}

	public Classifier getSelectedClassifier() {
		return this.selectedClassifier;
	}

	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		if (this.getState() == AlgorithmState.created) {
			this.init();
		}
		TwoPhaseHASCO twoPhaseHASCO = ((TwoPhaseHASCO) this.optimizingFactory.getOptimizer());
		return twoPhaseHASCO.getGraphGenerator();
	}

	public void setData(final Instances data) {
		this.setInput(data);
	}

	public Instances getData() {
		return this.getInput();
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

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		if (this.factory instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.factory).setLoggerName(name + ".factory");
		}
		if (this.preferredNodeEvaluator instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.preferredNodeEvaluator).setLoggerName(name + ".preferrednodeeval");
		}
		if (this.optimizingFactory instanceof ILoggingCustomizable) {
			this.optimizingFactory.setLoggerName(name + ".optimizingfactory");
		}
		if (this.hascoFactory instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.hascoFactory).setLoggerName(name + ".hascofactory");
		}
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}
}