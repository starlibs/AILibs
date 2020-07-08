package ai.libs.mlplan.core;

import java.io.IOException;
import java.util.Random;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.classification.singlelabel.learner.MajorityClassifier;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;

public class MLPlanSimpleBuilder extends AbstractMLPlanBuilder<IClassifier, MLPlanSimpleBuilder> {

	public MLPlanSimpleBuilder() throws IOException {
		super(new IProblemType() {

			@Override
			public String getSearchSpaceConfigFromFileSystem() {
				return "mlplan/mlplan-simple.searchspace.json";
			}

			@Override
			public String getSearchSpaceConfigFileFromResource() {
				return "";
			}

			@Override
			public String getRequestedInterface() {
				return "AbstractClassifier";
			}

			@Override
			public String getPreferredComponentName() {
				return "";
			}

			@Override
			public String getPreferredComponentListFromResource() {
				return "";
			}

			@Override
			public String getPreferredComponentListFromFileSystem() {
				return "";
			}

			@Override
			public String getPreferredBasicProblemComponentName() {
				return "";
			}

			@Override
			public double getPortionOfDataReservedForSelectionPhase() {
				return 0;
			}

			@Override
			public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSelectionPhase() {
				return EClassificationPerformanceMeasure.ERRORRATE;
			}

			@Override
			public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSearchPhase() {
				return EClassificationPerformanceMeasure.ERRORRATE;
			}

			@Override
			public String getName() {
				return "SimpleProblemType";
			}
		});

		/* configure classifier factory */
		this.withClassifierFactory(new ILearnerFactory<IClassifier>() {

			@Override
			public IClassifier getComponentInstantiation(final ComponentInstance groundComponent) throws ComponentInstantiationFailedException {
				return new MajorityClassifier();
			}

			@Override
			public void setProblemType(final IProblemType problemType) {
			}
		});

		/* configure dataset splitter */
		this.withDatasetSplitterForSearchSelectionSplit(new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), .9, new Random(0)));

		this.withMCCVBasedCandidateEvaluationInSearchPhase().withNumMCIterations(3).withTrainFoldSize(.7);
		this.withMCCVBasedCandidateEvaluationInSelectionPhase().withNumMCIterations(3).withTrainFoldSize(.7);

	}

	@Override
	public MLPlanSimpleBuilder getSelf() {
		return this;
	}
}
