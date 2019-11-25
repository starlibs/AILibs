package ai.libs.jaicore.ml.classification.singlelabel.dataset;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

public class SingleLabelClassificationDataset implements ISingleLabelClassificationDataset {

	public SingleLabelClassificationDataset(final ILabeledDataset<? extends ILabeledInstance> dataset) {

	}

	public SingleLabelClassificationDataset() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object[] getLabelVector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<ISingleLabelClassificationInstance> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILabeledDataset<ISingleLabelClassificationInstance> createEmptyCopy() throws DatasetCreationException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[][] getFeatureMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeColumn(final int columnPos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeColumn(final String columnName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeColumn(final IAttribute attribute) {
		// TODO Auto-generated method stub

	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(final Object o) {
		// TODO Auto-generated method stub
		return false;
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

	@Override
	public boolean add(final ISingleLabelClassificationInstance e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(final Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(final Collection<? extends ISingleLabelClassificationInstance> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends ISingleLabelClassificationInstance> c) {
		// TODO Auto-generated method stub
		return false;
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
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public ISingleLabelClassificationInstance get(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISingleLabelClassificationInstance set(final int index, final ISingleLabelClassificationInstance element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void add(final int index, final ISingleLabelClassificationInstance element) {
		// TODO Auto-generated method stub

	}

	@Override
	public ISingleLabelClassificationInstance remove(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(final Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lastIndexOf(final Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<ISingleLabelClassificationInstance> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<ISingleLabelClassificationInstance> listIterator(final int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ISingleLabelClassificationInstance> subList(final int fromIndex, final int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDataset<ISingleLabelClassificationInstance> createCopy() throws DatasetCreationException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

}
