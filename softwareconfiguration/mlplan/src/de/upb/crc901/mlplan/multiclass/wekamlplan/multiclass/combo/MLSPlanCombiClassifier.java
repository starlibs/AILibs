package de.upb.crc901.mlplan.multiclass.wekamlplan.multiclass.combo;
//package de.upb.crc901.mlplan.mlsplan.multiclass.combo;
//
//import java.io.IOException;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.Random;
//
//import org.aeonbits.owner.ConfigCache;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import de.upb.crc901.automl.PreferenceBasedNodeEvaluator;
//import de.upb.crc901.mlplan.multiclass.WekaMLPlanClassifier;
//import hasco.serialization.ComponentLoader;
//import hasco.variants.twophase.TwoPhaseHASCO;
//import jaicore.basic.FileUtil;
//import jaicore.basic.IObjectEvaluator;
//import jaicore.ml.WekaUtil;
//import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
//import jaicore.ml.evaluation.MulticlassEvaluator;
//import weka.classifiers.Classifier;
//import weka.core.Capabilities;
//import weka.core.Capabilities.Capability;
//import weka.core.CapabilitiesHandler;
//import weka.core.Instance;
//import weka.core.Instances;
//import weka.core.Option;
//import weka.core.OptionHandler;
//
//public class MLSPlanCombiClassifier extends TwoPhaseHASCO implements Classifier, CapabilitiesHandler, OptionHandler {
//
//	/** Logger for controlled output. */
//	private Logger logger = LoggerFactory.getLogger(WekaMLPlanClassifier.class);
//	private String loggerName;
//
//	private static final MLSPlanCombiClassifierConfig CONFIG = ConfigCache.getOrCreate(MLSPlanCombiClassifierConfig.class);
//
//	public MLSPlanCombiClassifier() throws IOException {
//		super(new ComponentLoader(CONFIG.componentsFile()));
//	}
//
//	@Override
//	public void buildClassifier(final Instances data) throws Exception {
//		if (CONFIG.cpus() < 1) {
//			throw new IllegalStateException("Cannot generate search where number of CPUs is " + this.getConfig().cpus());
//		}
//
//		this.logger.info("Starting ML-Plan with timeout {}s, and a portion of {} for the second phase.", this.getConfig().timeout(), this.getConfig().selectionDataPortion());
//
//		Instances dataForSearch;
//		Instances dataPreservedForSelection;
//		if (CONFIG.selectionDataPortion() > 0) {
//			List<Instances> selectionSplit = WekaUtil.getStratifiedSplit(data, new Random(this.getConfig().randomSeed()), this.getConfig().selectionDataPortion());
//			dataForSearch = selectionSplit.get(1);
//			dataPreservedForSelection = selectionSplit.get(0);
//		} else {
//			dataForSearch = data;
//			dataPreservedForSelection = null;
//		}
//
//		if (dataForSearch.isEmpty()) {
//			throw new IllegalStateException("Cannot search on no data and select on " + dataPreservedForSelection.size() + " data points.");
//		}
//
//		/* Set the classifier evaluator for the search. */
//		IObjectEvaluator<Classifier, Double> searchBenchmark = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(this.getConfig().randomSeed())), this.getConfig().searchMCIterations(), dataForSearch,
//				(float) this.getConfig().searchDataPortion());
//		super.setBenchmark(searchBenchmark);
//
//		/* Check whether selection phase is intended to be used and if so set the classifier evaluator for the selection phase */
//		if (dataPreservedForSelection != null) {
//			IObjectEvaluator<Classifier, Double> selectionBenchmark = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(this.getConfig().randomSeed())), this.getConfig().selectionMCIterations(), data,
//					(float) (1 - this.getConfig().selectionDataPortion()));
//			super.setSelectionPhaseEvaluator(selectionBenchmark);
//		}
//
//		this.logger.info("Creating search with a data split {}/{} for search/selection, which yields effectively a split of size:  {}/{}", 1 - this.getConfig().selectionDataPortion(), this.getConfig().selectionDataPortion(),
//				dataForSearch.size(), dataPreservedForSelection != null ? dataPreservedForSelection.size() : 0);
//
//		super.setFactory(new MLServicePipelineFactory());
//		super.setPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(super.getComponents(), FileUtil.readFileAsList(this.getConfig().componentsPrecedenceListFile())));
//
//		super.gatherSolutions();
//
//		try {
//			this.getSelectedClassifier().buildClassifier(data);
//		} catch (Exception e) {
//			this.logger.warn("The classifier could not be selected or could not be build on the entire training data.");
//		} finally {
//			this.cancel();
//		}
//	}
//
//	@Override
//	public double classifyInstance(final Instance instance) throws Exception {
//		if (this.getSelectedClassifier() == null) {
//			throw new IllegalStateException("Classifier has not been built yet.");
//		}
//
//		return this.getSelectedClassifier().classifyInstance(instance);
//	}
//
//	@Override
//	public double[] distributionForInstance(final Instance instance) throws Exception {
//		if (this.getSelectedClassifier() == null) {
//			throw new IllegalStateException("Classifier has not been built yet.");
//		}
//
//		return this.getSelectedClassifier().distributionForInstance(instance);
//	}
//
//	@Override
//	public Capabilities getCapabilities() {
//		Capabilities result = new Capabilities(this);
//		result.disableAll();
//
//		// attributes
//		result.enable(Capability.NOMINAL_ATTRIBUTES);
//		result.enable(Capability.NUMERIC_ATTRIBUTES);
//		result.enable(Capability.DATE_ATTRIBUTES);
//		result.enable(Capability.STRING_ATTRIBUTES);
//		result.enable(Capability.RELATIONAL_ATTRIBUTES);
//		result.enable(Capability.MISSING_VALUES);
//
//		// class
//		result.enable(Capability.NOMINAL_CLASS);
//		result.enable(Capability.NUMERIC_CLASS);
//		result.enable(Capability.DATE_CLASS);
//		result.enable(Capability.MISSING_CLASS_VALUES);
//
//		// instances
//		result.setMinimumNumberInstances(1);
//		return result;
//	}
//
//	@Override
//	public Enumeration<Option> listOptions() {
//		return null;
//	}
//
//	@Override
//	public void setOptions(final String[] options) throws Exception {
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
//	}
//
//	@Override
//	public String[] getOptions() {
//		return null;
//	}
//
//	@Override
//	public void setLoggerName(final String name) {
//		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
//		this.loggerName = name;
//		this.logger = LoggerFactory.getLogger(name);
//		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
//	}
//
//	@Override
//	public String getLoggerName() {
//		return this.loggerName;
//	}
//}
