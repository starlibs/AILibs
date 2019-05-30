package jaicore.ml.dyadranking.util;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A scaler that can be fit to a certain dataset and then be used to normalize
 * dyad datasets, i.e. transform the data such that the values of each feature
 * lie between 0 and 1.
 *
 * For feature x: x = x - x_min / (x_max - x_min)
 *
 * @author Michael Braun, Mirko Jürgens, Helena Graf
 *
 */

public class DyadMinMaxScaler extends AbstractDyadScaler {

	/**
	 *
	 */
	private static final long serialVersionUID = -1319262573945961139L;

	public void untransform(final DyadRankingDataset dataset) {
		int lengthX = dataset.get(0).getDyadAtPosition(0).getInstance().length();
		int lengthY = dataset.get(0).getDyadAtPosition(0).getAlternative().length();

		if (lengthX != this.statsX.length || lengthY != this.statsY.length) {
			throw new IllegalArgumentException("The scaler was fit to dyads with instances of length " + this.statsX.length + " and alternatives of length " + this.statsY.length + "\n but received instances of length " + lengthX
					+ " and alternatives of length " + lengthY);
		}

		this.untransformInstances(dataset);
		this.untransformAlternatives(dataset);
	}

	/**
	 * Undoes the transformation of the instances of each dyad.
	 *
	 * @param dataset
	 */
	public void untransformInstances(final DyadRankingDataset dataset) {
		for (IDyadRankingInstance instance : dataset) {
			for (Dyad dyad : instance) {
				this.untransformInstance(dyad);
			}
		}
	}

	/**
	 * Undoes the transformation of the instances of each dyad.
	 *
	 * @param dataset
	 * @param decimals number of decimal places for rounding
	 */
	public void untransformInstances(final DyadRankingDataset dataset, final int decimals) {
		for (IDyadRankingInstance instance : dataset) {
			for (Dyad dyad : instance) {
				this.untransformInstance(dyad, decimals);
			}
		}
	}

	/**
	 * Undoes the transformation of the instance of a single dyad.
	 *
	 * @param dyad
	 */
	public void untransformInstance(final Dyad dyad) {
		int lengthX = dyad.getInstance().length();
		if (lengthX != this.statsX.length) {
			throw new IllegalArgumentException("The scaler was fit to instances of length " + this.statsX.length + " but received an instance of length " + lengthX + ".");
		}
		for (int i = 0; i < lengthX; i++) {
			double value = dyad.getInstance().getValue(i);
			value *= this.statsX[i].getMax() - this.statsX[i].getMin();
			value += this.statsX[i].getMin();
			dyad.getInstance().setValue(i, value);
		}
	}

	/**
	 * Undoes the transformation of the instance of a single dyad.
	 *
	 * @param dyad
	 * @param decimals number of decimal places for rounding
	 */
	public void untransformInstance(final Dyad dyad, final int decimals) {
		StringBuilder pattern = new StringBuilder();
		pattern.append("#.");
		for (int i = 0; i < decimals; i++) {
			pattern.append("#");
		}
		int lengthX = dyad.getInstance().length();
		if (lengthX != this.statsX.length) {
			throw new IllegalArgumentException("The scaler was fit to instances of length " + this.statsX.length + " but received an instance of length " + lengthX + ".");
		}
		DecimalFormat df = new DecimalFormat(pattern.toString());
		for (int i = 0; i < lengthX; i++) {
			double value = dyad.getInstance().getValue(i);
			value *= this.statsX[i].getMax() - this.statsX[i].getMin();
			value += this.statsX[i].getMin();
			dyad.getInstance().setValue(i, Double.valueOf(df.format(value)));
		}
	}

	/**
	 * Undoes the transformation of the alternatives of each dyad.
	 *
	 * @param dataset
	 */
	public void untransformAlternatives(final DyadRankingDataset dataset) {
		for (IDyadRankingInstance instance : dataset) {
			for (Dyad dyad : instance) {
				this.untransformAlternative(dyad);
			}
		}
	}

	/**
	 * Undoes the transformation of the alternatives of each dyad.
	 *
	 * @param dataset
	 * @param decimals number of de
	 */
	public void untransformAlternatives(final DyadRankingDataset dataset, final int decimals) {
		for (IDyadRankingInstance instance : dataset) {
			for (Dyad dyad : instance) {
				this.untransformAlternative(dyad, decimals);
			}
		}
	}

