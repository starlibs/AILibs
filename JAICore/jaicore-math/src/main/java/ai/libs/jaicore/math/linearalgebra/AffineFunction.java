package ai.libs.jaicore.math.linearalgebra;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.ToDoubleFunction;

public class AffineFunction implements ToDoubleFunction<Number> {

	private final double a;
	private final double b;

	public AffineFunction(final double a, final double b) {
		super();
		this.a = a;
		this.b = b;
	}

	public AffineFunction(final BigDecimal x1, final BigDecimal y1, final BigDecimal x2, final BigDecimal y2) {
		this.a = y1.subtract(y2).doubleValue() / x1.subtract(x2).doubleValue();
		this.b = y1.subtract(x1.multiply(BigDecimal.valueOf(this.a))).doubleValue();
	}

	public AffineFunction(final double x1, final double y1, final double x2, final double y2) {
		if (x1 == x2) {
			throw new IllegalArgumentException("Cannot create an affine function from two points with the same x-choordinate " + x1 + ".");
		}
		this.a = (y1 - y2) / (x1 - x2);
		this.b = y1 - this.a * x1;
	}

	public double getA() {
		return this.a;
	}

	public double getB() {
		return this.b;
	}

	@Override
	public double applyAsDouble(final Number t) {
		if (t instanceof BigDecimal) {
			return ((BigDecimal) t).multiply(BigDecimal.valueOf(this.a)).add(BigDecimal.valueOf(this.b)).doubleValue();
		}
		else if (t instanceof BigInteger) {
			return new BigDecimal((BigInteger) t).multiply(BigDecimal.valueOf(this.a)).add(BigDecimal.valueOf(this.b)).doubleValue();
		}
		else if (t instanceof Integer) {
			return this.a * (Integer)t + this.b;
		}
		else {
			return this.a * (double)t + this.b;
		}
	}
}
