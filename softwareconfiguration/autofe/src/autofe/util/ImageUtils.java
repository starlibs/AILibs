package autofe.util;

import java.util.Arrays;

import org.deeplearning4j.nn.conf.CacheMode;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer.AlgoMode;
import org.deeplearning4j.zoo.model.AlexNet;
import org.deeplearning4j.zoo.model.LeNet;
import org.deeplearning4j.zoo.model.VGG16;
import org.deeplearning4j.zoo.model.VGG19;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.Filters.DifferenceEdgeDetector;
import Catalano.Imaging.Filters.Emboss;
import Catalano.Imaging.Filters.Erosion;
import Catalano.Imaging.Filters.ExtractBoundary;
import Catalano.Imaging.Filters.FastVariance;
import Catalano.Imaging.Filters.GaborFilter;
import Catalano.Imaging.Filters.GaussianBlur;
import Catalano.Imaging.Filters.HighBoost;
import Catalano.Imaging.Filters.HomogenityEdgeDetector;
import Catalano.Imaging.Filters.HorizontalRunLengthSmoothing;
import Catalano.Imaging.Filters.ImageNormalization;
import Catalano.Imaging.Filters.ImageQuantization;
import Catalano.Imaging.Filters.IsotropicCompassEdgeDetector;
import Catalano.Imaging.Filters.KirschCompassEdgeDetector;
import Catalano.Imaging.Filters.MorphologicGradientImage;
import Catalano.Imaging.Filters.RobertsCrossEdgeDetector;
import Catalano.Imaging.Filters.SobelCompassEdgeDetector;
import Catalano.Imaging.Filters.SobelEdgeDetector;
import Catalano.Imaging.Filters.WeightedMedian;
import Catalano.Imaging.Texture.BinaryPattern.RobustLocalBinaryPattern;
import Catalano.Imaging.Texture.BinaryPattern.UniformLocalBinaryPattern;
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
					return bitmap;

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
					result.putScalar(new int[] { i, j, 0 }, bitmapMatrix[i][j][0]);
					result.putScalar(new int[] { i, j, 1 }, bitmapMatrix[i][j][1]);
					result.putScalar(new int[] { i, j, 2 }, bitmapMatrix[i][j][2]);
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
			return new CatalanoInPlaceFilter(new GaussianBlur(), false);
		case "SobelEdgeDetector":
			return new CatalanoInPlaceFilter(new SobelEdgeDetector(), true);
		case "ExtractBoundary":
			return new CatalanoInPlaceFilter(new ExtractBoundary(), true);
		case "DifferenceEdgeDetector":
			return new CatalanoInPlaceFilter(new DifferenceEdgeDetector(), true);
		case "Erosion":
			return new CatalanoInPlaceFilter(new Erosion(), false);
		case "FastVariance":
			return new CatalanoInPlaceFilter(new FastVariance(), false);
		case "Emboss":
			return new CatalanoInPlaceFilter(new Emboss(), false);
		case "GaborFilter":
			return new CatalanoInPlaceFilter(new GaborFilter(), true);
		case "HighBoost":
			return new CatalanoInPlaceFilter(new HighBoost(), false);
		case "HomogenityEdgeDetector":
			return new CatalanoInPlaceFilter(new HomogenityEdgeDetector(), true);
		case "HorizontalRunLengthSmoothing":
			return new CatalanoInPlaceFilter(new HorizontalRunLengthSmoothing(), true);
		case "ImageQuantization":
			return new CatalanoInPlaceFilter(new ImageQuantization(), true);
		case "ImageNormalization":
			return new CatalanoInPlaceFilter(new ImageNormalization(), true);
		case "MorphologicGradientImage":
			return new CatalanoInPlaceFilter(new MorphologicGradientImage(), true);
		case "KirschCompassEdgeDetector":
			return new CatalanoInPlaceFilter(new KirschCompassEdgeDetector(), false);
		case "IsotropicCompassEdgeDetector":
			return new CatalanoInPlaceFilter(new IsotropicCompassEdgeDetector(), false);
		case "RobertsCrossEdgeDetector":
			return new CatalanoInPlaceFilter(new RobertsCrossEdgeDetector(), true);
		case "SobelCompassEdgeDetector":
			return new CatalanoInPlaceFilter(new SobelCompassEdgeDetector(), false);
		case "WeightedMedian":
			return new CatalanoInPlaceFilter(new WeightedMedian(), false);
		case "NonePreprocessor":
			return new CatalanoInPlaceFilter(null, false);

		/* Binary pattern */
		case "NoneExtractor":
			return new CatalanoBinaryPatternFilter(null, false);
		case "UniformLocalBinaryPattern":
			return new CatalanoBinaryPatternFilter(new UniformLocalBinaryPattern(), true);
		case "RobustLocalBinaryPattern":
			return new CatalanoBinaryPatternFilter(new RobustLocalBinaryPattern(), true);
		// case "ExtractNormalizedRGBChannel":
		// return new CatalanoExtractFilter(new ExtractNormalizedRGBChannel(), false,
		// true);

		default:
			// Return identity
			return new CatalanoInPlaceFilter(null, false);
		}
	}

	public static PretrainedNNFilter getPretrainedNNFilterByName(String name, final int layer, final int[] shape) {
		switch (name) {
		case "VGG16":
			return new PretrainedNNFilter(new VGG16(42, shape, 10, new Nesterovs(1e-2, 0.9), CacheMode.NONE,
					WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape);
		case "AlexNet":
			return new PretrainedNNFilter(new AlexNet(42, shape, 10, new Nesterovs(1e-2, 0.9), CacheMode.NONE,
					WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape);
		case "LeNet":
			return new PretrainedNNFilter(new LeNet(42, shape, 10, new Nesterovs(1e-2, 0.9), CacheMode.NONE,
					WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape);
		case "VGG19":
			return new PretrainedNNFilter(new VGG19(42, shape, 10, new Nesterovs(1e-2, 0.9), CacheMode.NONE,
					WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape);
		default:
			return new PretrainedNNFilter(new VGG16(42, shape, 10, new Nesterovs(1e-2, 0.9), CacheMode.NONE,
					WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), layer, shape);
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
}
