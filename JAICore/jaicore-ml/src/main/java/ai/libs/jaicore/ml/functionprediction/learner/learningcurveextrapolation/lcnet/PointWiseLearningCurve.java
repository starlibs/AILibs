package ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.lcnet;

import org.api4.java.ai.ml.core.evaluation.learningcurve.ILearningCurve;
import org.api4.java.ai.ml.core.exception.PredictionException;

/**
 * This class represents a learning curve that gets returned by the
 * LCNet from pybnn
 *
 * @author noni4
 */

public class PointWiseLearningCurve implements ILearningCurve {

	private int dataSetSize;
	private double[] configurations;

	private LCNetClient lcNetClient = new LCNetClient();
	private String identifier;

	public PointWiseLearningCurve(final int dataSetSize, final double[] configurations, final String identifier) {
		this.dataSetSize = dataSetSize;
		this.configurations = configurations;
		this.identifier = identifier;
	}

	@Override
	public double getCurveValue(final double x) {
		try {
			return this.lcNetClient.predict((int) (x / this.dataSetSize), this.configurations, this.identifier);
		} catch (PredictionException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
