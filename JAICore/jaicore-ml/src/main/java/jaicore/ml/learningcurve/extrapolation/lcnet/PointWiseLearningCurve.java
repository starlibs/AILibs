package jaicore.ml.learningcurve.extrapolation.lcnet;

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
	
	public PointWiseLearningCurve(int dataSetSize, double[] configurations, String identifier) {
		this.dataSetSize = dataSetSize;
		this.configurations = configurations;
		this.identifier = identifier;
	}

	@Override
	public double getCurveValue(double x) {
		return this.lcNetClient.predict((int) (x/this.dataSetSize), this.configurations, this.identifier);
	}

}
