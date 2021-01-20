package ai.libs.jaicore.ml.hpo.ga.gene;

import java.util.Random;

public class IntGene implements INumericGene {

	private final int lowerBound;
	private final int upperBound;
	private int value;

	/**
	 *
	 * @param lowerBound inclusive
	 * @param upperBound exclusive
	 */
	public IntGene(final int lowerBound, final int upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/**
	 *
	 * @param lowerBound inclusive
	 * @param upperBound exclusive
	 */
	public IntGene(final int lowerBound, final int upperBound, final Random rand) {
		this(lowerBound, upperBound, lowerBound + rand.nextInt(upperBound - lowerBound));
	}

	/**
	 *
	 * @param lowerBound inclusive
	 * @param upperBound exclusive
	 * @param value must lie in the interval [lowerBound, upperBound-1]
	 */
	public IntGene(final int lowerBound, final int upperBound, final int value) {
		this(lowerBound, upperBound);
		this.value = value;
	}

	@Override
	public void setValue(final Object value) {
		if (value == null || !(value instanceof Integer)) {
			throw new IllegalArgumentException("Value must not be null and it must be an integer.");
		}
		this.value = (Integer) value;
	}

	@Override
	public Integer getValue() {
		return this.value;
	}

	@Override
	public Integer getUpperBound() {
		return this.upperBound;
	}

	@Override
	public Integer getLowerBound() {
		return this.lowerBound;
	}

	@Override
	public IntGene copy() {
		return new IntGene(this.lowerBound, this.upperBound, this.value);
	}

	@Override
	public String toString() {
		return this.getValue() + "";
	}

}
