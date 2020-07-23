package ai.libs.jaicore.ml.classification.loss;

import static ai.libs.jaicore.basic.StringUtil.postpaddedString;
import static ai.libs.jaicore.basic.StringUtil.prepaddedString;
import static ai.libs.jaicore.basic.StringUtil.spaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Given two equal-length lists/vectors of values, this class computes a confusion matrix
 * @author mwever
 *
 */
public class ConfusionMatrix {

	private static final String COL_SEP = " | ";
	private final List<Object> objectIndex;
	private int[][] matrixEntries;

	/**
	 * Constructor computing the confusion matrix based on the given equal-length lists expected and actual.
	 * @param expected The list of expected values.
	 * @param actual The list of actual values.
	 */
	public ConfusionMatrix(final List<?> expected, final List<?> actual) {
		if (expected.size() != actual.size()) {
			throw new IllegalArgumentException("The proivded lists must be of the same length.");
		}

		Set<Object> distinctClasses = new HashSet<>(expected);
		distinctClasses.addAll(actual);
		this.objectIndex = new ArrayList<>(distinctClasses);
		this.matrixEntries = new int[this.objectIndex.size()][this.objectIndex.size()];
		for (int i = 0; i < expected.size(); i++) {
			this.matrixEntries[this.objectIndex.indexOf(expected.get(i))][this.objectIndex.indexOf(actual.get(i))] += 1;
		}
	}

	/**
	 * Gets the order of all the occurring elements which also defines the index of an element.
	 * @return The index list of objects.
	 */
	public List<Object> getObjectIndex() {
		return this.objectIndex;
	}

	/**
	 * Returns the index of an object in the confusion matrix.
	 * @param object The object for which to retrieve the index.
	 * @return The index of the given object.
	 */
	public int getIndexOfObject(final Object object) {
		return this.objectIndex.indexOf(object);
	}

	/**
	 * Returns an integer matrix with counts of the confusions.
	 * @return The integer matrix counting all occurring confusions.
	 */
	public int[][] getConfusionMatrix() {
		return this.matrixEntries;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		// determine maximum cell width
		int cellWidth = Math.max(this.objectIndex.stream().mapToInt(x -> x.toString().length()).max().getAsInt(),
				Arrays.stream(this.matrixEntries).mapToInt(x -> Arrays.stream(x).map(y -> (y + "").length()).max().getAsInt()).max().getAsInt());

		// write table head row
		sb.append(spaces(cellWidth));
		this.objectIndex.stream().map(x -> COL_SEP + postpaddedString(x.toString(), cellWidth)).forEach(sb::append);
		sb.append("\n");
		sb.append(IntStream.range(0, cellWidth + (cellWidth + COL_SEP.length()) * this.objectIndex.size()).mapToObj(x -> "-").collect(Collectors.joining())).append("\n");

		// write content of the table
		for (int i = 0; i < this.objectIndex.size(); i++) {
			sb.append(postpaddedString(this.objectIndex.get(i).toString(), cellWidth));
			for (int j = 0; j < this.objectIndex.size(); j++) {
				sb.append(COL_SEP).append(prepaddedString(this.matrixEntries[i][j] + "", cellWidth));
			}
			sb.append("\n");
		}

		return sb.toString();
	}

}
