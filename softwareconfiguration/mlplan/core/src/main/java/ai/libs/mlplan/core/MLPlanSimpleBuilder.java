package ai.libs.mlplan.core;

import java.io.IOException;
import java.util.Random;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.classification.singlelabel.learner.MajorityClassifier;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;

public class MLPlanSimpleBuilder extends AMLPlanBuilder<IClassifier, MLPlanSimpleBuilder> {

	public MLPlanSimpleBuilder() throws IOException {
		super(new IProblemType<IClassifier>() {

			@Override
			public String getSearchSpaceConfigFromFileSystem() {
				return "";
			}

			@Override
			public String getSearchSpaceConfigFileFromResource() {
				return "mlplan/mlplan-simple.searchspace.json";
			}

			@Override
			public String getRequestedInterface() {
				return "AbstractClassifier";
			}

			@Override
			public String getLastHASCOMethodPriorToParameterRefinementOfBareLearner() {
				return null;
			}

			@Override
			public String getPreferredComponentListFromResource() {
				return null;
			}

			@Override
			public String getPreferredComponentListFromFileSystem() {
				return null;
			}

			@Override
			public String getLastHASCOMethodPriorToParameterRefinementOfPipeline() {
				return null;
			}

			@Override
			public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> getSearchSelectionDatasetSplitter() {
				return new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>());
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

			@Override
			public ILearnerFactory<IClassifier> getLearnerFactory() {
				return new ILearnerFactory<IClassifier>() {

					@Override
					public IClassifier getComponentInstantiation(final IComponentInstance groundComponent) throws ComponentInstantiationFailedException {
						return new MajorityClassifier();
					}
				};
			}

			@Override
			public PipelineValidityCheckingNodeEvaluator getValidityCheckingNodeEvaluator() {
				return null;
			}
		});

		/* configure dataset splitter */
		this.withDatasetSplitterForSearchSelectionSplit(new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), .9, new Random(0)));

		this.withMCCVBasedCandidateEvaluationInSearchPhase(3, .7);
		this.withMCCVBasedCandidateEvaluationInSelectionPhase(3, .7);

	}

	@Override
	public MLPlanSimpleBuilder getSelf() {
		return this;
	}
}
