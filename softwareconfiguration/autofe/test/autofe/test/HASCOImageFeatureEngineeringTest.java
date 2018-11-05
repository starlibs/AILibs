package autofe.test;

import java.io.File;
import java.sql.Time;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.aeonbits.owner.ConfigFactory;

import autofe.algorithm.hasco.HASCOFeatureEngineering;
import autofe.algorithm.hasco.HASCOFeatureEngineeringConfig;
import autofe.algorithm.hasco.evaluation.AbstractHASCOFEObjectEvaluator;
import autofe.algorithm.hasco.evaluation.COCOObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import jaicore.basic.SQLAdapter;

public class HASCOImageFeatureEngineeringTest {

	// private static final int MAX_PIPELINE_SIZE = 5;

	private static final int MIN_INSTANCES = 100;

	private static final double SUBSAMPLE_RATIO = 0.01;

	private static final int DATASET_ID = DataSetUtils.CIFAR10_ID;

	public static void main(final String[] args) throws Exception {
		print("Load dataset...");
		// DataSet data = DataSetUtils.loadDatasetFromImageFolder(new
		// File("testrsc/kit/"), ColorSpace.Grayscale);
		DataSet data = DataSetUtils.getDataSetByID(DATASET_ID);

		print("Split dataset into train and test...");
		List<DataSet> stratifiedSplit = DataSetUtils.getStratifiedSplit(data, new Random(0), .7);

		double ratio = SUBSAMPLE_RATIO;
		List<DataSet> subsampledDataSet;
		if (stratifiedSplit.get(0).getInstances().size() * SUBSAMPLE_RATIO < MIN_INSTANCES) {
			ratio = (double) MIN_INSTANCES / stratifiedSplit.get(0).getInstances().size();
		}
		print("Subsampling ratio is " + ratio);
		subsampledDataSet = DataSetUtils.getStratifiedSplit(stratifiedSplit.get(0), new Random(0), ratio);
		DataSet dataForFE = subsampledDataSet.get(0);
		print("Subsampled dataset to use only " + dataForFE.getInstances().size() + " instances.");

		// setup factory for filter pipelines
		print(Arrays.toString(data.getIntermediateInstances().get(0).shape()));
		FilterPipelineFactory factory = new FilterPipelineFactory(data.getIntermediateInstances().get(0).shape());

		// setup benchmark for filter pipelines
		AbstractHASCOFEObjectEvaluator benchmark = new COCOObjectEvaluator();
		benchmark.setAdapter(new SQLAdapter("localhost", "autofeml", "Hallo33!", "autofeml_test"));
		benchmark.setEvalTable("autofeml_manual_test");
		benchmark.setData(dataForFE);

		// Setup HASCOImageFeatureEngineering
		HASCOFeatureEngineeringConfig config = ConfigFactory.create(HASCOFeatureEngineeringConfig.class);
		HASCOFeatureEngineering hasco = new HASCOFeatureEngineering(new File("model/catalano/catalano.json"),
				factory, benchmark, config);

		hasco.setNumCPUs(4);
		System.out.println(config.timeout());
		// hasco.setTimeout(30, TimeUnit.SECONDS);

		FilterPipeline solution = hasco.build(dataForFE);
		print(solution.toString());

		print("Search finished.");

	}

	public static void print(final String msg) {
		System.out.println(new Time(System.currentTimeMillis()).toString() + ": " + msg);
	}

}
