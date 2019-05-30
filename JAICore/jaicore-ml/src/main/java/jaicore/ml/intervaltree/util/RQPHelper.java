package jaicore.ml.intervaltree.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class RQPHelper {

	private RQPHelper() {
		// prevent instantiation of this util class.
	}

	/**
	 * Maps the WEKA query to a tree-friendly query while also preserving the header
	 * information of the query, this is important for M5 trees.
	 *
	 * @param data
	 * @return
	 */
	public static final IntervalAndHeader mapWEKAToTree(final Instance data) {
		Interval[] mappedData = new Interval[data.numAttributes() / 2];
		int counter = 0;
		ArrayList<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute("bias"));
		for (int attrNum = 0; attrNum < data.numAttributes(); attrNum = attrNum + 2) {
			mappedData[counter] = new Interval(data.value(attrNum), data.value(attrNum + 1));
			attributes.add(new Attribute("xVal" + counter));
			counter++;
		}
		Instances header = new Instances("queriedInterval", attributes, 2);
		header.setClassIndex(-1);
		return new IntervalAndHeader(mappedData, header);

	}

	public static final Interval[] substituteInterval(final Interval[] original, final Interval toSubstitute, final int index) {
		Interval[] copy = Arrays.copyOf(original, original.length);
		copy[index] = toSubstitute;
		return copy;
	}

	public static final <T> Entry<Interval[], T> getEntry(final Interval[] interval, final T tree) {
		return new AbstractMap.SimpleEntry<>(interval, tree);
	}

	public static class IntervalAndHeader {
		private final Interval[] intervals;
		private final Instances headerInformation;

		public IntervalAndHeader(final Interval[] intervals, final Instances headerInformation) {
			super();
			this.intervals = intervals;
			this.headerInformation = headerInformation;
		}

		public Interval[] getIntervals() {
			return this.intervals;
		}

		public Instances getHeaderInformation() {
			return this.headerInformation;
		}

	}
}
