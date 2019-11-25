package ai.libs.jaicore.ml.ranking.dataset;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.ranking.label.dataset.ILabelRankingDataset;
import org.api4.java.ai.ml.ranking.label.dataset.ILabelRankingInstance;

import ai.libs.jaicore.ml.core.dataset.ADataset;

public class LabelRankingDataset extends ADataset<ILabelRankingInstance> implements ILabelRankingDataset {

	/**
	 *
	 */
	private static final long serialVersionUID = -2831991433001656508L;

	protected LabelRankingDataset(final ILabeledInstanceSchema schema) {
		super(schema);
	}

	@Override
	public LabelRankingDataset createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new LabelRankingDataset(this.getInstanceSchema());
	}

	@Override
	public void removeColumn(final int columnPos) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public LabelRankingDataset createCopy() throws DatasetCreationException, InterruptedException {
		LabelRankingDataset copy = this.createEmptyCopy();
		for (ILabelRankingInstance i : this) {
			copy.add(i);
		}
		return copy;
	}

}