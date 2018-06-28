package autofe.util;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;

public final class ImageUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);
	
	private ImageUtils() {
		// Utility class
	}
	
	public static FastBitmap matrixToFastBitmap(final INDArray matrix, final ColorSpace colorSpace) {
		int[] shape = matrix.shape();
		FastBitmap bitmap = new FastBitmap(shape[0], shape[1], colorSpace);
		
		for(int i=0; i<shape[0]; i++) {
			for(int j=0; j<shape[1]; j++) {
				switch(colorSpace) {
				case ARGB:
					bitmap.setAlpha(i, j, matrix.getInt(i, j, 0));
					bitmap.setRed(i, j, matrix.getInt(i, j, 1));
					bitmap.setBlue(i, j, matrix.getInt(i, j, 2));
					bitmap.setGreen(i, j, matrix.getInt(i, j, 3));
					break;
				case Grayscale:
					bitmap.setGray(i, j, matrix.getInt(i, j));
					break;
				case RGB:
					bitmap.setRed(i, j, matrix.getInt(i, j, 0));
					bitmap.setBlue(i, j, matrix.getInt(i, j, 1));
					bitmap.setGreen(i, j, matrix.getInt(i, j, 2));
					break;
				default:
					logger.warn("Could not initialize FastBitmap due to lack of color space information.");
					break;
				}
			}
		}
		return bitmap;
	}
}
