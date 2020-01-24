package ai.libs.jaicore.math.linearalgebra;

import java.util.function.DoubleFunction;

public class LinearlyScaledFunction implements DoubleFunction<Double> {

	private final DoubleFunction<Double> baseFunction;
	private final AffineFunction mapping;

	public LinearlyScaledFunction(final DoubleFunction<Double> baseFunction, final double x1, final double y1, final double x2, final double y2) {
		super();
		this.baseFunction = baseFunction;
		this.mapping = new AffineFunction(baseFunction.apply(x1), y1, baseFunction.apply(x2), y2);
	}

	@Override
	public Double apply(final double arg0) {
		double intermediate = this.baseFunction.apply(arg0);
		return this.mapping.applyAsDouble(intermediate);
	}

	public AffineFunction getMapping() {
		return this.mapping;
	}
}
