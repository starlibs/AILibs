package ai.libs.jaicore.basic.sets;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class allows to get a casted view on the items of a list.
 *
 * For example, if you have a list List<Number> l, it is not possible to cast l to List<Integer> even if you know that all items are integers.
 * You can then use List<Integer> l2 = new ListView<>(l); to get a (read-only) list of integers with automated casts.
 *
 * @author Felix Mohr
 *
 * @param <T>
 */
public class ListView<T> implements List<T> {
	private static final String MSG_READ_ONLY = "This is a read-only view.";
	private List<?> list;

	public ListView(final List<?> list) {
		this.list = list;
	}

	public ListView(final List<?> list, final Class<T> clazz) {
		this(list);
		for (Object l : list) {
			if (!clazz.isInstance(l)) {
				throw new IllegalArgumentException();
			}
		}
	}

	@Override
	public boolean add(final T arg0) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public void add(final int arg0, final T arg1) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public boolean addAll(final Collection<? extends T> arg0) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public boolean addAll(final int arg0, final Collection<? extends T> arg1) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public boolean contains(final Object arg0) {
		return this.list.contains(arg0);
	}

	@Override
	public boolean containsAll(final Collection<?> arg0) {
		return this.list.containsAll(arg0);
	}

	@Override
	public T get(final int arg0) {
		return (T)this.list.get(arg0);
	}

	@Override
	public int indexOf(final Object arg0) {
		return this.list.indexOf(arg0);
	}

	@Override
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private Iterator<?> it = ListView.this.list.iterator();

			@Override
			public boolean hasNext() {
				return this.it.hasNext();
			}

			@Override
			public T next() {
				return (T)this.it.next();
			}
		};
	}

	@Override
	public int lastIndexOf(final Object arg0) {
		return this.list.lastIndexOf(arg0);
	}

	@Override
	public ListIterator<T> listIterator() {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public ListIterator<T> listIterator(final int arg0) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public boolean remove(final Object arg0) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public T remove(final int arg0) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public boolean removeAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public boolean retainAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public T set(final int arg0, final T arg1) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public int size() {
		return this.list.size();
	}

	@Override
	public List<T> subList(final int arg0, final int arg1) {
		throw new UnsupportedOperationException(MSG_READ_ONLY);
	}

	@Override
	public Object[] toArray() {
		return this.list.toArray();
	}

	@Override
	public <S> S[] toArray(final S[] arg0) {
		return this.list.toArray(arg0);
	}
}
