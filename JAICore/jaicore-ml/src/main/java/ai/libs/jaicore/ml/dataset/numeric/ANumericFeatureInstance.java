package ai.libs.jaicore.ml.dataset.numeric;

import java.util.Arrays;
import java.util.Iterator;

import org.api4.java.ai.ml.dataset.INumericFeatureInstance;

public abstract class ANumericFeatureInstance implements INumericFeatureInstance {

	private final double[] features;

	protected ANumericFeatureInstance(final double[] features) {
		this.features = features;
	}

	@Override
	public Double get(final int pos) {
		return this.features[pos];
	}

	@Override
	public int getNumFeatures() {
		return this.features.length;
	}

	@Override
	public Iterator<Double> iterator() {
		return Arrays.stream(this.features).iterator();
	}

	@Override
	public double[] toDoubleVector() {
		return this.features;
	}

}
