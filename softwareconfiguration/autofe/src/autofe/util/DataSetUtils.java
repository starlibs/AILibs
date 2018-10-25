package autofe.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import jaicore.ml.WekaUtil;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Utility class for handling imported data sets.
 *
 * @author Julian Lienen
 *
 */
public final class DataSetUtils {

	public static final String API_KEY = "4350e421cdc16404033ef1812ea38c01";

	public static final int CIFAR10_ID = 40927;
	public static final long[] CIFAR10_INPUT_SHAPE = new long[] { 32, 32, 3 };
	public static final int SEGMENT_ID = 40984;
	public static final int MNIST_ID = 554;
	public static final long[] MNIST_INPUT_SHAPE = new long[] { 28, 28, 1 };
	public static final int FASHION_MNIST_ID = 40996;
	// public static final int[] FASHION_MNIST_SHAPE = new int[] { 1, 1, 28, 28 };
	public static final long[] FASHION_MNIST_SHAPE = new long[] { 28, 28, 1 };

	private static final Logger logger = LoggerFactory.getLogger(DataSetUtils.class);

	private DataSetUtils() {
		// Utility class
	}

	public static INDArray instanceToMatrixByDataSet(final Instance instance, final int datasetID) {
		switch (datasetID) {
		case CIFAR10_ID:
			return cifar10InstanceToMatrix(instance);
		case MNIST_ID:
			return mnistInstanceToMatrix(instance);
		case FASHION_MNIST_ID:
			return mnistInstanceToMatrix(instance);
		default:
			logger.warn("Could not infer data set of instance to generate matrix. Returning null...");
			return null;
		}
	}

	// 28 / 28 / 1
	public static INDArray mnistInstanceToMatrix(final Instance instance) {
		final INDArray result = Nd4j.create(28, 28);
		final double[] imageValues = instance.toDoubleArray();
		if (imageValues.length != (28 * 28 + 1)) {
			throw new IllegalArgumentException("MNIST instances must have the dimensionality of 28 x 28 x 1!");
		}

		for (int i = 0; i < 28; i++) {
			for (int j = 0; j < 28; j++) {
				final int offset = i + 1;
				result.putScalar(i, j, imageValues[offset * j]);
			}
		}
		return result;
	}

