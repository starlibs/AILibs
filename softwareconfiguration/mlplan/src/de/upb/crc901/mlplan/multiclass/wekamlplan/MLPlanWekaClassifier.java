package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.core.HASCOSolutionCandidate;
import hasco.exceptions.ComponentInstantiationFailedException;
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
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import jaicore.ml.evaluation.evaluators.weka.AbstractEvaluatorMeasureBridge;
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
 * A WEKA classifier wrapping the functionality of ML-Plan where the constructed
 * object is a WEKA classifier.
 *
 * It implements the algorithm interface with itself (with modified state) as an
 * output
 *
 * @author wever, fmohr
 *
 */
public abstract class MLPlanWekaClassifier extends AAlgorithm<Instances, Classifier> implements Classifier, CapabilitiesHandler, OptionHandler, ILoggingCustomizable {

	/** Logger for controlled output. */
	private Logger logger = LoggerFactory.getLogger(MLPlanWekaClassifier.class);
	private String loggerName;

	private final File componentFile;
	private final Collection<Component> components;
	private final ClassifierFactory factory;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private final AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge;
	private Classifier selectedClassifier;
	private double internalValidationErrorOfSelectedClassifier;
	private TwoPhaseHASCOFactory hascoFactory;
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, Classifier, HASCOSolutionCandidate<Double>, Double> optimizingFactory;

	private Instances dataShownToSearch = null;

