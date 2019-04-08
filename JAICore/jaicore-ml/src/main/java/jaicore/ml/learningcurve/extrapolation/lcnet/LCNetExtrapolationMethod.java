package jaicore.ml.learningcurve.extrapolation.lcnet;

import jaicore.ml.interfaces.LearningCurve;
import jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;

/**
 * This class represents a learning curve extrapolation using the LCNet
 * from pybnn. It communicates with pybnn via the LCNetClient. If you use
 * if do not forget to call setConfigurations before you call 
 * extrapolateLearningCurveFromAnchorPoints, because the LCNet requires 
 * the configurations of you classifier 
 * 
 * @author noni4
 */

public class LCNetExtrapolationMethod implements LearningCurveExtrapolationMethod {
	
	private LCNetClient lcNet = new LCNetClient();
	private String identifier;
	private double[] configurations;
	
	public LCNetExtrapolationMethod(String identifier) {
		this.identifier = identifier;
		this.configurations = null;
	}
	
	public void setConfigurations(double[] configurations) {
		this.configurations = configurations;
	}

	@Override
	public LearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues, int dataSetSize)
			throws InvalidAnchorPointsException {
		if(this.configurations == null)
				throw new UnsupportedOperationException("Configurations is not allowed to be null");
		else
			return new PointWiseLearningCurve(dataSetSize, configurations, this.identifier);
	}
	
	//It is not clarified yet if this method should be called from this class
	public void deleteNet() {
		lcNet.deleteNet(this.identifier);
	}
	
	//It is not clarified yet if this method should be called from this class
	public void trainNet(int[] xValues, double[] yValues, int dataSetSize, double[][] configurations) {
		lcNet.train(xValues, yValues, dataSetSize, configurations, this.identifier);
	}
}
