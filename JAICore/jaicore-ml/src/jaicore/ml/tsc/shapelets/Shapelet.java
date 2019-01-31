package jaicore.ml.tsc.shapelets;

import java.util.Arrays;
import java.util.List;

public class Shapelet {
	private double[] data;
	private int startIndex;
	private int length;
	private int instanceIndex;
	private double determinedQuality;

	public Shapelet(final double[] data, final int startIndex, final int length, final int instanceIndex,
			final double determinedQuality) {
		this.data = data;
		this.startIndex = startIndex;
		this.length = length;
		this.instanceIndex = instanceIndex;
		this.determinedQuality = determinedQuality;
	}

	public Shapelet(final double[] data, final int startIndex, final int length, final int instanceIndex) {
		this.data = data;
		this.startIndex = startIndex;
		this.length = length;
		this.instanceIndex = instanceIndex;
	}

	public double[] getData() {
		return data;
	}

	public int getLength() {
		return length;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getInstanceIndex() {
		return instanceIndex;
	}

	public double getDeterminedQuality() {
		return determinedQuality;
	}

	public void setDeterminedQuality(double determinedQuality) {
		this.determinedQuality = determinedQuality;
	}

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

	@Override
	public String toString() {
		return "Shapelet [data=" + Arrays.toString(data) + ", startIndex=" + startIndex + ", length=" + length
				+ ", instanceIndex=" + instanceIndex + ", determinedQuality=" + determinedQuality + "]";
	}

	public static void sortByLengthAsc(final List<Shapelet> shapelets) {
		shapelets.sort((s1, s2) -> Integer.compare(s1.getLength(), s2.getLength()));
	}
}
