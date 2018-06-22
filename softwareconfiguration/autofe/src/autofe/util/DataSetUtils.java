package autofe.util;

import java.awt.image.BufferedImage;

import Catalano.Imaging.FastBitmap;
import weka.core.Instance;

public final class DataSetUtils {
	private DataSetUtils() {
		// Utility class
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
}
