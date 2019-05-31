package autofe.algorithm.hasco.evaluation.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.WekaUtil;
import autofe.util.DataSetUtils;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LDA;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Nystroem;

public class LDAEvaluationTest {
    private static final Logger logger = LoggerFactory.getLogger(LDAEvaluationTest.class);

    @Test
    public void evaluateTest() throws Exception {
        logger.info("Starting LDA evaluation test...");

        /* load dataset and create a train-test-split */
        OpenmlConnector connector = new OpenmlConnector();
        DataSetDescription ds = connector.dataGet(DataSetUtils.SEGMENT_ID);
        File file = ds.getDataset(DataSetUtils.API_KEY);
        Instances data = new Instances(new BufferedReader(new FileReader(file)));
        data.setClassIndex(data.numAttributes() - 1);
        List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, 42, .05f);

        Instances insts = dataSplit.get(0);
        List<Instances> split = WekaUtil.getStratifiedSplit(insts, 42, .7f);

        long timeStart = System.currentTimeMillis();

        LDA lda = new LDA();
        lda.buildClassifier(split.get(0));

        long timeStartEval = System.currentTimeMillis();

        Evaluation eval = new Evaluation(split.get(0));
        eval.evaluateModel(lda, split.get(1));
        logger.debug("LDA pct correct: " + eval.pctCorrect());
        Assert.assertTrue(eval.pctCorrect() > 0);

        long timeTaken = System.currentTimeMillis() - timeStart;
        long timeTakenEval = System.currentTimeMillis() - timeStartEval;

        logger.debug("LDA took " + (timeTaken / 1000) + " s.");
        logger.debug("LDA eval took " + (timeTakenEval / 1000) + " s.");
    }

    @Test
    public void evaluateKernelLDA() throws Exception {
        logger.info("Starting LDA evaluation test...");

        /* load dataset and create a train-test-split */
        OpenmlConnector connector = new OpenmlConnector();
        DataSetDescription ds = connector.dataGet(DataSetUtils.SEGMENT_ID);
        File file = ds.getDataset(DataSetUtils.API_KEY);
        Instances data = new Instances(new BufferedReader(new FileReader(file)));
        data.setClassIndex(data.numAttributes() - 1);
        List<Instances> dataSplit = WekaUtil.getStratifiedSplit(data, 42, .05f);

        Instances insts = dataSplit.get(0);
        List<Instances> split = WekaUtil.getStratifiedSplit(insts, 42, .7f);
        Instances newInsts = split.get(0);
        Instances evalInsts = split.get(1);

        long timeStart = System.currentTimeMillis();

        Nystroem kernelFilter = new Nystroem();
        kernelFilter.setInputFormat(newInsts);
        kernelFilter.setKernel(new RBFKernel(newInsts, 250007, 0.01));
        newInsts = Filter.useFilter(newInsts, kernelFilter);

        LDA lda = new LDA();

        lda.buildClassifier(newInsts);

        long timeStartEval = System.currentTimeMillis();

        Evaluation eval = new Evaluation(newInsts);
        eval.evaluateModel(lda, evalInsts);
        logger.debug("LDA pct correct: " + eval.pctCorrect());
        Assert.assertTrue(eval.pctCorrect() > 0);

        long timeTaken = System.currentTimeMillis() - timeStart;
        long timeTakenEval = System.currentTimeMillis() - timeStartEval;

        logger.debug("LDA took " + (timeTaken / 1000) + " s.");
        logger.debug("LDA eval took " + (timeTakenEval / 1000) + " s.");
    }
}
