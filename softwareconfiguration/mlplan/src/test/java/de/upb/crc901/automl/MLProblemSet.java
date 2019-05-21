package de.upb.crc901.automl;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import jaicore.basic.sets.SetUtil.Pair;
import weka.core.converters.ConverterUtils.DataSource;

public abstract class MLProblemSet extends AAlgorithmTestProblemSet<Pair<DataSource, String>> {

	public MLProblemSet(final String name) {
		super("ML task " + name);
	}

	public abstract Pair<DataSource, String> getDatasetSource();
}
