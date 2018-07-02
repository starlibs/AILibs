package autofe.util.test;

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
		INDArray matrix = Nd4j.ones(10, 10, 3);
		ColorSpace cs = ColorSpace.RGB;
		FastBitmap bitmap = ImageUtils.matrixToFastBitmap(matrix, cs);

		INDArray result = ImageUtils.fastBitmapToMatrix(bitmap, cs);

		Assert.assertEquals(matrix, result);

	}

	@Test
	public void determineColorSpaceTest() {
		Assert.assertEquals(ColorSpace.RGB, ImageUtils.determineColorSpace(Nd4j.ones(10, 10, 3)));
		Assert.assertEquals(ColorSpace.ARGB, ImageUtils.determineColorSpace(Nd4j.ones(10, 10, 4)));
		Assert.assertEquals(ColorSpace.Grayscale, ImageUtils.determineColorSpace(Nd4j.ones(10, 10, 1)));
		Assert.assertEquals(ColorSpace.Grayscale, ImageUtils.determineColorSpace(Nd4j.ones(10, 10, 2)));
	}
}
