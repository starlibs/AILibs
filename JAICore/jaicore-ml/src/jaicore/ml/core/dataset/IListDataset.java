package jaicore.ml.core.dataset;

import java.util.List;

/**
 * Extends the {@link}IDataset by including the List interface.
 */
public interface IListDataset<INSTANCE extends IInstance> extends IDataset<INSTANCE>, List<INSTANCE> {

}