package ai.libs.automl.mlplan.multiclass.wekamlplan.sklearn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import ai.libs.automl.AbstractMLPlanBuilderTest;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.regression.loss.ERulPerformanceMeasure;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.sklearn.EMLPlanSkLearnProblemType;
import ai.libs.mlplan.multiclass.sklearn.MLPlanSKLearnBuilder;

public class MLPlanSkLearnBuilderTest extends AbstractMLPlanBuilderTest {

	private static final File CLASSIFICATION_TEST_SEARCHSPACE = new File("resources/automl/searchmodels/sklearn/test-sklearn-classification-pipeline.json");
	private static final File CLASSIFICATION_TEST_DATASET = new File("testrsc/car.arff");
	private static final String CLASSIFICATION_REQUESTED_INTERFACE = "MLPipeline";
	private static final String CLASSIFICATION_PIPELINE = "make_pipeline(MaxAbsScaler(),GaussianNB())";

	private static final File RUL_TEST_SEARCHSPACE = new File("resources/automl/searchmodels/sklearn/test-sklearn-rul-pipeline.json");
	private static final File RUL_TEST_DATASET = new File("testrsc/rul_smallExample.arff");
	private static final String RUL_PIPELINE = "make_pipeline(FeatureAugmenter(column_id=\"instance_id\",n_jobs=0,column_kind=\"sensor\",disable_progressbar=True,column_sort=\"timestep\",column_value=\"value\",default_fc_parameters=Tsfresh(has_duplicate=True,longest_strike_below_mean=True,approximate_entropy=False,number_crossing_m=True,fft_coefficient=False,energy_ratio_by_chunks=True,standard_deviation=True,absolute_sum_of_changes=True,percentage_of_reoccurring_values_to_all_values=False,large_standard_deviation=False,cwt_coefficients=False,quantile=False,sum_of_reoccurring_values=True,skewness=True,first_location_of_maximum=True,has_duplicate_min=True,spkt_welch_density=True,c3=True,last_location_of_maximum=True,median=True,longest_strike_above_mean=True,mean_change=True,ratio_value_number_to_time_series_length=True,augmented_dickey_fuller=False,symmetry_looking=True,abs_energy=True,sum_of_reoccurring_data_points=True,count_below_mean=True,agg_linear_trend=False,last_location_of_minimum=True,range_count=True,friedrich_coefficients=False,time_reversal_asymmetry_statistic=True,kurtosis=True,ratio_beyond_r_sigma=False,has_duplicate_max=True,partial_autocorrelation=True,number_cwt_peaks=False,linear_trend=True,number_peaks=False,value_count=True,agg_autocorrelation=True,cid_ce=True,percentage_of_reoccurring_datapoints_to_all_datapoints=True,first_location_of_minimum=True,length=True,variance_larger_than_standard_deviation=True,sum_values=True,mean_abs_change=True,fft_aggregated=True,autocorrelation=False,count_above_mean=True,variance=True,mean=True,maximum=True,sample_entropy=False,ar_coefficient=False,minimum=True,binned_entropy=True,max_langevin_fixed_point=False,mean_second_derivative_central=True,change_quantiles=False)),InvalidNumberReplacementTransformer(),DecisionTreeRegressor(max_features=None,criterion=\"mse\",max_leaf_nodes=None,max_depth=None,splitter=\"best\"))";

	@Override
	public MLPlanSKLearnBuilder<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> getBuilder() throws IOException {
		MLPlanSKLearnBuilder<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> builder = new MLPlanSKLearnBuilder<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch>();
		return builder;
	}

	@Override
	public ERulPerformanceMeasure getPerformanceMeasure() throws Exception {
		return ERulPerformanceMeasure.MEAN_ABSOLUTE_PERCENTAGE_ERROR;
	}

	@Test
	public void testProblemTypeClassificationAsDefault() throws Exception {
		MLPlanSKLearnBuilder<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> builder = this.getBuilder();
		builder.withDataset(ArffDatasetAdapter.readDataset(CLASSIFICATION_TEST_DATASET));

		this.checkBuilderConfiguration(builder, EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS, "car");

		builder.withSearchSpaceConfigFile(CLASSIFICATION_TEST_SEARCHSPACE);
		assertEquals(CLASSIFICATION_TEST_SEARCHSPACE.getPath(), builder.getSearchSpaceConfigFile().getPath());

		builder.withRequestedInterface(CLASSIFICATION_REQUESTED_INTERFACE);
		builder.withTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withNodeEvaluationTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(15, TimeUnit.SECONDS));

		MLPlan<ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch>> mlplan = builder.build();
		try {
			ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> model = mlplan.call();
			assertEquals(EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS.getSkLearnProblemType().getName(), model.getProblemType().getName());
			assertEquals(CLASSIFICATION_PIPELINE, model.toString());
		} catch (NoSuchElementException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testProblemTypeRUL() throws IOException, DatasetDeserializationFailedException, AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		MLPlanSKLearnBuilder<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> builder = this.getBuilder();
		builder.withProblemType(EMLPlanSkLearnProblemType.RUL);
		builder.withDataset(ArffDatasetAdapter.readDataset(RUL_TEST_DATASET));

		this.checkBuilderConfiguration(builder, EMLPlanSkLearnProblemType.RUL, "CMAPSSsmallExample");

		builder.withSearchSpaceConfigFile(RUL_TEST_SEARCHSPACE);
		assertEquals(RUL_TEST_SEARCHSPACE.getPath(), builder.getSearchSpaceConfigFile().getPath());

		builder.withTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withNodeEvaluationTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(15, TimeUnit.SECONDS));

		MLPlan<ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch>> mlplan = builder.build();
		try {
			ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> model = mlplan.call();
			assertEquals(EMLPlanSkLearnProblemType.RUL.getSkLearnProblemType().getName(), model.getProblemType().getName());
			assertEquals(RUL_PIPELINE, model.toString());
		} catch (NoSuchElementException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testProblemTypeResetToClassification() throws IOException, DatasetDeserializationFailedException, AlgorithmTimeoutedException, AlgorithmException, InterruptedException, AlgorithmExecutionCanceledException {
		MLPlanSKLearnBuilder<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> builder = this.getBuilder();
		builder.withProblemType(EMLPlanSkLearnProblemType.RUL);
		builder.withProblemType(EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS);
		builder.withDataset(ArffDatasetAdapter.readDataset(CLASSIFICATION_TEST_DATASET));

		this.checkBuilderConfiguration(builder, EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS, "car");

		builder.withSearchSpaceConfigFile(CLASSIFICATION_TEST_SEARCHSPACE);
		assertEquals(CLASSIFICATION_TEST_SEARCHSPACE.getPath(), builder.getSearchSpaceConfigFile().getPath());

		builder.withRequestedInterface(CLASSIFICATION_REQUESTED_INTERFACE);
		builder.withTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withNodeEvaluationTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(15, TimeUnit.SECONDS));

		MLPlan<ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch>> mlplan = builder.build();
		try {
			ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> model = mlplan.call();
			assertEquals(EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS.getSkLearnProblemType().getName(), model.getProblemType().getName());
			assertEquals(CLASSIFICATION_PIPELINE, model.toString());
		} catch (NoSuchElementException e) {
			assertTrue(false);
		}
	}

}
