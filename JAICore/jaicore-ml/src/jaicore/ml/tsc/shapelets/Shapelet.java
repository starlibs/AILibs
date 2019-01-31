package jaicore.ml.tsc.shapelets;

import java.util.Arrays;
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
	public Shapelet(final double[] data, final int startIndex, final int length, final int instanceIndex,
			final double determinedQuality) {
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
		return data;
	}

	/**
	 * Getter for {@link Shapelet#length}.
	 * 
	 * @return Returns the shapelet's length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Getter for {@link Shapelet#startIndex}.
	 * 
	 * @return Returns the shapelet's start index.
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * Getter for {@link Shapelet#instanceIndex}.
	 * 
	 * @return Returns the shapelet's instance index.
	 */
	public int getInstanceIndex() {
		return instanceIndex;
	}

	/**
	 * Getter for {@link Shapelet#determinedQuality}.
	 * 
	 * @return Returns the shapelet's determined quality.
	 */
	public double getDeterminedQuality() {
		return determinedQuality;
	}

	/**
	 * Setter for {@link Shapelet#determinedQuality}.
	 * 
	 * @param determinedQuality
	 *            The new value to be set
	 */
	public void setDeterminedQuality(double determinedQuality) {
		this.determinedQuality = determinedQuality;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Shapelet) {
			Shapelet other = (Shapelet) obj;
			if (data == null && other.getData() != null || data != null && other.getData() == null)
				return false;

			return (data == null && other.getData() == null || Arrays.equals(this.data, other.getData()))
					&& length == other.getLength() && determinedQuality == other.determinedQuality
					&& instanceIndex == other.instanceIndex;
		}
		return super.equals(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Shapelet [data=" + Arrays.toString(data) + ", startIndex=" + startIndex + ", length=" + length
				+ ", instanceIndex=" + instanceIndex + ", determinedQuality=" + determinedQuality + "]";
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
}
