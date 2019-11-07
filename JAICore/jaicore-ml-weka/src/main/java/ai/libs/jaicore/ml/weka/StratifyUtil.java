package ai.libs.jaicore.ml.weka;

import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;

import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.core.Instances;

public class StratifyUtil {

	public static List<Instances> stratifiedSplit(final Instances data, final int seed, final double splits) throws SplitFailedException, InterruptedException {
		return WekaUtil.getStratifiedSplit(new WekaInstances(data), seed, splits).stream().map(x -> ((WekaInstances) x).getInstances()).collect(Collectors.toList());
	}

}
