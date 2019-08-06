package ai.libs.jaicore.ml.dataset.numeric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.api4.java.ai.ml.dataset.DatasetCreationException;
import org.api4.java.ai.ml.dataset.IDataSource;
import org.api4.java.ai.ml.dataset.attribute.IAttributeType;
import org.api4.java.ai.ml.dataset.supervised.INumericFeatureSupervisedDataset;
import org.api4.java.ai.ml.dataset.supervised.INumericFeatureSupervisedInstance;

public class NumericDataset<Y> implements INumericFeatureSupervisedDataset<Y, INumericFeatureSupervisedInstance<Y>> {

	class NumericDatasetInstance implements INumericFeatureSupervisedInstance<Y> {

		private int rowIndex;

		private NumericDatasetInstance(final int rowIndex) {
			this.rowIndex = rowIndex;
		}

		@Override
		public Double get(final int pos) {
			return NumericDataset.this.xMatrix.get(this.rowIndex)[pos];
		}

		@Override
		public int getNumFeatures() {
			return NumericDataset.this.xMatrix.get(this.rowIndex).length;
		}

		@Override
		public Iterator<Double> iterator() {
			return Arrays.stream(NumericDataset.this.xMatrix.get(this.rowIndex)).iterator();
		}

		@Override
		public Y getLabel() {
			return NumericDataset.this.yMatrix.get(this.rowIndex);
		}

		@Override
		public double[] toDoubleVector() {
			return NumericDataset.this.xMatrix.get(this.rowIndex);
		}

	}

	class InstanceIterator implements Iterator<INumericFeatureSupervisedInstance<Y>>, ListIterator<INumericFeatureSupervisedInstance<Y>> {

		private int nextIndex = 0;
		private int lastReturnedIndex = -1;

		private InstanceIterator() {
			// intentionally left blank
		}

		private InstanceIterator(final int startIndex) {
			this.nextIndex = startIndex;
		}

		@Override
		public boolean hasNext() {
			return this.nextIndex < NumericDataset.this.xMatrix.size();
		}

		@Override
		public INumericFeatureSupervisedInstance<Y> next() {
			return new NumericDatasetInstance(this.nextIndex++);
		}

		@Override
		public INumericFeatureSupervisedInstance<Y> previous() {
			return new NumericDatasetInstance((this.nextIndex--) - 2);
		}

		@Override
		public void add(final INumericFeatureSupervisedInstance<Y> arg0) {
			NumericDataset.this.add(arg0);
		}

		@Override
		public boolean hasPrevious() {
			return this.nextIndex > 0;
		}

		@Override
		public int nextIndex() {
			return this.nextIndex;
		}

		@Override
		public int previousIndex() {
			return this.nextIndex - 2;
		}

		@Override
		public void set(final INumericFeatureSupervisedInstance<Y> arg0) {
			NumericDataset.this.set(this.nextIndex - 1, arg0);
		}

		@Override
		public void remove() {
			if (this.lastReturnedIndex < 0) {
				throw new NoSuchElementException("No element to remove.");
			}
		}
	}

	private static final int DEFAULT_CAPACITY = 1;

	private final String relationName;
	private final List<IAttributeType> instanceAttributes;
	private final List<IAttributeType> targetAttributes;

	private ArrayList<double[]> xMatrix;
	private ArrayList<Y> yMatrix;

	public NumericDataset(final String relationName, final List<IAttributeType> instanceAttributes, final List<IAttributeType> targetAttributes) {
		this(relationName, instanceAttributes, targetAttributes, DEFAULT_CAPACITY);
	}

	public NumericDataset(final String relationName, final List<IAttributeType> instanceAttributes, final List<IAttributeType> targetAttributes, final int capacity) {
		this.relationName = relationName;
		this.instanceAttributes = new LinkedList<>(instanceAttributes);
		this.targetAttributes = new LinkedList<>(targetAttributes);
		this.xMatrix = new ArrayList<>(capacity);
		this.yMatrix = new ArrayList<>(capacity);
	}

	public NumericDataset(final NumericDataset<Y> other) {
		this.relationName = other.relationName;
		this.instanceAttributes = new LinkedList<>(other.instanceAttributes);
		this.targetAttributes = new LinkedList<>(other.targetAttributes);
		this.xMatrix = new ArrayList<>(other.xMatrix.size());
		other.xMatrix.stream().forEach(x -> this.xMatrix.add(Arrays.copyOf(x, x.length)));
		this.yMatrix = new ArrayList<>(other.yMatrix);
	}

	@Override
	public boolean add(final INumericFeatureSupervisedInstance<Y> instance) {
		this.xMatrix.add(instance.toDoubleVector());
		this.yMatrix.add(instance.getLabel());
		return true;
	}

