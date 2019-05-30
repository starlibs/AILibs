package jaicore.ml.core.dataset;

import java.util.List;

public interface IOrderedLabeledDataset<I extends ILabeledInstance<L>, L> extends IDataset<I>, List<I> {

}
