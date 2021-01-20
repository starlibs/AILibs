package ai.libs.jaicore.ml.hpo.ga.gene;

import java.util.Random;

public class RealGene implements INumericGene {

	private final double lowerBound;
	private final double upperBound;
	private double value;

	public RealGene(final double lowerBound, final double upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public RealGene(final double lowerBound, final double upperBound, final double value) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.value = value;
	}

	public RealGene(final double lowerBound, final double upperBound, final Random rand) {
		this(lowerBound, upperBound, lowerBound + rand.nextDouble() * (upperBound - lowerBound));
	}

	@Override
	public Double getValue() {
		return this.value;
	}

	@Override
	public void setValue(final Object value) {
		if (value == null || !(value instanceof Double)) {
			throw new IllegalArgumentException("Value must not be null and an instance of Double");
		}
		this.value = (Double) value;
	}

	@Override
	public Double getUpperBound() {
		return this.upperBound;
	}

	@Override
	public Double getLowerBound() {
		return this.lowerBound;
	}

	@Override
	public RealGene copy() {
		return new RealGene(this.lowerBound, this.upperBound, this.value);
	}

}
