package jaicore.ml.learningcurve.extrapolation.lcnet;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.interfaces.LearningCurve;

/**
 * This class represents a learning curve that gets returned by the
 * LCNet from pybnn
 *
 * @author noni4
 */

public class PointWiseLearningCurve implements LearningCurve {

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
