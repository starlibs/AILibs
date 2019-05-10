package jaicore.basic.sets;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class solves the following problem: Sometimes you want to use objects of a concrete List class L
 * to be used in a context where some extension of the List interface L' is used, which is not implemented by L.
 * To use objects of type L in such a context, it is a bad idea to cast and copy the items of L into an
 * object of type L' but instead a decorator implementing L' should be used that forwards all operations to the
 * original instance.
 *
 * The ListDecorator must be told how to create elements of the decorated list in order to insert
 *
 * @author fmohr
 *
 * @param <L>
 * @param <E>
 */
public class ListDecorator<L extends List<E>, E, D extends ElementDecorator<E>> implements List<D> {
	private final L list;
	private final Class<E> typeOfDecoratedItems = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
	private final Class<D> typeOfDecoratingItems = (Class<D>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[2];
	private final Constructor<D> constructorForDecoratedItems;

	public ListDecorator(final L list) {
		super();
		this.list = list;
		Constructor<D> constructorForDecoratedItems = null;
		try {
			constructorForDecoratedItems = this.typeOfDecoratingItems.getConstructor(this.typeOfDecoratedItems);
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace(); // this should never be thrown
		}
		this.constructorForDecoratedItems = constructorForDecoratedItems;
	}

	private D getDecorationForElement(final E element) {
		try {
			return this.constructorForDecoratedItems.newInstance(element);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean add(final D e) {
		return this.list.add(e.getElement());
	}

	@Override
	public void add(final int index, final D element) {
		this.list.add(index, element.getElement());
	}

	@Override
	public boolean addAll(final Collection<? extends D> c) {
		if (c == null) {
			throw new IllegalArgumentException("Cannot add NULL collection to list.");
		}
		boolean allSuccessfull = true;
		for (D e : c) {
			if (!this.list.add(e.getElement())) {
				allSuccessfull = false;
			}
		}
		return allSuccessfull;
	}

	@Override
	public boolean addAll(int index, final Collection<? extends D> c) {
		int sizeBefore = this.list.size();
		for (D e : c) {
			this.list.add(index++, e.getElement());
		}
		return sizeBefore + c.size() == this.list.size();
	}

	@Override
	public void clear() {
		this.list.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(final Object o) {
		return this.typeOfDecoratingItems.isInstance(o) && this.list.contains(((D) o).getElement());
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (Object o : c) {
			if (!this.contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public D get(final int index) {
		return this.getDecorationForElement(this.list.get(index));
	}

	@Override
	public int indexOf(final Object o) {
		return this.typeOfDecoratingItems.isInstance(o) ? (this.list.indexOf(((D) o).getElement())) : -1;
	}

	@Override
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	@Override
	public Iterator<D> iterator() {
		return new Iterator<D>() {

			Iterator<E> internalIterator = ListDecorator.this.list.iterator();

			@Override
			public boolean hasNext() {
				return this.internalIterator.hasNext();
			}

			@Override
			public D next() {
				return ListDecorator.this.getDecorationForElement(this.internalIterator.next());
			}

		};
	}

	@Override
	public int lastIndexOf(final Object o) {
		return this.typeOfDecoratingItems.isInstance(o) ? (this.list.lastIndexOf(((D) o).getElement())) : -1;
	}

	@Override
	public ListIterator<D> listIterator() {
		return this.listIterator(0);
	}

	@Override
	public ListIterator<D> listIterator(final int index) {
		return new ListIterator<D>() {

			ListIterator<E> internalIterator = ListDecorator.this.list.listIterator(index);

			@Override
			public void add(final D arg0) {
				this.internalIterator.add(arg0.getElement());
			}

			@Override
			public boolean hasNext() {
				return this.internalIterator.hasNext();
			}

			@Override
			public boolean hasPrevious() {
				return this.internalIterator.hasPrevious();
			}

			@Override
			public D next() {
				return ListDecorator.this.getDecorationForElement(this.internalIterator.next());
			}

			@Override
			public int nextIndex() {
				return this.internalIterator.nextIndex();
			}

			@Override
			public D previous() {
				return ListDecorator.this.getDecorationForElement(this.internalIterator.previous());
			}

			@Override
			public int previousIndex() {
				return this.internalIterator.previousIndex();
			}

			@Override
			public void remove() {
				this.internalIterator.remove();
			}

			@Override
			public void set(final D arg0) {
				this.internalIterator.set(arg0.getElement());
			}
		};
	}

	@Override
	public boolean remove(final Object o) {
		return this.typeOfDecoratingItems.isInstance(o) ? (this.list.remove(((D) o).getElement())) : false;
	}

	@Override
	public D remove(final int index) {
		return this.getDecorationForElement(this.list.remove(index));
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean changed = false;
		for (Object o : c) {
			if (this.list.remove(o)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		boolean changed = false;
		for (int i = 0; i < this.list.size(); i++) {
			D construct = this.getDecorationForElement(this.list.get(i));
			if (!c.contains(construct)) {
				this.list.remove(i);
				i--;
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public D set(final int index, final D element) {
		return this.getDecorationForElement(this.list.set(index, element.getElement()));
	}

	@Override
	public int size() {
		return this.list.size();
	}

	@Override
	public List<D> subList(final int fromIndex, final int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		Object[] arrayOfInternals = this.list.toArray();
		Object[] array = new Object[arrayOfInternals.length];
		for (int i = 0; i < arrayOfInternals.length; i++) {
			array[i] = this.getDecorationForElement((E)arrayOfInternals[i]);
		}
		return array;
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		Object[] arrayOfInternals = this.list.toArray();
		T[] array = (T[]) Array.newInstance(a.getClass().getComponentType(), arrayOfInternals.length);
		for (int i = 0; i < arrayOfInternals.length; i++) {
			array[i] = (T)this.getDecorationForElement((E)arrayOfInternals[i]);
		}
		return array;
	}

	public L getList() {
		return this.list;
	}
}