	public MLPlanWekaClassifier(final File componentFile, final ClassifierFactory factory, final AbstractEvaluatorMeasureBridge<Double, Double> evaluationMeasurementBridge, final MLPlanClassifierConfig config) throws IOException {
		super(config);
		this.componentFile = componentFile;
		this.components = new ComponentLoader(componentFile).getComponents();
		this.factory = factory;
		this.evaluationMeasurementBridge = evaluationMeasurementBridge;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
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
			double selectionDataPortion = this.getConfig().dataPortionForSelection();
			if (selectionDataPortion > 0) {
				List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(this.getInput(), this.getConfig().randomSeed(), selectionDataPortion);
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
					this.getInput().relationName(), MultiClassPerformanceMeasure.ERRORRATE, this.getConfig().cpus(), this.getTimeout().seconds(), this.getConfig().timeoutForCandidateEvaluation() / 1000,
					this.getConfig().timeoutForNodeEvaluation() / 1000, this.getConfig().randomCompletions(), MathExt.round(this.getConfig().dataPortionForSelection() * 100, 2), this.getConfig().numberOfMCIterationsDuringSearch(),
					(int) (100 * this.getConfig().getMCCVTrainFoldSizeDuringSearch()), this.getConfig().numberOfMCIterationsDuringSelection(), (int) (100 * this.getConfig().getMCCVTrainFoldSizeDuringSelection()),
					this.getConfig().expectedBlowupInSelection(), this.getConfig().expectedBlowupInPostprocessing());
			this.logger.info("Using the following preferred node evaluator: {}", this.preferredNodeEvaluator);

			/* create HASCO problem */
			IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(this.evaluationMeasurementBridge, this.getConfig().numberOfMCIterationsDuringSearch(), this.dataShownToSearch,
					this.getConfig().getMCCVTrainFoldSizeDuringSearch(), this.getConfig().randomSeed());

			IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> {
				try {
					if (this.evaluationMeasurementBridge instanceof CacheEvaluatorMeasureBridge) {
						CacheEvaluatorMeasureBridge bridge = ((CacheEvaluatorMeasureBridge) this.evaluationMeasurementBridge).getShallowCopy(c);

						long seed = this.getConfig().randomSeed() + c.hashCode();

						IObjectEvaluator<Classifier, Double> copiedSearchBenchmark = new MonteCarloCrossValidationEvaluator(bridge, this.getConfig().numberOfMCIterationsDuringSearch(), this.dataShownToSearch,
								this.getConfig().getMCCVTrainFoldSizeDuringSearch(), seed);

						return copiedSearchBenchmark.evaluate(this.factory.getComponentInstantiation(c));
					}
					return searchBenchmark.evaluate(this.factory.getComponentInstantiation(c));
				} catch (ComponentInstantiationFailedException e) {
					throw new ObjectEvaluationFailedException(e, "Evaluation of composition failed as the component instantiation could not be built.");
				}
			};

			IObjectEvaluator<ComponentInstance, Double> wrappedSelectionBenchmark = c -> {
				/* first conduct MCCV */
				AbstractEvaluatorMeasureBridge<Double, Double> bridge = this.evaluationMeasurementBridge;
				if (this.evaluationMeasurementBridge instanceof CacheEvaluatorMeasureBridge) {
					bridge = ((CacheEvaluatorMeasureBridge) bridge).getShallowCopy(c);

				}

				MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(bridge, MLPlanWekaClassifier.this.getConfig().numberOfMCIterationsDuringSelection(), MLPlanWekaClassifier.this.getInput(),
						MLPlanWekaClassifier.this.getConfig().getMCCVTrainFoldSizeDuringSelection(), this.getConfig().randomSeed());
				try {
					mccv.evaluate(this.factory.getComponentInstantiation(c));
				} catch (ComponentInstantiationFailedException e) {
					throw new ObjectEvaluationFailedException(e, "Evaluation of composition failed as the component instantiation could not be built.");
				}

				/* now retrieve .75-percentile from stats */
				double mean = mccv.getStats().getMean();
				double percentile = mccv.getStats().getPercentile(75f);
				MLPlanWekaClassifier.this.logger.info("Select {} as .75-percentile where {} would have been the mean. Samples size of MCCV was {}", percentile, mean, mccv.getStats().getN());
				return percentile;
			};

			try {
				TwoPhaseSoftwareConfigurationProblem problem = new TwoPhaseSoftwareConfigurationProblem(this.componentFile, "AbstractClassifier", wrappedSearchBenchmark, wrappedSelectionBenchmark);

				/* configure and start optimizing factory */
				OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(this.factory, problem);
				this.hascoFactory = new TwoPhaseHASCOFactory();
				this.hascoFactory.setPreferredNodeEvaluator(new AlternativeNodeEvaluator<TFDNode, Double>(this.getSemanticNodeEvaluator(this.dataShownToSearch), this.preferredNodeEvaluator));
				this.hascoFactory.setConfig(this.getConfig());
				this.optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, this.hascoFactory);
				logger.info("Setting logger directive of {} to {}", this.optimizingFactory, this.loggerName + ".2phasehasco");
				this.optimizingFactory.setLoggerName(this.loggerName + ".2phasehasco");
				this.optimizingFactory.setTimeout(getTimeout());
				this.optimizingFactory.registerListener(this);
				this.optimizingFactory.init();

				/* set state to active */
				return activate();
			} catch (IOException e) {
				throw new AlgorithmException(e, "Could not create TwoPhase configuration problem.");
			}
		}
		case active: {
			
			/* train the classifier returned by the optimizing factory */
			long startOptimizationTime = System.currentTimeMillis();
			this.selectedClassifier = this.optimizingFactory.call();
			this.internalValidationErrorOfSelectedClassifier = this.optimizingFactory.getPerformanceOfObject();
			long startBuildTime = System.currentTimeMillis();
			try {
				this.selectedClassifier.buildClassifier(this.getInput());
			} catch (Exception e) {
				throw new AlgorithmException(e, "Training the classifier failed!");
			}
			long endBuildTime = System.currentTimeMillis();
			this.logger.info("Selected model has been built on entire dataset. Build time of chosen model was {}ms. Total construction time was {}ms", endBuildTime - startBuildTime, endBuildTime - startOptimizationTime);
			return terminate();
		}
		default:
			throw new IllegalStateException("Cannot do anything in state " + this.getState());
		}

	}

	@Override
	public Classifier call() throws AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
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

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger.info("Switching logger name to {}", name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched ML-Plan logger to {}", name);

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
		this.registerListener(listener);
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
}