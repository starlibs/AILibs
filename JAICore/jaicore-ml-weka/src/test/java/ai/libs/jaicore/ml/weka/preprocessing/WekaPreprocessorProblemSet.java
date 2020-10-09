package ai.libs.jaicore.ml.weka.preprocessing;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.dataset.Dataset;
import ai.libs.jaicore.ml.core.dataset.DenseInstance;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;

public class WekaPreprocessorProblemSet implements IAlgorithmTestProblemSet<Pair<String, ILabeledDataset<ILabeledInstance>>> {

	private final String searcherClass;
	private final String evaluatorClass;

	public WekaPreprocessorProblemSet(final String searcherClass, final String evaluatorClass) {
		super();
		this.searcherClass = searcherClass;
		this.evaluatorClass = evaluatorClass;
	}

	@Override
	public String getName() {
		return this.searcherClass + "/" + this.evaluatorClass;
	}

	@Override
	public Pair<String, ILabeledDataset<ILabeledInstance>> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		try {
			return new Pair<>(this.getName(), OpenMLDatasetReader.deserializeDataset(30)); // page-blocks
		} catch (DatasetDeserializationFailedException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public Pair<String, ILabeledDataset<ILabeledInstance>> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		try {
			ILabeledDataset<ILabeledInstance> ds = OpenMLDatasetReader.deserializeDataset(1457); // amazon
			ILabeledInstanceSchema s = ds.getInstanceSchema().getCopy();
			int blowup = 10000;
			for (int i = 0; i < blowup; i++) {
				s.addAttribute(new NumericAttribute("a" + i));
			}
			ILabeledDataset<ILabeledInstance> dsBlownUp = new Dataset(s);
			for (ILabeledInstance i : ds) {
				double[] shortPoint = i.getPoint();
				Object[] point = new Object[shortPoint.length + blowup];
				for (int j = 0; j < shortPoint.length; j ++) {
					point[j] = shortPoint[j];
				}
				for (int j = 0; j < blowup; j++) {
					point[shortPoint.length + j] = Math.random();
				}
				dsBlownUp.add(new DenseInstance(point, i.getLabel()));
				dsBlownUp.add(new DenseInstance(point, i.getLabel()));
			}
			return new Pair<>(this.getName(), dsBlownUp);
		}
		catch (DatasetDeserializationFailedException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
