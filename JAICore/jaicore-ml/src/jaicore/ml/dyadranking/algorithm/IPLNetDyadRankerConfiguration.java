package jaicore.ml.dyadranking.algorithm;

import java.io.File;
import java.util.List;

import org.aeonbits.owner.Config.Sources;

import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;

@Sources({ "file:conf/plNet/plnet.properties" })
public interface IPLNetDyadRankerConfiguration extends IPredictiveModelConfiguration {

	public static final String K_PLNET_LEARNINGRATE = "plnet.learningrate";
	public static final String K_PLNET_HIDDEN_NODES = "plnet.hidden.nodes";
	public static final String K_PLNET_SEED = "plnet.hidden.nodes";
	public static final String K_ACTIVATION_FUNCTION = "plnet.hidden.activation.function";
	public static final String K_MAX_EPOCHS = "plnet.epochs";
	public static final String K_EARLY_STOPPING_INTERVAL = "plnet.early.stopping.interval";

	
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
	
	@Key(K_EARLY_STOPPING_INTERVAL)
	@DefaultValue("1")
	public int plNetEarlyStoppingInterval();
	
}