	/**
	 * Undoes the transformation on the alternative of a single dyad.
	 *
	 * @param dyad
	 */
	public void untransformAlternative(final Dyad dyad) {
		int lengthY = dyad.getAlternative().length();
		if (lengthY != this.statsY.length) {
			throw new IllegalArgumentException("The scaler was fit to alternatives of length " + this.statsY.length + " but received an alternative of length " + lengthY + ".");
		}
		for (int i = 0; i < lengthY; i++) {
			double value = dyad.getAlternative().getValue(i);
			value *= this.statsY[i].getMax() - this.statsY[i].getMin();
			value += this.statsY[i].getMin();
			dyad.getAlternative().setValue(i, value);
		}
	}

	/**
	 * Undoes the transformation on the alternative of a single dyad.
	 *
	 * @param dyad
	 */
	public void untransformAlternative(final Dyad dyad, final int decimals) {
		StringBuilder pattern = new StringBuilder();
		pattern.append("#.");
		for (int i = 0; i < decimals; i++) {
			pattern.append("#");
		}
		int lengthY = dyad.getAlternative().length();
		if (lengthY != this.statsY.length) {
			throw new IllegalArgumentException("The scaler was fit to alternatives of length " + this.statsY.length + " but received an alternative of length " + lengthY + ".");
		}
		DecimalFormat df = new DecimalFormat(pattern.toString());
		for (int i = 0; i < lengthY; i++) {
			double value = dyad.getAlternative().getValue(i);
			value *= this.statsY[i].getMax() - this.statsY[i].getMin();
			value += this.statsY[i].getMin();
			dyad.getAlternative().setValue(i, Double.valueOf(df.format(value)));

		}
	}

	/**
	 * Returns a String the maxima of all features this scaler has been fit to.
	 */
	public String getPrettyMaximaString() {
		if (this.statsX == null || this.statsY == null) {
			throw new IllegalStateException("The scaler must be fit before calling this method!");
		}

		StringBuilder builder = new StringBuilder();

		builder.append("Standard deviations for instances: ");
		for (SummaryStatistics stats : this.statsX) {
			builder.append(stats.getMax());
			builder.append(", ");
		}
		builder.append(System.lineSeparator());

		builder.append("Standard deviations for alternatives: ");
		for (SummaryStatistics stats : this.statsY) {
			builder.append(stats.getMax());
			builder.append(", ");
		}
		builder.append(System.lineSeparator());

		return builder.toString();
	}

	/**
	 * Returns a String for the minima of all features this scaler has been fit to.
	 */
	public String getPrettyMinimaString() {
		if (this.statsX == null || this.statsY == null) {
			throw new IllegalStateException("The scaler must be fit before calling this method!");
		}

		StringBuilder builder = new StringBuilder();

		builder.append("Means for instances: ");
		for (SummaryStatistics stats : this.statsX) {
			builder.append(stats.getMin());
			builder.append(", ");
		}
		builder.append(System.lineSeparator());

		builder.append("Means for alternatives: ");
		for (SummaryStatistics stats : this.statsY) {
			builder.append(stats.getMin());
			builder.append(", ");
		}
		builder.append(System.lineSeparator());

		return builder.toString();
	}

	@Override
	public void transformInstances(final Dyad dyad, final List<Integer> ignoredIndices) {
		for (int i = 0; i < dyad.getInstance().length(); i++) {
			double value = dyad.getInstance().getValue(i);
			value -= this.statsX[i].getMin();
			// prevent division by zero
			if ((this.statsX[i].getMax() - this.statsX[i].getMin()) != 0) {
				value /= this.statsX[i].getMax() - this.statsX[i].getMin();
			}
			dyad.getInstance().setValue(i, value);
		}
	}

	@Override
	public void transformAlternatives(final Dyad dyad, final List<Integer> ignoredIndices) {
		for (int i = 0; i < dyad.getAlternative().length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = dyad.getAlternative().getValue(i);
				value -= this.statsY[i].getMin();
				// prevent division by zero
				if ((this.statsY[i].getMax() - this.statsY[i].getMin()) != 0) {
					value /= this.statsY[i].getMax() - this.statsY[i].getMin();
				}
				dyad.getAlternative().setValue(i, value);
			}
		}
	}

	@Override
	public void transformInstaceVector(final Vector vector, final List<Integer> ignoredIndices) {
		for (int i = 0; i < vector.length(); i++) {
			double value = vector.getValue(i);
			value -= this.statsX[i].getMin();
			// prevent division by zero
			if ((this.statsX[i].getMax() - this.statsX[i].getMin()) != 0) {
				value /= this.statsX[i].getMax() - this.statsX[i].getMin();
			}
			vector.setValue(i, value);
		}
	}

}
