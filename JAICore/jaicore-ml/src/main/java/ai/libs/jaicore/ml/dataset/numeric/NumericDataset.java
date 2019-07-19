package ai.libs.jaicore.ml.dataset.numeric;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.dataset.IAttribute;

public class NumericDataset implements Iterable<Pair<double[], double[]>> {

	private static final int DEFAULT_CAPACITY = 1;
	private static final int ROW_EXTENSION = 1;

	private final String relationName;
	private final List<IAttribute> instanceAttributes;
	private final List<IAttribute> targetAttributes;

	private double[][] xMatrix;
	private double[][] yMatrix;
	private int size;

	private PriorityQueue<Integer> queue = new PriorityQueue<>();

	public NumericDataset(final String relationName, final List<IAttribute> instanceAttributes, final List<IAttribute> targetAttributes) {
		this(relationName, instanceAttributes, targetAttributes, DEFAULT_CAPACITY);
	}

	public NumericDataset(final String relationName, final List<IAttribute> instanceAttributes, final List<IAttribute> targetAttributes, final int capacity) {
		this.relationName = relationName;
		this.instanceAttributes = new LinkedList<>(instanceAttributes);
		this.targetAttributes = new LinkedList<>(targetAttributes);
		this.xMatrix = new double[capacity][];
		this.yMatrix = new double[capacity][];
		this.queue.offer(0);
		this.size = 0;
	}

	public NumericDataset(final NumericDataset other) {
		this.relationName = other.relationName;
		this.instanceAttributes = new LinkedList<>(other.instanceAttributes);
		this.targetAttributes = new LinkedList<>(other.targetAttributes);
		this.xMatrix = Arrays.copyOf(other.xMatrix, other.xMatrix.length);
		this.yMatrix = Arrays.copyOf(other.yMatrix, other.yMatrix.length);
		this.size = other.size;
		other.queue.stream().forEach(this.queue::offer);
	}

	public double[][] getX() {
		return this.xMatrix;
	}

	public double[][] getY() {
		return this.yMatrix;
	}

	public void addInstance(final double[] x, final double[] y) {
		Integer indexToInsert = this.queue.poll();
		if (indexToInsert == null) {
			throw new IllegalStateException("There was no next index for an instance to be inserted?!");
		}
		if (this.queue.isEmpty()) {
			this.queue.offer(indexToInsert + 1);
		}

		System.out.println(indexToInsert + " " + x.length);
		if (indexToInsert >= this.xMatrix.length) {
			this.xMatrix = Arrays.copyOf(this.xMatrix, this.xMatrix.length + ROW_EXTENSION);
			this.yMatrix = Arrays.copyOf(this.yMatrix, this.yMatrix.length + ROW_EXTENSION);
		}

		this.xMatrix[indexToInsert] = x;
		this.yMatrix[indexToInsert] = y;
		this.size++;
	}

	public Pair<double[], double[]> getInstance(final int index) {
		return new Pair<>(this.xMatrix[index], this.yMatrix[index]);
	}

	@Override
	public Iterator<Pair<double[], double[]>> iterator() {
		return new NumericDatasetIterator();
	}

	public boolean removeInstance(final int index) {
		if (index < 0 || index >= this.xMatrix.length) {
			throw new NoSuchElementException("There is no such element to be removed. Invalid index (" + index + ") given.");
		}

		if (this.xMatrix[index] != null || this.yMatrix[index] != null) {
			this.xMatrix[index] = null;
			this.yMatrix[index] = null;
			this.queue.offer(index);
			this.size--;
			return true;
		} else {
			return false;
		}
	}

	public String getRelationName() {
		return this.relationName;
	}

	public List<IAttribute> getInstanceAttributes() {
		return new LinkedList<>(this.instanceAttributes);
	}

	public List<IAttribute> getTargetAttributes() {
		return new LinkedList<>(this.targetAttributes);
	}

	public int size() {
		return this.size;
	}

	public class NumericDatasetIterator implements Iterator<Pair<double[], double[]>> {
		private int currentIndex;
		private List<Integer> emptySlotIndices;

		private NumericDatasetIterator() {
			this.currentIndex = 0;
			this.emptySlotIndices = new LinkedList<>(NumericDataset.this.queue);
		}

		@Override
		public boolean hasNext() {
			while (true) {
				// the last element of the queue marks the first position in the instance matrix which is still empty
				// thus, if the current index has reached / exceeded this point, there is no element anymore to iterate over.
				if (this.currentIndex >= this.emptySlotIndices.get(this.emptySlotIndices.size() - 1)) {
					return false;
				}

				// if the empty slot indicies list contains the current index the instance has been removed, thus increment
				// the counter and try anew.
				if (this.emptySlotIndices.contains(this.currentIndex)) {
					this.currentIndex++;
				} else {
					return true;
				}
			}
		}

		@Override
		public Pair<double[], double[]> next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException("No next element.");
			}
			return new Pair<>(NumericDataset.this.xMatrix[this.currentIndex], NumericDataset.this.yMatrix[this.currentIndex]);
		}

	}
}
