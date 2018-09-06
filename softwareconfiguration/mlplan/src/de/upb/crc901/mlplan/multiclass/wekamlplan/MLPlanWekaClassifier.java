package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
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
import hasco.variants.twophase.TwoPhaseHASCO;
import hasco.variants.twophase.TwoPhaseHASCOFactory;
import hasco.variants.twophase.TwoPhaseSoftwareConfigurationProblem;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.MathExt;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.BasicMLEvaluator;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
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
	private static final String REQUESTED_INTERFACE = "weka.classifiers.bayes.BayesNet"; // "AbstractClassifier";

	private final File componentFile;
	private final Collection<Component> components;
	private final ClassifierFactory factory;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private final BasicMLEvaluator benchmark;
	private final MLPlanClassifierConfig config;
	private Classifier selectedClassifier;
	private final EventBus eventBus = new EventBus();
	private TwoPhaseHASCOFactory hascoFactory;
	private OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactory;

	private AlgorithmState state = AlgorithmState.created;
	private Instances dataShownToSearch = null;
	private Instances data = null;

	public MLPlanWekaClassifier(File componentFile, ClassifierFactory factory, BasicMLEvaluator benchmark, MLPlanClassifierConfig config) throws IOException {
		this.componentFile = componentFile;
		this.components = new ComponentLoader(componentFile).getComponents();
		this.factory = factory;
		this.benchmark = benchmark;
		this.config = config;
	}

	@Override
	public boolean hasNext() {
		return state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			switch (state) {
			case created: {

				/* check whether data has been set */
				if (data == null)
					throw new IllegalArgumentException("Data to work on is still NULL");

				/* check number of CPUs assigned */
				if (config.cpus() < 1) {
					throw new IllegalStateException("Cannot generate search where number of CPUs is " + config.cpus());
				}

				/* set up exact splits */
				float selectionDataPortion = config.dataPortionForSelection();
				if (selectionDataPortion > 0) {
					List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(data, new Random(config.randomSeed()), selectionDataPortion);
					dataShownToSearch = selectionSplit.get(1);
				} else {
					dataShownToSearch = data;
				}
				if (dataShownToSearch.isEmpty()) {
					throw new IllegalStateException("Cannot search on no data.");
				}

				/* dynamically compute blow-ups */
				double blowUpInSelectionPhase = MathExt.round(1f / config.getMCCVTrainFoldSizeDuringSearch() * config.numberOfMCIterationsDuringSelection() / config.numberOfMCIterationsDuringSearch(),
						2);
				double blowUpInPostprocessing = MathExt.round((1 / (1 - config.dataPortionForSelection())) / config.numberOfMCIterationsDuringSelection(), 2);
				config.setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
				config.setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));

				/* communicate the parameters with which ML-Plan will run */
				this.logger.info(
						"Starting ML-Plan with {} CPUs and a timeout of {}s. The portion for the second phase is {}, evaluation is {}-{}-MCCV during search and {}-{}-MCCV in selection. Blow-ups are {} for selection phase and {} for post-processing phase.",
						config.cpus(), config.timeout(), config.dataPortionForSelection(), config.numberOfMCIterationsDuringSearch(), (int) (100 * config.getMCCVTrainFoldSizeDuringSearch()),
						config.numberOfMCIterationsDuringSelection(), (int) (100 * config.getMCCVTrainFoldSizeDuringSelection()), config.expectedBlowupInSelection(),
						config.expectedBlowupInPostprocessing());
				this.logger.info("Using the following preferred node evaluator: {}", preferredNodeEvaluator);

				/* create HASCO problem */
				IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(benchmark, config.numberOfMCIterationsDuringSearch(), dataShownToSearch,
						config.getMCCVTrainFoldSizeDuringSearch());
				IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> searchBenchmark.evaluate(factory.getComponentInstantiation(c));
				IObjectEvaluator<Classifier, Double> selectionBenchmark = new IObjectEvaluator<Classifier, Double>() {

					@Override
					public Double evaluate(Classifier object) throws Exception {

						/* first conduct MCCV */
						MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(benchmark, config.numberOfMCIterationsDuringSelection(), data,
								config.getMCCVTrainFoldSizeDuringSelection());
						mccv.evaluate(object);

						/* now retrieve .75-percentile from stats */
						double mean = mccv.getStats().getMean();
						double percentile = mccv.getStats().getPercentile(75f);
						logger.info("Select {} as .75-percentile where {} would have been the mean. Samples size of MCCV was {}", percentile, mean, mccv.getStats().getN());
						return percentile;
					}
				};
				IObjectEvaluator<ComponentInstance, Double> wrappedSelectionBenchmark = c -> selectionBenchmark.evaluate(factory.getComponentInstantiation(c));
				TwoPhaseSoftwareConfigurationProblem problem = new TwoPhaseSoftwareConfigurationProblem(componentFile, "AbstractClassifier", wrappedSearchBenchmark, wrappedSelectionBenchmark);
				
				/* configure and start optimizing factory */
				OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(factory, problem);
				hascoFactory = new TwoPhaseHASCOFactory();
				hascoFactory.setPreferredNodeEvaluator(new AlternativeNodeEvaluator<TFDNode, Double>(getSemanticNodeEvaluator(dataShownToSearch), preferredNodeEvaluator));
				hascoFactory.setConfig(config);
				optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, hascoFactory);
				optimizingFactory.setLoggerName(loggerName + ".2phasehasco");
				optimizingFactory.setTimeout(config.timeout(), TimeUnit.SECONDS);
				optimizingFactory.registerListener(this);
				optimizingFactory.init();

				/* set state to active */
				state = AlgorithmState.active;
				return new AlgorithmInitializedEvent();
			}
			case active: {

				/* train the classifier returned by the optimizing factory */
				selectedClassifier = optimizingFactory.call();
				long startBuildTime = System.currentTimeMillis();
				selectedClassifier.buildClassifier(data);
				long endBuildTime = System.currentTimeMillis();
				logger.info("Selected model has been built on entire dataset. Build time was {}ms", endBuildTime - startBuildTime);
				state = AlgorithmState.inactive;
				return new AlgorithmFinishedEvent();
			}
			default:
				throw new IllegalStateException("Cannot do anything in state " + state);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Classifier call() throws Exception {
		while (hasNext())
			next();
		return this;
	}

	@Override
	public Instances getInput() {
		return data;
	}

	@Override
	public void registerListener(Object listener) {
		optimizingFactory.registerListener(listener);
	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TimeUnit getTimeoutUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

	protected abstract INodeEvaluator<TFDNode, Double> getSemanticNodeEvaluator(Instances data);

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		this.setData(data);
		call();
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

	public void setPortionOfDataForPhase2(float portion) {
		config.setProperty(MLPlanClassifierConfig.SELECTION_PORTION, String.valueOf(portion));
	}

	public void setTimeout(int seconds) {
		config.setProperty(MLPlanClassifierConfig.K_TIMEOUT, String.valueOf(seconds));
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

	public void setTimeoutForSingleSolutionEvaluation(int timeout) {
		config.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeout * 1000));
	}

	public void setTimeoutForNodeEvaluation(int timeout) {
		config.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeout * 1000));
	}

	public Collection<Component> getComponents() {
		return Collections.unmodifiableCollection(this.components);
	}

	public void setRandomSeed(int seed) {
		config.setProperty(MLPlanClassifierConfig.K_RANDOM_SEED, String.valueOf(seed));
	}

	public void setNumCPUs(int num) {
		if (num < 1)
			throw new IllegalArgumentException("Need to work with at least one CPU");
		}
		if (num > Runtime.getRuntime().availableProcessors()) {
			this.logger.warn("Warning, configuring {} CPUs where the system has only {}", num, Runtime.getRuntime().availableProcessors());
		}
		this.config.setProperty(MLPlanClassifierConfig.K_CPUS, String.valueOf(num));
	}

	@Subscribe
	public void receiveSolutionEvent(SolutionCandidateFoundEvent<HASCOSolutionCandidate<Double>> event) {
		HASCOSolutionCandidate<Double> solution = event.getSolutionCandidate();
		try {
			logger.info("Received new solution {} with score {} and evaluation time {}ms", factory.getComponentInstantiation(solution.getComponentInstance()), solution.getScore(),
					solution.getTimeToComputeScore());
		} catch (Exception e) {
			e.printStackTrace();
		}
		eventBus.post(event);
	}

	public void registerListenerForSolutionEvaluations(Object listener) {
		eventBus.register(listener);
	}

	public Classifier getSelectedClassifier() {
		return this.selectedClassifier;
	}

	public GraphGenerator<TFDNode, String> getGraphGenerator() {
		if (state == AlgorithmState.created)
			init();
		TwoPhaseHASCO twoPhaseHASCO = ((TwoPhaseHASCO) optimizingFactory.getOptimizer());
		return twoPhaseHASCO.getGraphGenerator();
	}

	public void setData(Instances data) {
		this.data = data;
	}

	public Instances getData() {
		return data;
	}
	
	public AlgorithmInitializedEvent init() {
		AlgorithmEvent e = null;
		while (hasNext()) {
			e = next();
			if (e instanceof AlgorithmInitializedEvent)
				return (AlgorithmInitializedEvent)e;
		}
		throw new IllegalStateException("Could not complete initialization");
	}
}
