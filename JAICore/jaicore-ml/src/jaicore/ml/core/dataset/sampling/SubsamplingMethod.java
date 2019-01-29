package jaicore.ml.core.dataset.sampling;

import java.util.Random;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.math3.ml.distance.ManhattanDistance;

import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.casecontrol.CaseControlSampling;
import jaicore.ml.core.dataset.sampling.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import jaicore.ml.core.dataset.sampling.stratified.sampling.GMeansStratiAmountSelectorAndAssigner;
import jaicore.ml.core.dataset.sampling.stratified.sampling.StratifiedSampling;

/**
 * An enumeration of all the implemented subsampling methods. A default
 * configured instantiation can be returned for each.
 * 
 * @author Lukas Brandt
 */
public enum SubsamplingMethod {
	SIMPLE_RANDOM_SAMPLING {
		@Override
		public ASamplingAlgorithm<IInstance> getSubsampler(long seed) {
			return new SimpleRandomSampling<IInstance>(new Random(seed));
		}

	},
	SYSTEMATIC_SAMPLING {
		@Override
		public ASamplingAlgorithm<IInstance> getSubsampler(long seed) {
			return new SystematicSampling<IInstance>(new Random(seed));
		}
	},
	K_MEANS_SAMPLING {
		@Override
		public ASamplingAlgorithm<IInstance> getSubsampler(long seed) {
			return new KmeansSampling<IInstance>(seed, new ManhattanDistance());
		}
	},
	G_MEANS_SAMPLING {
		@Override
		public ASamplingAlgorithm<IInstance> getSubsampler(long seed) {
			return new GmeansSampling<IInstance>(seed);
		}
	},
	CASE_CONTROL_SAMPLING {
		@Override
		public ASamplingAlgorithm<IInstance> getSubsampler(long seed) {
			return new CaseControlSampling<IInstance>(new Random(seed));
		}
	},
	LOCAL_CASE_CONTROL_SAMPLING {
		@Override
		public ASamplingAlgorithm<IInstance> getSubsampler(long seed) {
			throw new NotImplementedException(
					"Create a subsampler with default configuration for LCC is not implemented yet");
		}
	},
	OSMAC_SAMPLING {
		@Override
		public ASamplingAlgorithm<IInstance> getSubsampler(long seed) {
			throw new NotImplementedException(
					"Create a subsampler with default configuration for OSMAC is not implemented yet");
		}
	},
	ATTRIBUTE_STRATIFIED_SAMLPING {
		@Override
		public ASamplingAlgorithm<IInstance> getSubsampler(long seed) {
			AttributeBasedStratiAmountSelectorAndAssigner<IInstance> a = new AttributeBasedStratiAmountSelectorAndAssigner<>();
			return new StratifiedSampling<IInstance>(a, a, new Random(seed));
		}
	},
	G_MEANS_STRATIFIED_SAMPLING {
		@Override
		public ASamplingAlgorithm<IInstance> getSubsampler(long seed) {
			GMeansStratiAmountSelectorAndAssigner<IInstance> g = new GMeansStratiAmountSelectorAndAssigner<IInstance>(
					(int) seed);
			return new StratifiedSampling<IInstance>(g, g, new Random(seed));
		}
	};

	public abstract ASamplingAlgorithm<IInstance> getSubsampler(long seed);
}
