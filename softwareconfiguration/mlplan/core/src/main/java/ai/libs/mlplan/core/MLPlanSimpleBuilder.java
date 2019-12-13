package ai.libs.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.api4.java.ai.ml.classification.IClassifier;

import ai.libs.jaicore.ml.classification.singlelabel.learner.MajorityClassifier;

public class MLPlanSimpleBuilder extends AbstractMLPlanBuilder<IClassifier, MLPlanSimpleBuilder> {

	public MLPlanSimpleBuilder() {
		try {
			this.withSearchSpaceConfigFile(new File("resources/mlplan/mlplan-simple.searchspace.json"));

			/* configure classifier factory */
			this.withClassifierFactory(ci -> new MajorityClassifier());

			/* configure dataset splitter */
			this.withDatasetSplitterForSearchSelectionSplit((data, random, relativeFoldSizes) -> {
				return Arrays.asList(data, data);
			});

			this.withMCCVBasedCandidateEvaluationInSearchPhase().withNumMCIterations(3).withTrainFoldSize(.7);
			this.withMCCVBasedCandidateEvaluationInSelectionPhase().withNumMCIterations(3).withTrainFoldSize(.7);
			this.withRequestedInterface("AbstractClassifier");
			//			this.withSearchPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory<>());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public MLPlanSimpleBuilder getSelf() {
		return this;
	}
}
