package autofe.algorithm.hasco.filter.image;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.Texture.BinaryPattern.LocalBinaryPattern;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

public class LocalBinaryPatternFilter implements IFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(LocalBinaryPatternFilter.class);

	private LocalBinaryPattern lbp = new LocalBinaryPattern();

	@Override
	public DataSet applyFilter(final DataSet inputData, final boolean copy) {

		if(inputData.getIntermediateInstances() == null || inputData.getIntermediateInstances().size() == 0 ||
				inputData.getIntermediateInstances().get(0).rank() < 2)
			throw new IllegalArgumentException("Intermediate instances must have a rank of at least 2 for image processing.");
		
		ColorSpace colorSpace = null;
		switch(inputData.getIntermediateInstances().get(0).rank()) {
		case 2:
			// Greyscale
			colorSpace = ColorSpace.Grayscale;
			break;
		case 3:
			if(inputData.getIntermediateInstances().get(0).shape()[2] == 3) {
				// RGB
				colorSpace = ColorSpace.RGB;
			} else {
				// ARGB
				colorSpace = ColorSpace.ARGB;
			}
			break;
		default:
			colorSpace = ColorSpace.RGB;
		}
		
		// TODO: Check for copy flag
		
		// Assume to deal with FastBitmap instances
		List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
		for (INDArray inst : inputData.getIntermediateInstances()) {
			FastBitmap bitmap = ImageUtils.matrixToFastBitmap(inst, colorSpace);
			bitmap = lbp.toFastBitmap(bitmap);
			INDArray result = null;
			double[][][] bitmapMatrix = bitmap.toMatrixRGBAsDouble();
			
			switch(colorSpace) {
			case Grayscale:
				result = Nd4j.create(bitmap.toMatrixGrayAsDouble());
				break;
			case ARGB:
				result = Nd4j.create(bitmap.getWidth(), bitmap.getHeight(), 4);
				
				for(int i=0; i< bitmap.getWidth(); i++) {
					for(int j=0; j<bitmap.getHeight(); j++) {
						result.put(new int[] {i, j}, Nd4j.create(ArrayUtils.addAll(new double[] {bitmap.getAlpha(i, j)}, bitmapMatrix[i][j])));
					}
				}
				break;
			case RGB:
				result = Nd4j.create(bitmap.getWidth(), bitmap.getHeight(), 3);
				for(int i=0; i<bitmap.getWidth(); i++) {
					for(int j=0; j<bitmap.getHeight(); j++) {
						result.put(new int[] {i,j}, Nd4j.create(bitmapMatrix[i][j]));
					}
				}
				break;
			default:
				logger.warn("Could not determine color space. Saving an empty matrix...");
			}
			transformedInstances.add(result);
		}

		return new DataSet(inputData.getInstances(), transformedInstances);
	}

}
