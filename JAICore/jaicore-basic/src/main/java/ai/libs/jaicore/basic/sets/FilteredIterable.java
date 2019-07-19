package ai.libs.jaicore.basic.sets;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class allows to iterate of any type of {@link Iterable} in a filtered way. More specifically, given a
 * whitelist of indices, this class allows to iterate only over the elements listed in that whitelist.
 * This util works for all kind of {@link Iterable}s. The basic idea is to avoid copying data for iterating
 * over subsets only.
 *
 * Note that this does only work for iterables with a fixed order, i.e., no HashSets!
 *
 * @author mwever
 *
 * @param <X> The type of elements to iterate over.
 */
public class FilteredIterable<X> implements Iterable<X> {

	private Iterable<X> wrappedIterable;
	private List<Integer> filteredIndices;

	/**
	 * Standard c'tor.
	 * @param wrappedIterable The iterable which is to be wrapped and filtered.
	 * @param filteredIndices The list of indices that are whitelisted and are expected to be returned when iterating over the iterable.
	 */
	public FilteredIterable(final Iterable<X> wrappedIterable, final List<Integer> filteredIndices) {
		this.wrappedIterable = wrappedIterable;
		this.filteredIndices = new LinkedList<>(filteredIndices);
		Collections.sort(this.filteredIndices);
	}

	@Override
	public Iterator<X> iterator() {
		return new FilteredIterator(this.wrappedIterable.iterator(), this.getFilteredIndices());
	}

	/**
	 * Getter for the list of filtered indices.
	 * @return The list of filtered indices.
	 */
	public List<Integer> getFilteredIndices() {
		return new LinkedList<>(this.filteredIndices);
	}

	/**
	 * The iterator which allows to iterate over the filtered subset only as described in the list of filtered indices.
	 *
	 * @author mwever
	 */
	private class FilteredIterator implements Iterator<X> {
		private final Iterator<X> wrappedIterator;
		private final List<Integer> remainingFilteredInstances;
		private int currentIndex;

		private FilteredIterator(final Iterator<X> wrappedIterator, final List<Integer> remainingFilteredInstances) {
			this.wrappedIterator = wrappedIterator;
			this.remainingFilteredInstances = remainingFilteredInstances;
			this.currentIndex = 0;
		}

		@Override
		public boolean hasNext() {
			if (this.remainingFilteredInstances.isEmpty()) {
				return false;
			}
			int nextIndex = this.remainingFilteredInstances.get(0);
			while (this.currentIndex < nextIndex) {
				this.wrappedIterator.next();
				this.currentIndex++;
			}
			return true;
		}

		@Override
		public X next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException("There is no element left!");
			}
			int extractedIndex = this.remainingFilteredInstances.remove(0);
			if (extractedIndex != this.currentIndex) {
				throw new IllegalStateException("Current index does not match the extracted index from the list. Extracted: " + extractedIndex);
			}
			this.currentIndex++;
			return this.wrappedIterator.next();
		}
	}

}
