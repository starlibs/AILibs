package ai.libs.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.api4.java.ai.ml.classification.IClassifier;

import ai.libs.jaicore.ml.classification.singlelabel.learner.MajorityClassifier;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;

public class MLPlanSimpleBuilder extends AbstractMLPlanBuilder<IClassifier, MLPlanSimpleBuilder> {

	public MLPlanSimpleBuilder() {
		try {
			this.withSearchSpaceConfigFile(new File("resources/mlplan/mlplan-simple.searchspace.json"));

			/* configure classifier factory */
			this.withClassifierFactory(ci -> new MajorityClassifier());

			/* configure dataset splitter */
			this.withDatasetSplitterForSearchSelectionSplit(new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), .9, new Random(0)));

			this.withMCCVBasedCandidateEvaluationInSearchPhase().withNumMCIterations(3).withTrainFoldSize(.7);
			this.withMCCVBasedCandidateEvaluationInSelectionPhase().withNumMCIterations(3).withTrainFoldSize(.7);
			this.withRequestedInterface("AbstractClassifier");

		} catch (IOException e) {
			throw new IllegalStateException("The resource file could not be found or accessed!", e);
		}
	}

	@Override
	public MLPlanSimpleBuilder getSelf() {
		return this;
	}
}
