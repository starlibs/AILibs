package jaicore.ml.intervaltree.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import jaicore.ml.core.Interval;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class RQPHelper {

	/**
	 * Maps the WEKA query to a tree-friendly query while also preserving the header
	 * information of the query, this is important for M5 trees.
	 * 
	 * @param data
	 * @return
	 */
	public static final IntervalAndHeader mapWEKAToTree(Instance data) {
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
	
	public static final Interval[] substituteInterval(Interval[] original, Interval toSubstitute, int index) {
		Interval[] copy = Arrays.copyOf(original, original.length);
		copy[index] = toSubstitute;
		return copy;
	}
	
	public static final <T> Entry<Interval[], T> getEntry(Interval[] interval, T tree) {
		return new AbstractMap.SimpleEntry<>(interval, tree);
	}
	

	

	/**
	 * Prunes the range query to the features of this bagged random tree (i.e.
	 * removes the features that were not selected for this tree)Ï
	 * 
	 * @param rangeQuery
	 * @return
	 */
	public Instance pruneIntervals(Instance rangeQuery, Instances header) {
		System.out.println("Num attr header " + (header.numAttributes() - 1));
		System.out.println("Num attr range query " + (rangeQuery.numAttributes() / 2));
		for (int i = 0; i < header.numAttributes(); i++) {
			weka.core.Attribute attribute = header.attribute(i);
			// System.out.println("Attribute at pos "+ i + " has header "+
			// attribute.name());
		}
		return rangeQuery;
	}

	public static class IntervalAndHeader {
		private final Interval[] intervals;
		private final Instances headerInformation;

		public IntervalAndHeader(Interval[] intervals, Instances headerInformation) {
			super();
			this.intervals = intervals;
			this.headerInformation = headerInformation;
		}

		public Interval[] getIntervals() {
			return intervals;
		}

		public Instances getHeaderInformation() {
			return headerInformation;
		}

	}
}
