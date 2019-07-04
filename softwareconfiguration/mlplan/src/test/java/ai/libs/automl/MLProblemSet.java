package ai.libs.automl;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.sets.Pair;
import weka.core.converters.ConverterUtils.DataSource;

public abstract class MLProblemSet extends AAlgorithmTestProblemSet<Pair<DataSource, String>> {

	public MLProblemSet(final String name) {
		super("ML task " + name);
	}

	public abstract Pair<DataSource, String> getDatasetSource();
}
