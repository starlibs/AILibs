package autofe.util.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;

import autofe.algorithm.hasco.filter.generic.WEKAFilter;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Standardize;

public class DataSetUtilsTest {
    public static final int CIFAR10_ID = 40927;
    public static final long[] CIFAR10_INPUT_SHAPE = new long[]{32, 32, 3};
    public static final int SEGMENT_ID = 40984;
    public static final long[] SEGMENT_INPUT_SHAPE = new long[]{20};
    public static final int MNIST_ID = 554;
    public static final long[] MNIST_INPUT_SHAPE = new long[]{28, 28, 1};
    public static final int FASHION_MNIST_ID = 40996;
    public static final long[] FASHION_MNIST_SHAPE = new long[]{28, 28, 1};

    // @Test
    public void cifar10InstancesAttributesTest() {
        ArrayList<Attribute> atts = new ArrayList<>();
        for (int i = 0; i < 32 * 32 * 3 + 1; i++) {
            atts.add(new Attribute("blub" + i));
        }
        Instances instances = new Instances("test", atts, 1);
        DenseInstance inst = new DenseInstance(atts.size());
        for (int i = 0; i < inst.numAttributes(); i++) {
            inst.setValue(i, 1d);
        }
        inst.setDataset(instances);
        instances.add(inst);

        INDArray result = DataSetUtils.cifar10InstanceToMatrix(inst);
        Assert.assertArrayEquals(new long[]{32, 32, 3}, result.shape());
    }

    @Test
    public void subsamplingTest() throws Exception {
        DataSet data = DataSetUtils.getDataSetByID(MNIST_ID);

        List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(10), false, .7);
        data = trainTestSplit.get(0);

        System.out.println("Num instances / attributes: " + data.getInstances().numInstances() + " / "
                + data.getInstances().numAttributes());

        System.out.println("Start subsampling without MLPlan factor...");
        DataSet result = DataSetUtils.subsample(data, 0.01, 200, new Random(0));
        System.out.println("Done. " + result.getInstances().numInstances());

        System.out.println("Start subsampling with custom MLPlan factor...");
        result = DataSetUtils.subsample(data, 0.05, 200, new Random(0));
        System.out.println("Done. " + result.getInstances().numInstances());

        System.out.println("Start subsampling with MLPlan factor...");
        result = DataSetUtils.subsample(data, 0.01, 200, new Random(10), 10);
        System.out.println("Done. " + result.getInstances().numInstances());
        Assert.assertTrue(result.getInstances().numInstances() > 0);
        Assert.assertTrue(result.getIntermediateInstances().size() > 0);
    }

    // @Test
    public void updateIntermediateInstancesTest() throws Exception {
        DataSet data = DataSetUtils.getDataSetByID(FASHION_MNIST_ID);
        data = DataSetUtils.getStratifiedSplit(data, new Random(42), 0.1).get(0);
        WEKAFilter filter = new WEKAFilter(new Standardize());

        DataSet result = filter.applyFilter(data, false);
        System.out.println(result.getInstances().get(0).value(0));
        System.out.println(result.getIntermediateInstances().get(0).getDouble(0));

        System.out.println(result.getInstances().numAttributes());
        System.out.println(result.getIntermediateInstances().get(0).shapeInfoToString());

        Assert.assertEquals(result.getInstances().get(0).value(0),
                result.getIntermediateInstances().get(0).getDouble(0), 0.01);
    }
}
