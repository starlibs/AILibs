package ai.libs.jaicore.ml.weka.classification;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.dataset.DatasetDeriver;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;

public class WekaClassifierProblemSet implements IAlgorithmTestProblemSet<Pair<String, ILabeledDataset<ILabeledInstance>>> {

	private final String classifierClass;

	private static ILabeledDataset<ILabeledInstance> simpleDataset = null;
	private static ILabeledDataset<ILabeledInstance> difficultDataset = null;

	public WekaClassifierProblemSet(final String classifierClass) {
		super();
		this.classifierClass = classifierClass;
	}

	@Override
	public String getName() {
		return this.classifierClass;
	}

	@Override
	public Pair<String, ILabeledDataset<ILabeledInstance>> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		try {
			if (simpleDataset == null) {
				simpleDataset = OpenMLDatasetReader.deserializeDataset(30);
			}
			return new Pair<>(this.classifierClass, simpleDataset); // page-blocks
		} catch (DatasetDeserializationFailedException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public Pair<String, ILabeledDataset<ILabeledInstance>> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		int blowUpFactor = 400;

		if (difficultDataset == null) {
			ILabeledDataset<ILabeledInstance> ds = simpleDataset;
			if (ds == null) {
				ds = this.getSimpleProblemInputForGeneralTestPurposes().getY();
			}
			DatasetDeriver<ILabeledDataset<ILabeledInstance>> deriver = new DatasetDeriver<>(ds);
			deriver.addIndices(IntStream.range(0, ds.size()).boxed().collect(Collectors.toList()), blowUpFactor);
			try {
				difficultDataset = deriver.build();
			} catch (DatasetCreationException e) {
				throw new AlgorithmTestProblemSetCreationException(e);
			}
		}
		return new Pair<>(this.classifierClass, difficultDataset);
	}

	@Override
	public String toString() {
		return this.classifierClass;
	}
}
