package de.upb.crc901.mlplan.metamining.dyadranking;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@Sources({ "file:conf/draco/dyadranking/nodeevaluator.properties" })
public interface DyadRankingBasedNodeEvaluatorConfig extends Mutable {

	/* The amount of top-ranked pipelines that are being evaluated. */
	public static final String NUM_EVALUATIONS_KEY = "numEval";

	/* The amount of random samples that are drawn in each f-value computation. */
	public static final String NUM_SAMPLES_KEY = "numSamples";

	/* The seed of the random completer. */
	public static final String SEED_KEY = "seed";

	public static final String PLNET_ZIP_KEY = "plnetPath";
	
	public static final String SCALER_SER_KEY= "scalerPath";

	public static final String LANDMARKERS_KEY = "landmarkers";

	public static final String LANDMARKERS_SAMPLE_SIZE_KEY = "landmarkerSampleSize";

	public static final String USE_LANDMARKERS = "useLandmarkers";

	/*
	 * Seocifies if the dad ranker should evaluate the top k pipelines, or, just
	 * return the score which is predicted by the PLNet
	 */
	public static final String USE_EVALUATIONS = "useEvaluations";

	@Key(NUM_EVALUATIONS_KEY)
	@DefaultValue("10")
	public int getNumberOfEvaluations();

	@Key(NUM_SAMPLES_KEY)
	@DefaultValue("20")
	public int getNumberOfRandomSamples();

	@Key(SEED_KEY)
	@DefaultValue("42")
	public int getSeed();

	@Key(PLNET_ZIP_KEY)
	@DefaultValue("resources/draco/plnet/final_plnet_minmax.zip")
	public String getPlNetPath();

	@Key(LANDMARKERS_KEY)
	@Separator(";")
	@DefaultValue("4; 8; 16")
	public int[] getLandmarkers();

	@Key(LANDMARKERS_SAMPLE_SIZE_KEY)
	@DefaultValue("10")
	public int getLandmarkerSampleSize();

	@Key(USE_LANDMARKERS)
	@DefaultValue("false")
	public boolean useLandmarkers();

	@Key(USE_EVALUATIONS)
	@DefaultValue("true")
	public boolean useEvaluations();

	@Key(SCALER_SER_KEY)
	@DefaultValue("resources/draco/plnet/minmaxscaler.ser")
	public String scalerPath();
}
