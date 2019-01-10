package jaicore.ml.dyadranking.dataset;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.attribute.IAttributeType;

/**
 * A dataset representation for dyad ranking. Contains
 * {@link IDyadRankingInstance}s. In particular, this dataset is just an
 * extension to the {@link ArrayList} implementation with typecasts to
 * {@link IDyadRankingInstance}.
 * 
 * @author Helena Graf, Mirko Jürgens, Jonas Hanselle
 *
 */
public class DyadRankingDataset extends ArrayList<IInstance> implements IDataset {

	private static final long serialVersionUID = -1102494546233523992L;

	/**
	 * Creates an empty dyad ranking dataset.
	 */
	public DyadRankingDataset() {
		super();
	}

	/**
	 * Creates a dyad ranking dataset containing all elements in the given
	 * {@link Collection} in the order specified by the collections iterator.
	 * 
	 * @param c {@link Collection} containing {@link IInstance} objects
	 */
	public DyadRankingDataset(Collection<IInstance> c) {
		super(c);
	}

	/**
	 * Creates an empty dyad ranking dataset with the given initial capacity.
	 * 
	 * @param initialCapacity initial capacity of the dyad ranking dataset
	 */
	public DyadRankingDataset(int initialCapacity) {
		super(initialCapacity);
	}

	public DyadRankingDataset(List<IDyadRankingInstance> sparseDyadRankingInstances) {
		super(sparseDyadRankingInstances);
	}

	@Override
	public <T> IAttributeType<T> getTargetType(Class<? extends T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IAttributeType<?>> getAttributeTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfAttributes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void serialize(OutputStream out) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deserialize(InputStream in) {
		// TODO Auto-generated method stub

	}

	@Override
	public IDyadRankingInstance get(int index) {
		return (IDyadRankingInstance) super.get(index);
	}

}
