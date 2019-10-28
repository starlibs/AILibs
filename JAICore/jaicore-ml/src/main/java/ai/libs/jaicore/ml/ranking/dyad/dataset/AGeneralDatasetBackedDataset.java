package ai.libs.jaicore.ml.ranking.dyad.dataset;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.dataset.Dataset;

public class AGeneralDatasetBackedDataset<E extends ILabeledInstance> implements List<E> {

	private Dataset dataset;

	public AGeneralDatasetBackedDataset() {

	}

	public AGeneralDatasetBackedDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	protected Dataset getInternalDataset() {
		return dataset;
	}

	protected void setInternalDataset(Dataset internalDataset) {
		this.dataset = internalDataset;
	}

	@Override
	public int size() {
		return dataset.size();
	}

	@Override
	public boolean isEmpty() {
		return dataset.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return dataset.contains(o);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		return (Iterator<E>) dataset.iterator();
	}

	@Override
	public Object[] toArray() {
		return dataset.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return dataset.toArray(a);
	}

	@Override
	public boolean add(E e) {
		return dataset.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return dataset.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return dataset.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return dataset.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return dataset.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return dataset.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return dataset.retainAll(c);
	}

	@Override
	public void clear() {
		dataset.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		return (E) dataset.get(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E set(int index, E element) {
		return (E) dataset.set(index, element);
	}

	@Override
	public void add(int index, E element) {
		dataset.add(index, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E remove(int index) {
		return (E) dataset.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return dataset.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return dataset.lastIndexOf(o);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListIterator<E> listIterator() {
		return (ListIterator<E>) dataset.listIterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListIterator<E> listIterator(int index) {
		return (ListIterator<E>) dataset.listIterator(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return (List<E>) dataset.subList(fromIndex, toIndex);
	}

}
