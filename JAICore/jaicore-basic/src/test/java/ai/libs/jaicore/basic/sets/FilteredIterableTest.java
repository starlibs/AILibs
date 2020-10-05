package ai.libs.jaicore.basic.sets;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

/**
 * Test to verify the correct behavior of {@link FilteredIterable}.
 *
 * @author mwever
 */
public class FilteredIterableTest {

	private static final List<String> ORIGINAL_LIST = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
	private static final int NUM_RESHUFFLES = 10;

	@BeforeClass
	public static void setup() {

	}

	/**
	 * Test whether we can iterate over a filtered list of strings correctly.
	 */
	@Test
	public void testCorrectElementsIterated() {
		for (int numElements = 1; numElements <= ORIGINAL_LIST.size(); numElements++) {
			for (int shuffleSeed = 0; shuffleSeed < NUM_RESHUFFLES; shuffleSeed++) {
				List<String> listCopy = new LinkedList<>(ORIGINAL_LIST);
				Collections.shuffle(listCopy);

				List<Integer> filteredIndices = new LinkedList<>();
				for (int i = 0; i < numElements; i++) {
					filteredIndices.add(ORIGINAL_LIST.indexOf(listCopy.get(i)));
				}

				FilteredIterable<String> filteredIterable = new FilteredIterable<>(ORIGINAL_LIST, filteredIndices);
				List<Integer> sortedCopyOfFilteredIndices = new LinkedList<>(filteredIndices);
				Collections.sort(sortedCopyOfFilteredIndices);

				StringBuilder expected = new StringBuilder();
				StringBuilder actual = new StringBuilder();

				Iterator<String> filteredIterator = filteredIterable.iterator();
				for (Integer correctNextIndex : sortedCopyOfFilteredIndices) {
					actual.append(filteredIterator.next() + " ");
					expected.append(ORIGINAL_LIST.get(correctNextIndex) + " ");
				}

				assertEquals("The elements returned by the FilteredIterable are not correct.", expected.toString(), actual.toString());
			}
		}
	}

}
