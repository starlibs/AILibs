package autofe.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deeplearning4j.nn.conf.CacheMode;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer.AlgoMode;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.zoo.model.AlexNet;
import org.deeplearning4j.zoo.model.LeNet;
import org.deeplearning4j.zoo.model.ResNet50;
import org.deeplearning4j.zoo.model.VGG16;
import org.deeplearning4j.zoo.model.VGG19;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.Tools.ImageHistogram;
import autofe.algorithm.hasco.filter.image.CatalanoBinaryPatternFilter;
import autofe.algorithm.hasco.filter.image.CatalanoInPlaceFilter;
import autofe.algorithm.hasco.filter.image.PretrainedNNFilter;
import autofe.algorithm.hasco.filter.meta.IFilter;

public final class ImageUtils {

	private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);

	private ImageUtils() {
		// Utility class
	}

	public static FastBitmap matrixToFastBitmap(final INDArray matrix, final ColorSpace colorSpace) {
		int[] shape = matrix.shape();
		FastBitmap bitmap = new FastBitmap(shape[0], shape[1], colorSpace);

		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {
				switch (colorSpace) {
				case ARGB:
					bitmap.setAlpha(i, j, matrix.getInt(i, j, 0));
					bitmap.setRed(i, j, matrix.getInt(i, j, 1));
					bitmap.setBlue(i, j, matrix.getInt(i, j, 2));
					bitmap.setGreen(i, j, matrix.getInt(i, j, 3));
					break;
				case RGB:
					bitmap.setRed(i, j, matrix.getInt(i, j, 0));
					bitmap.setBlue(i, j, matrix.getInt(i, j, 1));
					bitmap.setGreen(i, j, matrix.getInt(i, j, 2));
					break;
				case Grayscale:
					try {
						bitmap.setGray((i + 1) * j, matrix.getInt(i, j));
					} catch (Exception e) {
						logger.warn("Got exception: " + e.getMessage());
						logger.warn(Arrays.toString(shape));
						logger.warn("...");
					}
					break;
				default:
					logger.warn("Could not initialize FastBitmap due to lack of color space information.");
					break;
				}
			}
		}
		return bitmap;
	}

	public static INDArray fastBitmapToMatrix(final FastBitmap bitmap, final ColorSpace colorSpace) {
		INDArray result = null;

		double[][][] bitmapMatrix = null;

		switch (colorSpace) {
		case Grayscale:
			result = Nd4j.create(bitmap.toMatrixGrayAsDouble());
			break;
		case ARGB:
			result = Nd4j.create(bitmap.getWidth(), bitmap.getHeight(), 4);
			bitmapMatrix = bitmap.toMatrixRGBAsDouble();

			for (int i = 0; i < bitmap.getWidth(); i++) {
				for (int j = 0; j < bitmap.getHeight(); j++) {
					result.putScalar(new int[] { i, j, 0 }, bitmapMatrix[i][j][0]);
					result.putScalar(new int[] { i, j, 1 }, bitmapMatrix[i][j][1]);
					result.putScalar(new int[] { i, j, 2 }, bitmapMatrix[i][j][2]);
					result.putScalar(new int[] { i, j, 3 }, bitmapMatrix[i][j][3]);
				}
			}
			break;
		case RGB:
			bitmapMatrix = bitmap.toMatrixRGBAsDouble();

			result = Nd4j.create(bitmap.getWidth(), bitmap.getHeight(), 3);
			for (int i = 0; i < bitmap.getWidth(); i++) {
				for (int j = 0; j < bitmap.getHeight(); j++) {
					result.putScalar(new int[] { i, j, 0 }, bitmapMatrix[j][i][0]);
					result.putScalar(new int[] { i, j, 1 }, bitmapMatrix[j][i][1]);
					result.putScalar(new int[] { i, j, 2 }, bitmapMatrix[j][i][2]);
				}
			}
			break;
		default:
			logger.warn("Could not determine color space. Saving an empty matrix...");
			result = Nd4j.create(new double[] {});
		}
		return result;
	}

	public static ColorSpace determineColorSpace(final INDArray example) {
		// ColorSpace colorSpace = null;
		int[] exampleShape = example.shape();
		if (exampleShape.length >= 3) {
			if (exampleShape[2] == 3) {
				return ColorSpace.RGB;
			} else if (exampleShape[2] > 3) {
				return ColorSpace.ARGB;
			}
		}
		return ColorSpace.Grayscale;
	}

	public static IFilter getCatalanoFilterByName(final String name) {
		switch (name) {
		case "GaussianBlur":
		case "SobelEdgeDetector":
		case "ExtractBoundary":
		case "DifferenceEdgeDetector":
		case "Erosion":
		case "FastVariance":
		case "Emboss":
		case "GaborFilter":
		case "HighBoost":
		case "HomogenityEdgeDetector":
		case "HorizontalRunLengthSmoothing":
		case "ImageQuantization":
		case "ImageNormalization":
		case "MorphologicGradientImage":
		case "KirschCompassEdgeDetector":
		case "IsotropicCompassEdgeDetector":
		case "RobertsCrossEdgeDetector":
		case "SobelCompassEdgeDetector":
		case "WeightedMedian":
		case "NonePreprocessor":
			return new CatalanoInPlaceFilter(name);

		/* Binary pattern */
		case "NoneExtractor":
		case "UniformLocalBinaryPattern":
		case "RobustLocalBinaryPattern":
			return new CatalanoBinaryPatternFilter(name);
		// case "ExtractNormalizedRGBChannel":
		// return new CatalanoExtractFilter(new ExtractNormalizedRGBChannel(), false,
		// true);

		default:
			// Return identity
			return new CatalanoInPlaceFilter(null);
		}
	}

	public static PretrainedNNFilter getPretrainedNNFilterByName(final String name, final int layer,
			final int[] shape) {
		switch (name) {
		case "AlexNet":
			return new PretrainedNNFilter(new AlexNet(42, shape, 10, new Nesterovs(1e-2, 0.9), CacheMode.NONE,
					WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape, name);
		case "LeNet":
			return new PretrainedNNFilter(new LeNet(42, shape, 10, new Nesterovs(1e-2, 0.9), CacheMode.NONE,
					WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape, name);
		case "VGG19":
			return new PretrainedNNFilter(new VGG19(42, shape, 10, new Nesterovs(1e-2, 0.9), CacheMode.NONE,
					WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape, name);
		case "ResNet50":
			return new PretrainedNNFilter(new ResNet50(42, shape, 10, WeightInit.DISTRIBUTION, new Nesterovs(1e-2, 0.9),
					CacheMode.NONE, WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape, name);
		default:
			return new PretrainedNNFilter(new VGG16(42, shape, 10, new Nesterovs(1e-2, 0.9), CacheMode.NONE,
					WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape, "VGG16");
		}
	}

	public static INDArray imageHistorgramToMatrix(final ImageHistogram histogram) {

		INDArray result = Nd4j.create(histogram.getValues().length + 9);
		double[] values = histogram.Normalize();
		for (int i = 0; i < values.length; i++) {
			result.putScalar(i, values[i]);
		}
		result.putScalar(values.length, histogram.getMean());
		result.putScalar(values.length + 1, histogram.getStdDev());
		result.putScalar(values.length + 2, histogram.getEntropy());
		result.putScalar(values.length + 3, histogram.getKurtosis());
		result.putScalar(values.length + 4, histogram.getSkewness());
		result.putScalar(values.length + 5, histogram.getMedian());
		result.putScalar(values.length + 6, histogram.getMode());
		result.putScalar(values.length + 7, histogram.getMin());
		result.putScalar(values.length + 8, histogram.getMax());
		return result;
	}

	/**
	 * Converts grayscale matrices to RGB matrices.
	 * 
	 * @param matrices
	 *            Input matrices (Grayscale with single channels, assuming shape
	 *            [width, height, 1, [depth, ...])
	 * @return Returns RGB matrices with same shape but only three channels
	 */
	public static List<INDArray> grayscaleMatricesToRGB(final List<INDArray> matrices) {
		List<INDArray> result = new ArrayList<>(matrices.size());
		for (int i = 0; i < matrices.size(); i++) {
			INDArray currMatrix = matrices.get(i);
			INDArray resultMatrix = Nd4j.create(currMatrix.shape()[0], currMatrix.shape()[1], 3);
			for (int j = 0; j < currMatrix.shape()[0]; j++) {
				for (int k = 0; k < currMatrix.shape()[1]; k++) {
					for (int l = 0; l < 3; l++) {
						resultMatrix.putScalar(new int[] { j, k, l }, currMatrix.getDouble(j, k));
					}
				}
			}
			result.add(resultMatrix);
		}

		return result;
	}

	/**
	 * Converts RGB matrices to grayscale matrices using the Luminosity method (cf.
	 * ITU-R recommendation BT.709).
	 * 
	 * @param matrices
	 *            Input matrices (RGB with 3 channels, assuming shape [width,
	 *            height, 3, [depth, ...])
	 * @return Returns grayscale matrices with same shape but only one channel
	 */
	public static List<INDArray> rgbMatricesToGrayscale(final List<INDArray> matrices) {
		if (matrices == null || matrices.size() < 1 || matrices.get(0).shape().length < 3
				|| matrices.get(0).shape()[2] < 3)
			throw new IllegalArgumentException("Parameter matrices must not be null and must have three channels.");

		List<INDArray> result = new ArrayList<>(matrices.size());
		for (int i = 0; i < matrices.size(); i++) {
			INDArray currMatrix = matrices.get(i);
			INDArray resultMatrix = Nd4j.create(currMatrix.shape()[0], currMatrix.shape()[1], 1);

			for (int j = 0; j < currMatrix.shape()[0]; j++) {
				for (int k = 0; k < currMatrix.shape()[1]; k++) {
					double r = currMatrix.getDouble(j, k, 0);
					double g = currMatrix.getDouble(j, k, 1);
					double b = currMatrix.getDouble(j, k, 2);

					double gray = (int) (r * 0.2125 + g * 0.7154 + b * 0.0721);

					resultMatrix.putScalar(new int[] { j, k, 0 }, gray);
				}
			}

			result.add(resultMatrix);
		}

		return result;
	}
}
