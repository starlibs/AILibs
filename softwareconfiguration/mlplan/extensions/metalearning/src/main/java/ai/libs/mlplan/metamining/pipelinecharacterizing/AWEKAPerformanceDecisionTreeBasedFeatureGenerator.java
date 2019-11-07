package ai.libs.mlplan.metamining.pipelinecharacterizing;

import java.util.ArrayList;
import java.util.Map;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.common.math.IVector;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * A {@link IPerformanceDecisionTreeBasedFeatureGenerator} that uses a WEKA
 * implementation of a decision tree.
 *
 * @author Helena Graf
 *
 */
public abstract class AWEKAPerformanceDecisionTreeBasedFeatureGenerator implements IPerformanceDecisionTreeBasedFeatureGenerator {

	@Override
	public void train(final Map<IVector, Double> intermediatePipelineRepresentationsWithPerformanceValues) throws TrainingException {
		// Step 1: Transform to Instances Object
		ArrayList<Attribute> attInfo = new ArrayList<>();
		for (int i = 0; i < intermediatePipelineRepresentationsWithPerformanceValues.keySet().toArray(new IVector[0])[0].length(); i++) {
			attInfo.add(new Attribute("Attribute-" + i));
		}
		attInfo.add(new Attribute("Target"));
		Instances train = new Instances("train", attInfo, intermediatePipelineRepresentationsWithPerformanceValues.size());
		train.setClassIndex(train.numAttributes() - 1);
		intermediatePipelineRepresentationsWithPerformanceValues.forEach((features, value) -> {
			double[] values = new double[features.length() + 1];
			for (int i = 0; i < features.length(); i++) {
				values[i] = features.getValue(i);
			}
			values[values.length - 1] = value;
			train.add(new DenseInstance(1, values));
		});

		try {
			this.train(train);
		} catch (AlgorithmException e) {
			throw new TrainingException("Could not train the " + this.getClass().getName() + ".", e);
		}
	}

	/**
	 * Constructs an internal decision tree based on the Instances object so that
	 * the feature generator can be used in the future to predict features for some
	 * new vector ({@link #predict(IVector)}).
	 *
	 * @param data
	 * @throws Exception
	 */
	public abstract void train(Instances data) throws AlgorithmException;

}