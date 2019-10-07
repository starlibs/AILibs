package ai.libs.jaicore.ml.core.dataset.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.dataset.splitter.IDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

public class RandomKFoldSplitter<I extends IInstance, D extends IDataset<I>> implements IDatasetSplitter<I, D> {

	private final int folds;
	private final Random rand;

	public RandomKFoldSplitter(final int folds) {
		this(folds, new Random());
	}

	public RandomKFoldSplitter(final int folds, final Random rand) {
		this.folds = folds;
		this.rand = rand;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<D> split(final D data, final long seed) throws SplitFailedException, InterruptedException {
		List<D> cvFolds = new ArrayList<>();
		try {
			for (int i = 0; i < this.folds; i++) {
				cvFolds.add((D) data.createEmptyCopy());
			}
			List<Integer> indices = IntStream.range(0, data.size()).mapToObj(Integer::valueOf).collect(Collectors.toList());
			Collections.shuffle(indices, this.rand);

			for (int i = 0; i < indices.size(); i++) {
				cvFolds.get((i % cvFolds.size())).add(data.get(i));
			}
			return cvFolds;
		} catch (DatasetCreationException e) {
			throw new SplitFailedException("Could not create fold container.", e);
		}
	}

}
