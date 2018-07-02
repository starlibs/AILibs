package autofe.util;

import java.util.Arrays;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
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
import autofe.algorithm.hasco.filter.image.CatalanoWrapperFilter;

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

	public static CatalanoWrapperFilter getCatalanoFilterByName(final String name) {
		switch (name) {
		case "GaussianBlur":
			return new CatalanoWrapperFilter(new GaussianBlur(), false);
		case "SobelEdgeDetector":
			return new CatalanoWrapperFilter(new SobelEdgeDetector(), true);
		case "ExtractBoundary":
			return new CatalanoWrapperFilter(new ExtractBoundary(), true);
		case "DifferenceEdgeDetector":
			return new CatalanoWrapperFilter(new DifferenceEdgeDetector(), true);
		case "Erosion":
			return new CatalanoWrapperFilter(new Erosion(), false);
		case "FastVariance":
			return new CatalanoWrapperFilter(new FastVariance(), false);
		case "Emboss":
			return new CatalanoWrapperFilter(new Emboss(), false);
		case "GaborFilter":
			return new CatalanoWrapperFilter(new GaborFilter(), true);
		case "HighBoost":
			return new CatalanoWrapperFilter(new HighBoost(), false);
		case "HomogenityEdgeDetector":
			return new CatalanoWrapperFilter(new HomogenityEdgeDetector(), true);
		case "HorizontalRunLengthSmoothing":
			return new CatalanoWrapperFilter(new HorizontalRunLengthSmoothing(), true);
		case "ImageQuantization":
			return new CatalanoWrapperFilter(new ImageQuantization(), true);
		case "ImageNormalization":
			return new CatalanoWrapperFilter(new ImageNormalization(), true);
		case "MorphologicGradientImage":
			return new CatalanoWrapperFilter(new MorphologicGradientImage(), true);
		case "KirschCompassEdgeDetector":
			return new CatalanoWrapperFilter(new KirschCompassEdgeDetector(), false);
		case "IsotropicCompassEdgeDetector":
			return new CatalanoWrapperFilter(new IsotropicCompassEdgeDetector(), false);
		case "RobertsCrossEdgeDetector":
			return new CatalanoWrapperFilter(new RobertsCrossEdgeDetector(), true);
		case "SobelCompassEdgeDetector":
			return new CatalanoWrapperFilter(new SobelCompassEdgeDetector(), false);
		case "WeightedMedian":
			return new CatalanoWrapperFilter(new WeightedMedian(), false);
		case "None":
			return new CatalanoWrapperFilter(null, false);
		default:
			return new CatalanoWrapperFilter(new GaussianBlur(), false);
		}
	}
}
