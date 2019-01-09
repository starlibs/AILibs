package jaicore.ml.core.dataset.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import jaicore.ml.core.dataset.sampling.stratified.sampling.StratifiedSampling;

public class AttributeBasedStratifiedSamplingTester<I extends IInstance> extends GeneralSamplingTester<I> {

	private static final long RANDOM_SEED = 1;

	@Override
	public IAlgorithmFactory<IDataset<I>, IDataset<I>> getFactory() {
		return new IAlgorithmFactory<IDataset<I>, IDataset<I>>() {

			private IDataset<I> input;

			@Override
			public void setProblemInput(IDataset<I> problemInput) {
				this.input = problemInput;
			}

			@Override
			public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, IDataset<I>> reducer) {
				throw new UnsupportedOperationException("Problem input not applicable for subsampling algorithms!");
			}

			@Override
			public IAlgorithm<IDataset<I>, IDataset<I>> getAlgorithm() {
				Random r = new Random(RANDOM_SEED);
				List<Integer> attributeIndices = new ArrayList<>();
				// We assume that the target is the last attribute
				attributeIndices.add(input.getNumberOfAttributes());

				AttributeBasedStratiAmountSelectorAndAssigner<I> selectorAndAssigner = new AttributeBasedStratiAmountSelectorAndAssigner<>(
						attributeIndices);

				AAlgorithm<IDataset<I>, IDataset<I>> algorithm = new StratifiedSampling<>(selectorAndAssigner,
						selectorAndAssigner, r);

				if (this.input != null) {
					algorithm.setInput(input);
				}
				return algorithm;
			}

		};
	}

}
