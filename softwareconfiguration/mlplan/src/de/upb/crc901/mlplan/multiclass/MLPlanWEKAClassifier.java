package de.upb.crc901.mlplan.multiclass;

import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.automl.PreferenceBasedNodeEvaluator;
import de.upb.crc901.automl.hascowekaml.WEKAPipelineFactory;
import de.upb.crc901.mlplan.AbstractMLPlan;
import hasco.serialization.ComponentLoader;
import jaicore.basic.FileUtil;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;

public class MLPlanWEKAClassifier extends AbstractMLPlan implements Classifier {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = 7307103251631475620L;

	/**
	 * Config instance to make static properties available.
	 */
	private static final MLPlanWEKAClassifierConfig CONFIG = ConfigCache.getOrCreate(MLPlanWEKAClassifierConfig.class);

	/**
	 * Logger for controlled output.
	 */
	private Logger logger = LoggerFactory.getLogger(MLPlanWEKAClassifier.class);
	private String loggerName;

	public MLPlanWEKAClassifier() {
		super(CONFIG.componentFile());

		/* Check whether a component file is given and whether it is also available */
		if (CONFIG.componentFile() == null || !CONFIG.componentFile().exists()) {
			throw new IllegalArgumentException(
					"The file " + CONFIG.componentFile() + " is null or does not exist and cannot be used by ML-Plan");
		}

		/*
		 * set the classifier factory to be the WEKAPipelineFactory (using pipelines
		 * only from WEKA)
		 */
		this.setClassifierFactory(new WEKAPipelineFactory());
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		/*
		 * we allow CPUs-1 threads for node evaluation. Setting the timeout evaluator to
		 * null means to really prune all those
		 */
		if (CONFIG.numberOfCPUS() < 1) {
			throw new IllegalStateException("Cannot generate search where number of CPUs is " + CONFIG.numberOfCPUS());
		}

		this.logger.info("Starting ML-Plan with timeout {}s, and a portion of {} for the second phase.",
				CONFIG.timeoutInSeconds(), CONFIG.dataPortionForSelection());

		ComponentLoader componentLoader = new ComponentLoader();
		componentLoader.loadComponents(CONFIG.componentFile());

		/* split data set */
		Instances dataForSearch;
		Instances dataPreservedForSelection;
		if (CONFIG.dataPortionForSelection() > 0) {
			List<Instances> split = WekaUtil.realizeSplit(data,
					WekaUtil.getArbitrarySplit(data, new Random(CONFIG.seed()), CONFIG.dataPortionForSelection()));
			dataForSearch = split.get(1);
			dataPreservedForSelection = split.get(0);
			if (dataForSearch.isEmpty()) {
				throw new IllegalStateException(
						"Cannot search on no data and select on " + dataPreservedForSelection.size() + " data points.");
			}
		} else {
			dataForSearch = data;
			dataPreservedForSelection = null;
		}

		this.logger.info(
				"Creating search with a data split {}/{} for search/selection, which yields effectively a split of size:  {}/{}",
				1 - CONFIG.dataPortionForSelection(), CONFIG.dataPortionForSelection(), dataForSearch.size(),
				dataPreservedForSelection != null ? dataPreservedForSelection.size() : 0);

		this.setPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(componentLoader.getComponents(),
				FileUtil.readFileAsList(CONFIG.preferredComponents())));

		this.setClassifierEvaluatorForSearch(new MonteCarloCrossValidationEvaluator(
				new MulticlassEvaluator(new Random(CONFIG.seed())), CONFIG.numberOfMCIterationsDuringSearch(),
				dataForSearch, CONFIG.getMCCVTrainFoldSizeDuringSearch()));

		this.setSelectionPhaseEvaluator(new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(3)),
				CONFIG.numberOfMCIterationsDuringSelection(), data, CONFIG.getMCCVTrainFoldSizeDuringSelection()));

		/* set hardware resource limits */
		this.setMemory(CONFIG.memoryLimit());
		this.setNumberOfCPUs(CONFIG.numberOfCPUS());

		/* set ml plan specific run configurations */
		this.setTimeout(CONFIG.timeoutInSeconds());
		this.setTimeoutPerNodeFComputationInMS(CONFIG.timeoutPerNodeFComputation() * 1000);
		this.setPortionOfDataForPhase2(CONFIG.dataPortionForSelection());
		this.setNumberOfMCIterationsPerSolutionInSelectionPhase(CONFIG.numberOfMCIterationsDuringSelection());
		this.setNumberOfConsideredSolutions(CONFIG.numberOfConsideredSolutionDuringSelection());

		super.buildClassifier(data);
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		if (this.getSelectedClassifier() == null) {
			throw new IllegalStateException("Classifier has to be built successfully before usage.");
		}

		return this.getSelectedClassifier().classifyInstance(instance);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		if (this.getSelectedClassifier() == null) {
			throw new IllegalStateException("Classifier has to be built successfully before usage.");
		}

		return this.getSelectedClassifier().distributionForInstance(instance);
	}

	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
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
		for (int i = 0; i < options.length; i++) {
			switch (options[i].toLowerCase()) {
			case "-t": {
				this.setTimeout(Integer.parseInt(options[++i]));
				break;
			}
			case "-r": {
				this.setRandom(Integer.parseInt(options[++i]));
				break;
			}
			default: {
				throw new IllegalArgumentException("Unknown option " + options[i] + ".");
			}
			}
		}
	}

	@Override
	public String[] getOptions() {
		return null;
	}

}
