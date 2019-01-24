package jaicore.ml.dyadranking.algorithm;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;

@Sources({ "file:conf/plNet/plnet.properties" })
public interface IPLNetDyadRankerConfiguration extends IPredictiveModelConfiguration {
	/**
	 * The learning rate for the gradient updater.
	 */
	public static final String K_PLNET_LEARNINGRATE = "plnet.learningrate";
	/**
	 * List of integers describing the architecture of the hidden layers. The i-th
	 * element represents the number of units in the i-th hidden layer.
	 */
	public static final String K_PLNET_HIDDEN_NODES = "plnet.hidden.nodes";
	/**
	 * The random seed to use.
	 */
	public static final String K_PLNET_SEED = "plnet.seed";
	/**
	 * The activation function for the hidden layers. For a list of supported
	 * functions, see <a href=
	 * "https://deeplearning4j.org/docs/latest/deeplearning4j-cheat-sheet#config-afn">https://deeplearning4j.org/docs/latest/deeplearning4j-cheat-sheet#config-afn</a>
	 */
	public static final String K_ACTIVATION_FUNCTION = "plnet.hidden.activation.function";
	/**
	 * The maximum number of epochs to be used during training, i.e. how many times
	 * the training algorithm should iterate through the entire training data set.
	 * Set to 0 for no limit apart from early stopping.
	 */
	public static final String K_MAX_EPOCHS = "plnet.epochs";
	/**
	 * The size of mini batches used during training.
	 */
	public static final String K_MINI_BATCH_SIZE = "plnet.minibatch.size";
	/**
	 * How often (in epochs) the validation error should be checked for early
	 * stopping.
	 */
	public static final String K_EARLY_STOPPING_INTERVAL = "plnet.early.stopping.interval";
	/**
	 * For how many epochs early stopping should wait until training is stopped if
	 * no improvement in the validation error is observed.
	 */
	public static final String K_EARLY_STOPPING_PATIENCE = "plnet.early.stopping.patience";

	/**
	 * The ratio of data used for training in early stopping. 1 - this ratio is used
	 * for testing.
	 */
	public static final String K_EARLY_STOPPING_TRAIN_RATIO = "plnet.early.stopping.train.ratio";
	
	/**
	 * Whether to retrain on the full training data after early stopping, using the same number of epochs
	 * the model was trained for before early stopping occured.
	 */
	public static final String K_EARLY_STOPPING_RETRAIN = "plnet.early.stopping.retrain";

	@Key(K_PLNET_LEARNINGRATE)
	@DefaultValue("0.1")
	public double plNetLearningRate();

	@Key(K_PLNET_HIDDEN_NODES)
	@DefaultValue("8")
	public List<Integer> plNetHiddenNodes();

	@Key(K_PLNET_SEED)
	@DefaultValue("42")
	public int plNetSeed();

	@Key(K_ACTIVATION_FUNCTION)
	@DefaultValue("SIGMOID")
	public String plNetActivationFunction();

	@Key(K_MAX_EPOCHS)
	@DefaultValue("25")
	public int plNetMaxEpochs();
	
	@Key(K_MINI_BATCH_SIZE)
	@DefaultValue("4")
	public int plNetMiniBatchSize();

	@Key(K_EARLY_STOPPING_INTERVAL)
	@DefaultValue("1")
	public int plNetEarlyStoppingInterval();

	@Key(K_EARLY_STOPPING_PATIENCE)
	@DefaultValue("10")
	public int plNetEarlyStoppingPatience();
	
	@Key(K_EARLY_STOPPING_TRAIN_RATIO)
	@DefaultValue("0.8")
	public double plNetEarlyStoppingTrainRatio();
	
	@Key(K_EARLY_STOPPING_RETRAIN)
	@DefaultValue("true")
	public boolean plNetEarlyStoppingRetrain();

}
