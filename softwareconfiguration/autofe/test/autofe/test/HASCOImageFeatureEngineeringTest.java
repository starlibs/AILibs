package autofe.test;

import java.io.File;
import java.sql.Time;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import autofe.algorithm.hasco.AutoFEPreferredNodeEvaluator;
import autofe.algorithm.hasco.HASCOImageFeatureEngineering;
import autofe.algorithm.hasco.evaluation.AbstractHASCOFEObjectEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML;
import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedML.HASCOClassificationMLSolution;
import hasco.serialization.ComponentLoader;
import jaicore.basic.TimeOut;

public class HASCOImageFeatureEngineeringTest {

	private static final int MAX_PIPELINE_SIZE = 5;

	private static final int MIN_INSTANCES = 100;

	private static final double SUBSAMPLE_RATIO = 0.01;

	public static void main(final String[] args) throws Exception {
		print("Load dataset...");
		// DataSet data = DataSetUtils.loadDatasetFromImageFolder(new File("testrsc/kit/"), ColorSpace.Grayscale);
		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.CIFAR10_ID);

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

		print("Load components...");
		ComponentLoader cl = new ComponentLoader(new File("model/catalano/catalano.json"));

		HASCOSupervisedML.REQUESTED_INTERFACE = "FilterPipeline";
		HASCOImageFeatureEngineering hasco = new HASCOImageFeatureEngineering(cl);

		// setup feactory for filter pipelines
		FilterPipelineFactory factory = new FilterPipelineFactory(data.getIntermediateInstances().get(0).shape());
		hasco.setFactory(factory);

		// setup benchmark for filter pipelines
		AbstractHASCOFEObjectEvaluator benchmark = new ClusterObjectEvaluator();
		benchmark.setData(dataForFE);
		hasco.setBenchmark(benchmark);

		AutoFEPreferredNodeEvaluator nodeEvaluator = new AutoFEPreferredNodeEvaluator(cl.getComponents(), factory, MAX_PIPELINE_SIZE);
		hasco.setPreferredNodeEvaluator(nodeEvaluator);

		hasco.setNumberOfCPUs(4);

		hasco.setTimeoutForSingleFEvaluation(30 * 1000);

		hasco.gatherSolutions(new TimeOut(2, TimeUnit.MINUTES));

		HASCOClassificationMLSolution<FilterPipeline> solution = hasco.getCurrentlyBestSolution();
		print(solution.toString());

		print("Search finished.");

	}

	public static void print(final String msg) {
		System.out.println(new Time(System.currentTimeMillis()).toString() + ": " + msg);
	}

}
