package autofe.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public final class DataSetUtils {

	public static final int CIFAR10_ID = 40927;
	public static final int SEGMENT_ID = 40984;
	public static final int MNIST_ID = 554;

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
		default:
			logger.warn("Could not infer data set of instance to generate matrix. Returning null...");
			return null;
		}
	}

	// 28 / 28 / 1
	public static INDArray mnistInstanceToMatrix(final Instance instance) {
		INDArray result = Nd4j.create(28, 28);
		double[] imageValues = instance.toDoubleArray();
		if (imageValues.length != (28 * 28 + 1))
			throw new IllegalArgumentException("MNIST instances must have the dimensionality of 28 x 28 x 1!");

		for (int i = 0; i < 28; i++) {
			for (int j = 0; j < 28; j++) {
				int offset = i + 1;
				result.putScalar(i, j, imageValues[offset * j]);
			}
		}
		return result;
	}

	// 1024 / 1024 / 1024: red / green / blue channel
	public static FastBitmap cifar10InstanceToBitmap(final Instance instance) {
		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		double[] imageValues = instance.toDoubleArray();
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				int offset = (i + 1);
				int a = 255;
				int r = (int) imageValues[offset * j];
				int g = (int) imageValues[1024 + offset * j];
				int b = (int) imageValues[2048 + offset * j];
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
		INDArray result = Nd4j.create(32, 32, 3);
		double[] imageValues = instance.toDoubleArray();
		if (imageValues.length != (32 * 32 * 3 + 1))
			throw new IllegalArgumentException("Cifar 10 instances must have the dimensionality of 32 x 32 x 3!");

		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				int offset = i + 1;
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

		if (images == null || images.size() == 0)
			throw new IllegalArgumentException("Parameter 'images' must not be null or empty!");

		// Create attributes
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < images.iterator().next().getRGBData().length; i++) {
			attributes.add(new Attribute("val" + i));
		}
		attributes.add(refInstances.classAttribute());

		Instances result = new Instances("Instances", attributes, refInstances.size());
		result.setClassIndex(result.numAttributes() - 1);

		for (int i = 0; i < images.size(); i++) {
			FastBitmap image = images.get(i);
			int[] rgbData = image.getRGBData();
			Instance inst = new DenseInstance(rgbData.length + 1);
			for (int j = 0; j < rgbData.length; i++) {
				inst.setValue(j, rgbData[j]);
			}
			inst.setClassValue(refInstances.get(i).classValue());
		}

		return result;
	}

	public static Instances matricesToInstances(final List<INDArray> matrices, final Instances refInstances) {
		if (matrices == null || matrices.size() == 0)
			throw new IllegalArgumentException("Parameter 'matrices' must not be null or empty!");

		// Create attributes
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < matrices.get(0).length(); i++) {
			attributes.add(new Attribute("val" + i));
		}
		attributes.add(refInstances.classAttribute());

		Instances result = new Instances("Instances", attributes, refInstances.size());
		result.setClassIndex(result.numAttributes() - 1);

		for (int i = 0; i < matrices.size(); i++) {

			// Initialize instance
			Instance inst = new DenseInstance(attributes.size());
			inst.setDataset(result);

			// Update instance entries
			INDArray matrix = matrices.get(i);
			for (int j = 0; j < matrix.length(); j++) {
				inst.setValue(j, matrix.getDouble(j));
			}

			// Set class value
			inst.setClassValue(refInstances.get(i).classValue());

			result.add(inst);
		}

		return result;

	}
}
