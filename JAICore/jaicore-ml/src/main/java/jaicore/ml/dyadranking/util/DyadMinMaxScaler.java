package jaicore.ml.dyadranking.util;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
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
 * @author Michael Braun, Mirko Jürgens
 *
 */

public class DyadMinMaxScaler extends AbstractDyadScaler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1319262573945961139L;

	public void untransform(DyadRankingDataset dataset) {
		int lengthX = dataset.get(0).getDyadAtPosition(0).getInstance().length();
		int lengthY = dataset.get(0).getDyadAtPosition(0).getAlternative().length();

		if (lengthX != statsX.length || lengthY != statsY.length)
			throw new IllegalArgumentException("The scaler was fit to dyads with instances of length " + statsX.length
					+ " and alternatives of length " + statsY.length + "\n but received instances of length " + lengthX
					+ " and alternatives of length " + lengthY);

		untransformInstances(dataset);
		untransformAlternatives(dataset);
	}

	/**
	 * Undoes the transformation of the instances of each dyad.
	 * 
	 * @param dataset
	 */
	public void untransformInstances(DyadRankingDataset dataset) {
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				untransformInstance(dyad);
			}
		}
	}

	/**
	 * Undoes the transformation of the instances of each dyad.
	 * 
	 * @param dataset
	 * @param decimals number of decimal places for rounding
	 */
	public void untransformInstances(DyadRankingDataset dataset, int decimals) {
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				untransformInstance(dyad, decimals);
			}
		}
	}

	/**
	 * Undoes the transformation of the instance of a single dyad.
	 * 
	 * @param dyad
	 */
	public void untransformInstance(Dyad dyad) {
		int lengthX = dyad.getInstance().length();
		if (lengthX != statsX.length) {
			throw new IllegalArgumentException("The scaler was fit to instances of length " + statsX.length
					+ " but received an instance of length " + lengthX + ".");
		}
		for (int i = 0; i < lengthX; i++) {
			double value = dyad.getInstance().getValue(i);
			value *= statsX[i].getMax() - statsX[i].getMin();
			value += statsX[i].getMin();
			dyad.getInstance().setValue(i, value);
		}
	}
	
	/**
	 * Undoes the transformation of the instance of a single dyad.
	 * 
	 * @param dyad
	 * @param decimals number of decimal places for rounding
	 */
	public void untransformInstance(Dyad dyad, int decimals) {
		String pattern = "#.";
		for(int i = 0; i < decimals; i++)
			pattern += "#";
		DecimalFormat df = new DecimalFormat(pattern);
		int lengthX = dyad.getInstance().length();
		if (lengthX != statsX.length) {
			throw new IllegalArgumentException("The scaler was fit to instances of length " + statsX.length
					+ " but received an instance of length " + lengthX + ".");
		}
		for (int i = 0; i < lengthX; i++) {
			double value = dyad.getInstance().getValue(i);
			value *= statsX[i].getMax() - statsX[i].getMin();
			value += statsX[i].getMin();
			dyad.getInstance().setValue(i, Double.valueOf(df.format(value)));
		}
	}

	/**
	 * Undoes the transformation of the alternatives of each dyad.
	 * 
	 * @param dataset
	 */
	public void untransformAlternatives(DyadRankingDataset dataset) {
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				untransformAlternative(dyad);
			}
		}
	}

	/**
	 * Undoes the transformation of the alternatives of each dyad.
	 * 
	 * @param dataset
	 * @param decimals number of de
	 */
	public void untransformAlternatives(DyadRankingDataset dataset, int decimals) {
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			for (Dyad dyad : drInstance) {
				untransformAlternative(dyad, decimals);
			}
		}
	}

	/**
	 * Undoes the transformation on the alternative of a single dyad.
	 * 
	 * @param dyad
	 */
	public void untransformAlternative(Dyad dyad) {
		int lengthY = dyad.getAlternative().length();
		if (lengthY != statsY.length) {
			throw new IllegalArgumentException("The scaler was fit to alternatives of length " + statsY.length
					+ " but received an alternative of length " + lengthY + ".");
		}
		for (int i = 0; i < lengthY; i++) {
			double value = dyad.getAlternative().getValue(i);
			value *= statsY[i].getMax() - statsY[i].getMin();
			value += statsY[i].getMin();
			dyad.getAlternative().setValue(i, value);
		}
	}
	
	/**
	 * Undoes the transformation on the alternative of a single dyad.
	 * 
	 * @param dyad
	 */
	public void untransformAlternative(Dyad dyad, int decimals) {
		String pattern = "#.";
		for(int i = 0; i < decimals; i++)
			pattern += "#";
		DecimalFormat df = new DecimalFormat(pattern);
		int lengthY = dyad.getAlternative().length();
		if (lengthY != statsY.length) {
			throw new IllegalArgumentException("The scaler was fit to alternatives of length " + statsY.length
					+ " but received an alternative of length " + lengthY + ".");
		}
		for (int i = 0; i < lengthY; i++) {
			double value = dyad.getAlternative().getValue(i);
			value *= statsY[i].getMax() - statsY[i].getMin();
			value += statsY[i].getMin();
			dyad.getAlternative().setValue(i, Double.valueOf(df.format(value)));
			
		}
	}

	/**
	 * Prints the maxima of all features this scaler has been fit to.
	 */
	public void printMaxima() {
		if (statsX == null || statsY == null)
			throw new IllegalStateException("The scaler must be fit before calling this method!");
		System.out.print("Standard deviations for instances: ");
		for (SummaryStatistics stats : statsX) {
			System.out.print(stats.getMax() + ", ");
		}
		System.out.println();
		System.out.print("Standard deviations for alternatives: ");
		for (SummaryStatistics stats : statsY) {
			System.out.print(stats.getMax() + ", ");
		}
		System.out.println();
	}

	/**
	 * Prints the minima of all features this scaler has been fit to.
	 */
	public void printMinima() {
		if (statsX == null || statsY == null)
			throw new IllegalStateException("The scaler must be fit before calling this method!");
		System.out.print("Means for instances: ");
		for (SummaryStatistics stats : statsX) {
			System.out.print(stats.getMin() + ", ");
		}
		System.out.println();
		System.out.print("Means for alternatives: ");
		for (SummaryStatistics stats : statsY) {
			System.out.print(stats.getMin() + ", ");
		}
		System.out.println();
	}

	@Override
	public void transformInstances(Dyad dyad, List<Integer> ignoredIndices) {
		for (int i = 0; i < dyad.getInstance().length(); i++) {
			double value = dyad.getInstance().getValue(i);
			value -= statsX[i].getMin();
			// prevent division by zero
			if ((statsX[i].getMax() - statsX[i].getMin()) != 0)
				value /= statsX[i].getMax() - statsX[i].getMin();
			dyad.getInstance().setValue(i, value);
		}
	}

	@Override
	public void transformAlternatives(Dyad dyad, List<Integer> ignoredIndices) {
		for (int i = 0; i < dyad.getAlternative().length(); i++) {
			if (!ignoredIndices.contains(i)) {
				double value = dyad.getAlternative().getValue(i);
				value -= statsY[i].getMin();
				// prevent division by zero
				if ((statsY[i].getMax() - statsY[i].getMin()) != 0)
					value /= statsY[i].getMax() - statsY[i].getMin();
				dyad.getAlternative().setValue(i, value);
			}
		}
	}

	@Override
	public void transformInstaceVector(Vector vector, List<Integer> ignoredIndices) {
		for (int i = 0; i < vector.length(); i++) {
			double value = vector.getValue(i);
			value -= statsX[i].getMin();
			// prevent division by zero
			if ((statsX[i].getMax() - statsX[i].getMin()) != 0)
				value /= statsX[i].getMax() - statsX[i].getMin();
			vector.setValue(i, value);
		}
	}

}
