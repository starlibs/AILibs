package jaicore.ml.core.dataset;

import java.util.List;

/**
 * Extends the {@link}IDataset by including the List interface.
 */
public interface IListDataset<I extends IInstance> extends IDataset<I>, List<I> {

}