package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.model.ComponentInstance;
import hasco.optimizingfactory.OptimizingFactory;
import hasco.optimizingfactory.OptimizingFactoryProblem;
import hasco.variants.twophase.TwoPhaseHASCOFactory;
import hasco.variants.twophase.TwoPhaseSoftwareConfigurationProblem;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IObjectEvaluator;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.BasicMLEvaluator;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
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
public class WekaMLPlanClassifier implements Classifier, CapabilitiesHandler, OptionHandler, ILoggingCustomizable {

	/** Logger for controlled output. */
	private Logger logger = LoggerFactory.getLogger(WekaMLPlanClassifier.class);
	private String loggerName;

	private final File componentFile;
	private final ClassifierFactory factory;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private final BasicMLEvaluator benchmark;
	private final MLPlanClassifierConfig config;
	private Classifier selectedClassifier;
	
	public WekaMLPlanClassifier(File componentFile, ClassifierFactory factory, BasicMLEvaluator benchmark, MLPlanClassifierConfig config) {
		this.componentFile = componentFile;
		this.factory = factory;
		this.benchmark = benchmark;
		this.config = config;
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		if (config.cpus() < 1) {
			throw new IllegalStateException("Cannot generate search where number of CPUs is " + config.cpus());
		}

//		// Setup preferred node evaluator
//		this.setPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(super.getComponents(), FileUtil.readFileAsList(this.getConfig().componentsPrecedenceListFile())));
//		// Extend the functionality of the preferred node evaluator and enhance it by some additional knowledge.
//		this.setPreferredNodeEvaluator(new SemanticNodeEvaluator(this.getComponents(), data, this.getPreferredNodeEvaluator()));
		this.logger.info("Starting ML-Plan with {} CPUs and a timeout of {}s, and a portion of {} for the second phase.", config.cpus(), config.timeout(), config.dataPortionForSelection());

		float selectionDataPortion = .7f; // TODO: Configurable machen
		Instances dataForSearch;
		Instances dataPreservedForSelection;
		if (selectionDataPortion > 0) {
			List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(data, new Random(config.randomSeed()), .7f);
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
		IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(benchmark, config.numberOfMCIterationsDuringSearch(),
				dataForSearch, (float) selectionDataPortion);
		IObjectEvaluator<ComponentInstance, Double> wrappedSearchBenchmark = c -> searchBenchmark.evaluate(factory.getComponentInstantiation(c));
		IObjectEvaluator<Classifier, Double> selectionBenchmark = new MonteCarloCrossValidationEvaluator(benchmark, config.numberOfMCIterationsDuringSelection(), data,
					(float) (1 - selectionDataPortion));
		IObjectEvaluator<ComponentInstance, Double> wrappedSelectionBenchmark = c -> selectionBenchmark.evaluate(factory.getComponentInstantiation(c));
		TwoPhaseSoftwareConfigurationProblem problem = new TwoPhaseSoftwareConfigurationProblem(componentFile, "AbstractClassifier", wrappedSearchBenchmark, wrappedSelectionBenchmark);
		
		/* configure and start optimizing factory */
		OptimizingFactoryProblem<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactoryProblem = new OptimizingFactoryProblem<>(factory, problem);
		TwoPhaseHASCOFactory hascoFactory = new TwoPhaseHASCOFactory();
		hascoFactory.setPreferredNodeEvaluator(preferredNodeEvaluator);
		hascoFactory.setConfig(config);
		OptimizingFactory<TwoPhaseSoftwareConfigurationProblem, Classifier, Double> optimizingFactory = new OptimizingFactory<>(optimizingFactoryProblem, hascoFactory);
		optimizingFactory.setLoggerName(loggerName + ".2phasehasco");
		optimizingFactory.setTimeout(config.timeout(), TimeUnit.SECONDS);
		selectedClassifier = optimizingFactory.call();
		
		/* train the classifier returned by the optimizing factory */
		selectedClassifier.buildClassifier(data);
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		if (selectedClassifier == null) {
			throw new IllegalStateException("Classifier has not been built yet.");
		}
		return selectedClassifier.classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		if (selectedClassifier == null) {
			throw new IllegalStateException("Classifier has not been built yet.");
		}

		return selectedClassifier.distributionForInstance(instance);
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
//		for (int i = 0; i < options.length; i++) {
//			switch (options[i].toLowerCase()) {
//			case "-t": {
//				this.setTimeout(Integer.parseInt(options[++i]));
//				break;
//			}
//			case "-r": {
//				this.setRandom(Integer.parseInt(options[++i]));
//				break;
//			}
//			default: {
//				throw new IllegalArgumentException("Unknown option " + options[i] + ".");
//			}
//			}
//		}
	}

	@Override
	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoggerName(String name) {
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
		config.setProperty(MLPlanClassifierConfig.K_VISUALIZE, String.valueOf(true));
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	public INodeEvaluator<TFDNode, Double> getPreferredNodeEvaluator() {
		return preferredNodeEvaluator;
	}

	public void setPreferredNodeEvaluator(INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluator = preferredNodeEvaluator;
	}

	public MLPlanClassifierConfig getConfig() {
		return config;
	}

	public File getComponentFile() {
		return componentFile;
	}
	
	public void setTimeoutForSingleFEvaluation(int timeout) {
		
	}
}
