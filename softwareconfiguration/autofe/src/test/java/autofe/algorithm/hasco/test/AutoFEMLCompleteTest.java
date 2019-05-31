package autofe.algorithm.hasco.test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import autofe.util.test.DataSetUtilsTest;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

import ai.libs.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import autofe.algorithm.hasco.AutoFEMLComplete;
import autofe.algorithm.hasco.AutoFEWekaPipelineFactory;
import autofe.algorithm.hasco.MLPlanFEWekaClassifierConfig;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;

public class AutoFEMLCompleteTest {
    @Test
    public void autoFEMLCompleteTest() throws Exception {

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
        System.out.println("Timeout:"+ autofeml.getTimeout().toString());

        System.out.println("Start building AutoFEML classifier...");

        autofeml.buildClassifier(trainTestSplit.get(0));

        System.out.println("Solution: " + autofeml.getSelectedPipeline());
        System.out.println("Internal score: " + autofeml.getInternalValidationErrorOfSelectedClassifier());
        Assert.assertTrue(autofeml.getInternalValidationErrorOfSelectedClassifier() < 1);
    }
}