	public void add(final double[] x, final Y y) {
		this.xMatrix.add(x);
		this.yMatrix.add(y);
	}

	public NumericDatasetInstance getInstance(final int index) {
		return new NumericDatasetInstance(index);
	}

	public boolean removeInstance(final int index) {
		if (index < 0 || index >= this.xMatrix.size()) {
			throw new NoSuchElementException("There is no such element to be removed. Invalid index (" + index + ") given.");
		}

		this.xMatrix.remove(index);
		this.yMatrix.remove(index);
		return true;
	}

	public String getRelationName() {
		return this.relationName;
	}

	@Override
	public int size() {
		return this.xMatrix.size();
	}

	@Override
	public List<IAttributeType> getFeatureTypes() {
		return new LinkedList<>(this.instanceAttributes);
	}

	@Override
	public int getNumFeatures() {
		return this.instanceAttributes.size();
	}

	@Override
	public List<IAttributeType> getLabelTypes() {
		return new LinkedList<>(this.targetAttributes);
	}

	@Override
	public int getNumLabels() {
		return this.targetAttributes.size();
	}

	@Override
	public IDataSource<Double, INumericFeatureSupervisedInstance<Y>> createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new NumericDataset<>(this);
	}

	@Override
	public Iterator<INumericFeatureSupervisedInstance<Y>> iterator() {
		return new InstanceIterator();
	}

	@Override
	public void add(final int index, final INumericFeatureSupervisedInstance<Y> element) {
		this.xMatrix.add(index, element.toDoubleVector());
		this.yMatrix.add(index, element.getLabel());
	}

	@Override
	public boolean addAll(final Collection<? extends INumericFeatureSupervisedInstance<Y>> c) {
		c.stream().forEach(this::add);
		return true;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends INumericFeatureSupervisedInstance<Y>> c) {
		AtomicInteger indexCursor = new AtomicInteger(index);
		c.stream().forEach(x -> this.add(indexCursor.getAndIncrement(), x));
		return true;
	}

	@Override
	public void clear() {
		this.xMatrix.clear();
		this.yMatrix.clear();
	}

	@Override
	public boolean contains(final Object o) {
		if (!(o instanceof INumericFeatureSupervisedInstance)) {
			return false;
		}

		INumericFeatureSupervisedInstance<?> instance = (INumericFeatureSupervisedInstance<?>) o;
		if (!instance.getLabel().getClass().isInstance(this.get(0).getLabel().getClass())) {
			return false;
		}

		for (INumericFeatureSupervisedInstance<Y> i : this) {
			if (Arrays.equals(i.toDoubleVector(), instance.toDoubleVector()) && instance.getLabel().equals(i.getLabel())) {
				return true;
			}
		}

		return false;
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
	public INumericFeatureSupervisedInstance<Y> get(final int index) {
		return new NumericDatasetInstance(index);
	}

	@Override
	public int indexOf(final Object o) {
		if (!(o instanceof INumericFeatureSupervisedInstance)) {
			return -1;
		}

		INumericFeatureSupervisedInstance<?> instance = (INumericFeatureSupervisedInstance<?>) o;
		if (!instance.getLabel().getClass().isInstance(this.get(0).getLabel().getClass())) {
			return -1;
		}

		for (int i = 0; i < this.size(); i++) {
			if (Arrays.equals(this.get(i).toDoubleVector(), instance.toDoubleVector()) && instance.getLabel().equals(this.get(i).getLabel())) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return this.xMatrix.isEmpty();
	}

	@Override
	public int lastIndexOf(final Object o) {
		if (!(o instanceof INumericFeatureSupervisedInstance)) {
			return -1;
		}

		INumericFeatureSupervisedInstance<?> instance = (INumericFeatureSupervisedInstance<?>) o;
		if (!instance.getLabel().getClass().isInstance(this.get(0).getLabel().getClass())) {
			return -1;
		}

		for (int i = this.size() - 1; i > 0; i--) {
			if (Arrays.equals(this.get(i).toDoubleVector(), instance.toDoubleVector()) && instance.getLabel().equals(this.get(i).getLabel())) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<INumericFeatureSupervisedInstance<Y>> listIterator() {
		return null;
	}

	@Override
	public ListIterator<INumericFeatureSupervisedInstance<Y>> listIterator(final int index) {
		return null;
	}

	@Override
	public boolean remove(final Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public INumericFeatureSupervisedInstance<Y> remove(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public INumericFeatureSupervisedInstance<Y> set(final int index, final INumericFeatureSupervisedInstance<Y> element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<INumericFeatureSupervisedInstance<Y>> subList(final int fromIndex, final int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

}
