package autofe.examples;

import autofe.algorithm.hasco.AutoFEMLComplete;
import autofe.algorithm.hasco.AutoFEWekaPipelineFactory;
import autofe.algorithm.hasco.MLPlanFEWekaClassifierConfig;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import autofe.util.test.DataSetUtilsTest;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AutoFEMLCompleteExample {
    private static Logger logger = LoggerFactory.getLogger(AutoFEMLCompleteExample.class);

    public static void main(String[] args) throws Exception {

        DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.FASHION_MNIST_ID);
        long[] shape = DataSetUtilsTest.FASHION_MNIST_SHAPE;

        List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(42), .7);

        MLPlanFEWekaClassifierConfig config = ConfigFactory.create(MLPlanFEWekaClassifierConfig.class);

        AutoFEWekaPipelineFactory factory = new AutoFEWekaPipelineFactory(new FilterPipelineFactory(shape),
                new WEKAPipelineFactory());

        AutoFEMLComplete autofeml = new AutoFEMLComplete(42, 0.01, 5, 200, config, factory);
        autofeml.setTimeoutForNodeEvaluation(30);
        autofeml.setTimeoutForSingleSolutionEvaluation(30);
        autofeml.setTimeout(600, TimeUnit.SECONDS);
        autofeml.setNumCPUs(4);
        logger.info("Timeout: {}", autofeml.getTimeout());

        logger.info("Start building AutoFEML classifier...");

        autofeml.buildClassifier(trainTestSplit.get(0));

        logger.info("Solution: {}", autofeml.getSelectedPipeline());
        logger.info("Internal score: {}", autofeml.getInternalValidationErrorOfSelectedClassifier());
    }
}
