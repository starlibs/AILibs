package ai.libs.jaicore.ml.ranking.filter;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.filter.FilterApplicationFailedException;
import org.api4.java.ai.ml.core.filter.unsupervised.IUnsupervisedFilter;

public class PairWisePreferenceToBinaryClassificationFilter implements IUnsupervisedFilter {

	private final Object labelA;
	private final Object labelB;

	public PairWisePreferenceToBinaryClassificationFilter(final Object labelA, final Object labelB) {
		this.labelA = labelA;
		this.labelB = labelB;
	}

	public Object getLabelA() {
		return this.labelA;
	}

	public Object getLabelB() {
		return this.labelB;
	}

	@Override
	public ILabeledDataset<ILabeledInstance> predict(final ILabeledDataset<ILabeledInstance> input) throws InterruptedException, FilterApplicationFailedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILabeledDataset<ILabeledInstance> predict(final ILabeledInstance input) throws InterruptedException, FilterApplicationFailedException {
		throw new UnsupportedOperationException();
	}

}