	// 1024 / 1024 / 1024: red / green / blue channel
	public static FastBitmap cifar10InstanceToBitmap(final Instance instance) {
		final BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		final double[] imageValues = instance.toDoubleArray();
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				final int offset = (i + 1);
				final int a = 255;
				final int r = (int) imageValues[offset * j];
				final int g = (int) imageValues[1024 + offset * j];
				final int b = (int) imageValues[2048 + offset * j];
				int p = 0;
				p = p | (a << 24);
				p = p | (r << 16);
				p = p | (g << 8);
				p = p | b;
				image.setRGB(i, j, p);
			}
		}
		return new FastBitmap(image);
	}

	// 1024 / 1024 / 1024: red / green / blue channel
	public static INDArray cifar10InstanceToMatrix(final Instance instance) {
		final INDArray result = Nd4j.create(32, 32, 3);
		final double[] imageValues = instance.toDoubleArray();
		if (imageValues.length != (32 * 32 * 3 + 1)) {
			throw new IllegalArgumentException("Cifar 10 instances must have the dimensionality of 32 x 32 x 3!");
		}

		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				final int offset = i + 1;
				// INDArray rgb = Nd4j.create(new double[] { imageValues[offset * j],
				// imageValues[1024 + offset * j],
				// imageValues[2048 + offset * j] });
				result.putScalar(new int[] { i, j, 0 }, imageValues[offset * j]);
				result.putScalar(new int[] { i, j, 1 }, imageValues[1024 + offset * j]);
				result.putScalar(new int[] { i, j, 2 }, imageValues[2048 + offset * j]);
			}
		}

		return result;
	}

	public static Instances bitmapsToInstances(final List<FastBitmap> images, final Instances refInstances) {

		if (images == null || images.size() == 0) {
			throw new IllegalArgumentException("Parameter 'images' must not be null or empty!");
		}

		// Create attributes
		final ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < images.iterator().next().getRGBData().length; i++) {
			attributes.add(new Attribute("val" + i));
		}

		// Add class attribute
		final List<String> classValues = IntStream.range(0, refInstances.classAttribute().numValues()).asDoubleStream()
				.mapToObj(d -> String.valueOf(d)).collect(Collectors.toList());
		final Attribute classAtt = new Attribute("classAtt", classValues);
		attributes.add(classAtt);

		final Instances result = new Instances("Instances", attributes, refInstances.size());
		result.setClassIndex(result.numAttributes() - 1);

		for (int i = 0; i < images.size(); i++) {
			final FastBitmap image = images.get(i);
			final int[] rgbData = image.getRGBData();
			final Instance inst = new DenseInstance(rgbData.length + 1);
			inst.setDataset(result);

			for (final int j = 0; j < rgbData.length; i++) {
				inst.setValue(j, rgbData[j]);
			}

			inst.setClassValue(refInstances.get(i).classValue());
		}

		return result;
	}

	public static Instances matricesToInstances(final DataSet data) {
		return matricesToInstances(data.getIntermediateInstances(), data.getInstances());
	}

	public static Instances matricesToInstances(final List<INDArray> matrices, final Instances refInstances) {
		if (matrices == null || matrices.size() == 0) {
			throw new IllegalArgumentException("Parameter 'matrices' must not be null or empty!");
		}

		// Create attributes
		final ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < matrices.get(0).length(); i++) {
			final Attribute newAtt = new Attribute("val" + i);
			attributes.add(newAtt);
		}

		final List<String> classValues = IntStream.range(0, refInstances.classAttribute().numValues()).asDoubleStream()
				.mapToObj(d -> String.valueOf(d)).collect(Collectors.toList());
		final Attribute classAtt = new Attribute("classAtt", classValues);
		attributes.add(classAtt);

		final Instances result = new Instances("Instances", attributes, refInstances.size());
		result.setClassIndex(result.numAttributes() - 1);

		for (int i = 0; i < matrices.size(); i++) {

			// Initialize instance
			// Instance inst = new DenseInstance(attributes.size());
			final Instance inst = new DenseInstance(1,
					ArrayUtils.addAll(Nd4j.toFlattened(matrices.get(i)).toDoubleVector(), 0));
			inst.setDataset(result);

			// Set class value
			inst.setClassValue(refInstances.get(i).classValue());

			result.add(inst);
		}

		return result;

	}

	public static Instance matrixToInstance(final INDArray instance, final Instances refInstances) {
		if (instance == null || refInstances == null) {
			throw new IllegalArgumentException("Parameter 'instance' and 'refInstances' must not be null!");
		}

		// Create attributes
		final ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < instance.length(); i++) {
			final Attribute newAtt = new Attribute("val" + i);
			attributes.add(newAtt);
		}

		final List<String> classValues = IntStream.range(0, refInstances.classAttribute().numValues()).asDoubleStream()
				.mapToObj(d -> String.valueOf(d)).collect(Collectors.toList());
		final Attribute classAtt = new Attribute("classAtt", classValues);
		attributes.add(classAtt);

		final Instances result = new Instances("Instances", attributes, refInstances.size());
		result.setClassIndex(result.numAttributes() - 1);

		// Initialize instance
		// Instance inst = new DenseInstance(attributes.size());
		final Instance inst = new DenseInstance(1, ArrayUtils.addAll(Nd4j.toFlattened(instance).toDoubleVector(), 0));
		inst.setDataset(result);

		return inst;
	}

	public static long[] getInputShapeByDataSet(final int dataSetID) {
		switch (dataSetID) {
		case MNIST_ID:
			return MNIST_INPUT_SHAPE;
		case CIFAR10_ID:
			return CIFAR10_INPUT_SHAPE;
		case FASHION_MNIST_ID:
			return MNIST_INPUT_SHAPE;
		default:
			return new long[] { 1 };
		}
	}

	/**
	 * Function determining how much of the input data should be used to infer a
	 * filter pipeline.
	 *
	 * @param instances
	 *            Complete input instances
	 * @return Returns a split ratio where this double indicates which percentage of
	 *         the given input instances should be used to automatically generate
	 *         features.
	 */
	public static double getSplitRatioToUse(final Instances instances) {
		if (instances == null) {
			throw new IllegalArgumentException("Parameter 'instances' must not be null!");
		}

		final int numAttributes = instances.numAttributes();
		final int numInstances = instances.numInstances();

		if (numAttributes > 1000 && numInstances > 10000) {
			return 3000d / numInstances;
		} else if (numAttributes > 500 && numInstances > 7500) {
			return 5000d / numInstances;
		}

		return 1d;
	}

	public static int getDataSetIDByName(final String dataSetName) {
		if (dataSetName == null || dataSetName.equals("")) {
			throw new IllegalArgumentException("Parameter 'dataSetName' must not be null or empty!");
		}

		switch (dataSetName) {
		case "mnist":
			return DataSetUtils.MNIST_ID;
		case "cifar10":
			return DataSetUtils.CIFAR10_ID;
		case "fashion-mnist":
			return DataSetUtils.FASHION_MNIST_ID;
		default:
			throw new UnsupportedOperationException("Data set '" + dataSetName + "' is not supported yet.");
		}
	}

	public static DataSet loadDatasetFromImageFolder(final File folder) throws IOException {
		final List<String> classValues = new LinkedList<>();
		for (final File subFolder : folder.listFiles()) {
			classValues.add(subFolder.getName());
		}
		final ArrayList<Attribute> attributeList = new ArrayList<>();
		final Attribute classAttribute = new Attribute("class", classValues);
		attributeList.add(classAttribute);

		final Instances refInstances = new Instances("refInstances", attributeList, 0);
		refInstances.setClass(classAttribute);

		final List<INDArray> matrixList = new LinkedList<>();

		for (final File subFolder : folder.listFiles()) {
			final String className = subFolder.getName();
			for (final File imageFile : subFolder.listFiles()) {
				final FastBitmap fb = new FastBitmap(ImageIO.read(imageFile));
				INDArray matrix = null;
				try {
					matrix = ImageUtils.fastBitmapToMatrix(fb, fb.getColorSpace());
				} catch (final Throwable e) {
					System.out.println(imageFile.getAbsolutePath());
					e.printStackTrace();
				}
				matrixList.add(matrix);
				final DenseInstance nI = new DenseInstance(attributeList.size());
				nI.setValue(classAttribute, className);
				nI.setDataset(refInstances);
				refInstances.add(nI);
			}
		}

		return new DataSet(refInstances, matrixList);
	}

	public static List<DataSet> getStratifiedSplit(final DataSet data, final Random rand, final double... portions) {
		final List<DataSet> splits = new LinkedList<>();

		final Collection<Integer>[] indices = WekaUtil.getStratifiedSplitIndices(data.getInstances(), new Random(),
				portions);

		for (final Collection<Integer> splitIndices : indices) {
			final List<INDArray> indArray = new LinkedList<>();
			final Instances refInstances = new Instances(data.getInstances(), 0);

			for (final Integer index : splitIndices) {
				indArray.add(data.getIntermediateInstances().get(index));
				refInstances.add(data.getInstances().get(index));
			}

			splits.add(new DataSet(refInstances, indArray));
		}

		return splits;
	}

	public static DataSet getDataSetByID(final int datasetID) throws Exception {
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(datasetID);
		File file = ds.getDataset(API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);

		List<INDArray> indArrayList = new LinkedList<>();
		for (Instance i : data) {
			indArrayList.add(instanceToMatrixByDataSet(i, datasetID));
		}

		return new DataSet(data, indArrayList);
	}

	public static DataSet subsample(final DataSet originalData, final double subsampleRatio,
			final int minInstances, final Random random) {
		return subsample(originalData, subsampleRatio, minInstances, random, 1d);
	}
	
	/**
	 * Utility function subsampling the given <code>originalData</code>
	 * @param originalData
	 * @param subsampleRatio
	 * @param minInstances
	 * @param random
	 * @param factor
	 * @return
	 */
	public static DataSet subsample(final DataSet originalData, final double subsampleRatio,
			final int minInstances, final Random random, final double factor) {
		if(subsampleRatio>=1d) {
			logger.debug("Subsampling is not performed.");
			return originalData;
		}
			
		
		double ratio = subsampleRatio * factor;
		if (originalData.getInstances().size() * ratio < minInstances) {
			ratio = (double) minInstances / originalData.getInstances().size();
		}
		DataSet subsampledData = getStratifiedSplit(originalData, random, subsampleRatio).get(0);
		logger.debug("Subsampling ratio is {} and means {} many instances.", ratio, subsampledData.getInstances().size());
		return subsampledData;
	}
}
