package jaicore.ml.tsc.classifier;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/tsbf-eval.properties" })
public interface TimeSeriesBagOfFeaturesExperimentConfig extends IMultiClassClassificationExperimentConfig {
	/**
	 * Time series based-on Bag of Features classifier parameters
	 */
	public static final String TSBF_NUM_BINS = "tsbf.numBins";
	public static final String TSBF_NUM_FOLDS = "tsbf.numFolds";
	public static final String TSBF_Z_PROP = "tsbf.zProps";
	public static final String TSBF_MIN_INTERVAL_LENGTH = "tsbf.minIntervalLengths";

	@Key(TSBF_NUM_BINS)
	public List<String> getTSBFNumBins();

	@Key(TSBF_NUM_FOLDS)
	public List<String> getTSBFNumFolds();

	@Key(TSBF_Z_PROP)
	public List<String> getTSBFZProps();

	@Key(TSBF_MIN_INTERVAL_LENGTH)
	public List<String> getTSBFMinIntervalLengths();
}
