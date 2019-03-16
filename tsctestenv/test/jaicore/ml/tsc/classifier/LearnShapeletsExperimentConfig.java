package jaicore.ml.tsc.classifier;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/ls-eval.properties" })
public interface LearnShapeletsExperimentConfig extends IMultiClassClassificationExperimentConfig {
	/**
	 * Learn Shapelets classifier parameters
	 */
	public static final String LS_K = "ls.ks";
	public static final String LS_LEARNING_RATE = "ls.learningRates";
	public static final String LS_REGULARIZATION = "ls.regularizations";
	public static final String LS_SCALE_R_ = "ls.scaleRs";
	public static final String LS_MIN_SHAPE_LENGTH = "ls.minShapeLengths";
	public static final String LS_MAX_ITERATIONS = "ls.maxIterations";

	@Key(LS_K)
	public List<String> getLSKs();

	@Key(LS_LEARNING_RATE)
	public List<String> getLSLearningRatess();

	@Key(LS_REGULARIZATION)
	public List<String> getLSRegularizations();

	@Key(LS_SCALE_R_)
	public List<String> getLSScaleRs();

	@Key(LS_MIN_SHAPE_LENGTH)
	public List<String> getLSMinShapeLengths();

	@Key(LS_MAX_ITERATIONS)
	public List<String> getLSMaxIterations();
}
