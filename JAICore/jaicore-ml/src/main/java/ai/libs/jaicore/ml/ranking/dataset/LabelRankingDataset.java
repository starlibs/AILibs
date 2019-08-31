package ai.libs.jaicore.ml.ranking.dataset;

import java.util.ArrayList;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.ranking.label.dataset.ILabelRankingDataset;
import org.api4.java.ai.ml.ranking.label.dataset.ILabelRankingInstance;

public class LabelRankingDataset extends ArrayList<ILabelRankingInstance> implements ILabelRankingDataset {

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return null;
	}

	@Override
	public Object[] getLabelVector() {
		return null;
	}

	@Override
	public IDataset<ILabelRankingInstance> createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return null;
	}

	@Override
	public Object[][] getFeatureMatrix() {
		return null;
	}

}
