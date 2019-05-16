package jaicore.ml.tsc.classifier;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/st-eval.properties" })
public interface ShapeletTransformExperimentConfig extends IMultiClassClassificationExperimentConfig {
	/**
	 * Shapelet Transform classifier parameters
	 */
	public static final String ST_K = "st.ks";
	public static final String ST_MIN_SHAPELET_LENGTH = "st.minShapeletLengths";
	public static final String ST_MAX_SHAPELET_LENGTH = "st.maxShapeletLengths";

	@Key(ST_K)
	public List<String> getSTKs();

	@Key(ST_MIN_SHAPELET_LENGTH)
	public List<String> getSTMinShapeletLengths();

	@Key(ST_MAX_SHAPELET_LENGTH)
	public List<String> getSTMaxShapeletLengths();
}
