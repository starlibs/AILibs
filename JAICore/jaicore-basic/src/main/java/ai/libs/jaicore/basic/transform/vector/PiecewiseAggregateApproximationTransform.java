package ai.libs.jaicore.basic.transform.vector;

public class PiecewiseAggregateApproximationTransform implements IVectorTransform {

	private final int reducedLength;

	public PiecewiseAggregateApproximationTransform(final int reducedLength) {
		this.reducedLength = reducedLength;
	}

	@Override
	public double[] transform(final double[] input) {
		double[] ppa = new double[this.reducedLength];
		double n = input.length;
		for (int i = 0; i < this.reducedLength; i++) {
			double ppavalue = 0;
			for (int j = (int) (n / ((this.reducedLength * (i - 1)) + 1)); j < ((n / this.reducedLength) * i); j++) {
				ppavalue += input[j];
			}
			ppavalue = (this.reducedLength / n) * ppavalue;
			ppa[i] = ppavalue;
		}
		return ppa;
	}

}
