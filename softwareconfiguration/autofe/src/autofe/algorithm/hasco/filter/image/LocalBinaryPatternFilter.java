package autofe.algorithm.hasco.filter.image;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.Texture.BinaryPattern.LocalBinaryPattern;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

public class LocalBinaryPatternFilter implements IFilter, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 924262565754950582L;

	private LocalBinaryPattern lbp = new LocalBinaryPattern();

	@Override
	public DataSet applyFilter(final DataSet inputData, final boolean copy) {

		if (inputData.getIntermediateInstances() == null || inputData.getIntermediateInstances().size() == 0
				|| inputData.getIntermediateInstances().get(0).rank() < 2) {
			throw new IllegalArgumentException(
					"Intermediate instances must have a rank of at least 2 for image processing.");
		}

		ColorSpace colorSpace = ImageUtils.determineColorSpace(inputData.getIntermediateInstances().get(0));

		// Assume to deal with FastBitmap instances
		List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
		for (INDArray inst : inputData.getIntermediateInstances()) {
			FastBitmap bitmap = ImageUtils.matrixToFastBitmap(inst, colorSpace);
			if (colorSpace != ColorSpace.Grayscale) {
				bitmap.toGrayscale();
			}
			bitmap = this.lbp.toFastBitmap(bitmap);

			INDArray result = ImageUtils.fastBitmapToMatrix(bitmap, ColorSpace.Grayscale);
			transformedInstances.add(result);
		}

		return new DataSet(inputData.getInstances(), transformedInstances);
	}

	@Override
	public String toString() {
		return LocalBinaryPattern.class.getName() + "-[]";
	}

	@Override
	public LocalBinaryPatternFilter clone() throws CloneNotSupportedException {
		return new LocalBinaryPatternFilter();
	}

}
