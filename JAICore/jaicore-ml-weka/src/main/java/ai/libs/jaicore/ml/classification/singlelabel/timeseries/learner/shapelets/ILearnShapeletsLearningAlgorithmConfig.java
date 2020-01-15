package ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.shapelets;

import ai.libs.jaicore.basic.IOwnerBasedRandomizedAlgorithmConfig;

public interface ILearnShapeletsLearningAlgorithmConfig extends IOwnerBasedRandomizedAlgorithmConfig {

	public static final String K_NUM_SHAPELETS = "numshapelets";
	public static final String K_LEARNINGRATE = "learningrate";
	public static final String K_REGULARIZATION = "regularization";
	public static final String K_SHAPELETLENGTH_MIN = "minshapeletlength";
	public static final String K_SHAPELETLENGTH_RELMIN = "relativeminshapeletlength";
	public static final String K_SCALER = "scaler";
	public static final String K_MAXITER = "maxiter";
	public static final String K_GAMMA = "gamma";
	public static final String K_ESTIMATEK = "estimatek";

	/**
	 * Parameter which determines how many of the most-informative shapelets should be used.
	 * Corresponds to K in the paper
	 */
	@Key(K_NUM_SHAPELETS)
	@DefaultValue("1")
	public int numShapelets();

	/**
	 * The learning rate used within the SGD.
	 */
	@Key(K_LEARNINGRATE)
	@DefaultValue("0.01")
	public double learningRate();

	/**
	 * The regularization used wihtin the SGD.
	 */
	@Key(K_REGULARIZATION)
	@DefaultValue("0.01")
	public double regularization();

	/**
	 * The minimum shapelet of the shapelets to be learned. Internally derived by
	 * the time series lengths and the <code>minShapeLengthPercentage</code>.
	 */
	@Key(K_SHAPELETLENGTH_MIN)
	public int minShapeletLength();

	/**
	 * The minimum shape length percentage used to calculate the minimum shape length.
	 */
	@Key(K_SHAPELETLENGTH_RELMIN)
	@DefaultValue("0.1")
	public double minShapeLengthPercentage();

	/**
	 * The number of scales used for the shapelet lengths.
	 */
	@Key(K_SCALER)
	@DefaultValue("2")
	public int scaleR();

	/**
	 * The maximum iterations used for the SGD.
	 */
	@Key(K_MAXITER)
	@DefaultValue("300")
	public int maxIterations();

	/**
	 * Gamma value used for momentum during gradient descent. Defaults to 0.5.
	 */
	@Key(K_GAMMA)
	@DefaultValue("0.5")
	public double gamma();

	/**
	 * Parameter indicator whether estimation of K (number of learned shapelets)
	 * should be derived from the number of total segments. False by default.
	 */
	@Key(K_ESTIMATEK)
	@DefaultValue("false")
	public boolean estimateK();
}
