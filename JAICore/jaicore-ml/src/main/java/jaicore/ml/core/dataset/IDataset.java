package jaicore.ml.core.dataset;

import java.util.Collection;

public interface IDataset<I> extends Collection<I> {
	
	/**
	 * Creates an empty copy of the same structure (and same type).
	 *
	 * @return The newly created dataset.
	 */
	public IDataset<I> createEmpty() throws DatasetCreationException;
}
