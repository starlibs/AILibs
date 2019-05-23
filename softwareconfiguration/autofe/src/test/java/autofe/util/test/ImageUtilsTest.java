package autofe.util.test;

import java.util.Arrays;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import autofe.util.ImageUtils;
import junit.framework.Assert;

public class ImageUtilsTest {
	@Test
	public void matrixToFastBitmapTest() {
		INDArray matrix = Nd4j.ones(10, 10, 3);
		ColorSpace cs = ColorSpace.RGB;
		FastBitmap result = ImageUtils.matrixToFastBitmap(matrix, cs);
		Assert.assertEquals(ColorSpace.RGB, result.getColorSpace());
		Assert.assertEquals(10 * 10, result.getSize());
		Assert.assertEquals(1, result.getRed(2, 2));
	}

	@Test
	public void fastBitmapToMatrixTest() {
		INDArray matrix = Nd4j.ones(10, 20, 3);
		ColorSpace cs = ColorSpace.RGB;
		FastBitmap bitmap = ImageUtils.matrixToFastBitmap(matrix, cs);

		INDArray result = ImageUtils.fastBitmapToMatrix(bitmap, cs);

		Assert.assertEquals(Arrays.toString(matrix.shape()), Arrays.toString(result.shape()));

	}

	@Test
	public void determineColorSpaceTest() {
		Assert.assertEquals(ColorSpace.RGB, ImageUtils.determineColorSpace(Nd4j.ones(10, 10, 3)));
		Assert.assertEquals(ColorSpace.ARGB, ImageUtils.determineColorSpace(Nd4j.ones(10, 10, 4)));
		Assert.assertEquals(ColorSpace.Grayscale, ImageUtils.determineColorSpace(Nd4j.ones(10, 10, 1)));
		Assert.assertEquals(ColorSpace.Grayscale, ImageUtils.determineColorSpace(Nd4j.ones(10, 10, 2)));
	}

	@Test
	public void rgbMatricesToGrayscaleTest() {
		INDArray matrix = Nd4j.ones(30, 30, 3);
		INDArray transformedMatrix = ImageUtils.rgbMatricesToGrayscale(Arrays.asList(matrix)).get(0);
		Assert.assertEquals(1, transformedMatrix.shape()[2]);
		// Expected value corresponds to (1 * 0.2125 + 1 * 0.7154 + 1 * 0.07)
		Assert.assertEquals(1.0, transformedMatrix.getDouble(0, 0, 0));
	}
}
