package jaicore.ml.tsc.shapelets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a shapelet, i. e. a specific subsequence of a time series
 * representing a characteristic shape.
 *
 * @author Julian Lienen
 *
 */
public class Shapelet {
	/**
	 * The data vector of the shapelet.
	 */
	private double[] data;

	/**
	 * The start index of the shapelet in the origin time series.
	 */
	private int startIndex;

	/**
	 * The length of the shapelet.
	 */
	private int length;

	/**
	 * The instance index which is assigned when extracting the shapelet from a
	 * given time series.
	 */
	private int instanceIndex;

	/**
	 * The quality determined by an assessment function.
	 */
	private double determinedQuality;

	/**
	 * Constructs a shapelet specified by the given parameters.
	 *
	 * @param data
	 *            See {@link Shapelet#data}
	 * @param startIndex
	 *            See {@link Shapelet#startIndex}
	 * @param length
	 *            See {@link Shapelet#length}
	 * @param instanceIndex
	 *            See {@link Shapelet#instanceIndex}
	 * @param determinedQuality
	 *            See {@link Shapelet#determinedQuality}
	 */
	public Shapelet(final double[] data, final int startIndex, final int length, final int instanceIndex, final double determinedQuality) {
		this.data = data;
		this.startIndex = startIndex;
		this.length = length;
		this.instanceIndex = instanceIndex;
		this.determinedQuality = determinedQuality;
	}

	/**
	 * Constructs a shapelet specified by the given parameters.
	 *
	 * @param data
	 *            See {@link Shapelet#data}
	 * @param startIndex
	 *            See {@link Shapelet#startIndex}
	 * @param length
	 *            See {@link Shapelet#length}
	 * @param instanceIndex
	 *            See {@link Shapelet#instanceIndex}
	 */
	public Shapelet(final double[] data, final int startIndex, final int length, final int instanceIndex) {
		this.data = data;
		this.startIndex = startIndex;
		this.length = length;
		this.instanceIndex = instanceIndex;
	}

	/**
	 * Getter for {@link Shapelet#data}.
	 *
	 * @return Return the shapelet's data vector
	 */
	public double[] getData() {
		return this.data;
	}

	/**
	 * Getter for {@link Shapelet#length}.
	 *
	 * @return Returns the shapelet's length
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * Getter for {@link Shapelet#startIndex}.
	 *
	 * @return Returns the shapelet's start index.
	 */
	public int getStartIndex() {
		return this.startIndex;
	}

	/**
	 * Getter for {@link Shapelet#instanceIndex}.
	 *
	 * @return Returns the shapelet's instance index.
	 */
	public int getInstanceIndex() {
		return this.instanceIndex;
	}

	/**
	 * Getter for {@link Shapelet#determinedQuality}.
	 *
	 * @return Returns the shapelet's determined quality.
	 */
	public double getDeterminedQuality() {
		return this.determinedQuality;
	}

	/**
	 * Setter for {@link Shapelet#determinedQuality}.
	 *
	 * @param determinedQuality
	 *            The new value to be set
	 */
	public void setDeterminedQuality(final double determinedQuality) {
		this.determinedQuality = determinedQuality;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.data);
		long temp;
		temp = Double.doubleToLongBits(this.determinedQuality);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + this.instanceIndex;
		result = prime * result + this.length;
		result = prime * result + this.startIndex;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Shapelet) {
			Shapelet other = (Shapelet) obj;
			if (this.data == null && other.getData() != null || this.data != null && other.getData() == null) {
				return false;
			}

			return (this.data == null && other.getData() == null || Arrays.equals(this.data, other.getData())) && this.length == other.getLength() && this.determinedQuality == other.determinedQuality
					&& this.instanceIndex == other.instanceIndex;
		}
		return super.equals(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Shapelet [data=" + Arrays.toString(this.data) + ", startIndex=" + this.startIndex + ", length=" + this.length + ", instanceIndex=" + this.instanceIndex + ", determinedQuality=" + this.determinedQuality + "]";
	}

	/**
	 * Function sorting a list of shapelets in place by the length (ascending).
	 *
	 * @param shapelets
	 *            The list to be sorted in place.
	 */
	public static void sortByLengthAsc(final List<Shapelet> shapelets) {
		shapelets.sort((s1, s2) -> Integer.compare(s1.getLength(), s2.getLength()));
	}

	/**
	 * Returns the shapelet with the highest quality in the given list
	 * <code>shapelets</code>.
	 *
	 * @param shapelets
	 *            The list of shapelets which is evaluated
	 * @return Returns the shapelet with the highest determined quality
	 */
	public static Shapelet getHighestQualityShapeletInList(final List<Shapelet> shapelets) {
		return Collections.max(shapelets, (s1, s2) -> (-1) * Double.compare(s1.getDeterminedQuality(), s2.getDeterminedQuality()));
	}
}
