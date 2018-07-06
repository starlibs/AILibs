package autofe.algorithm.hasco.filter.image;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.IExtract;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

public class CatalanoExtractFilter extends AbstractCatalanoFilter<IExtract> {

	private boolean requiresRGB;

	public CatalanoExtractFilter(final IExtract extractFilter, final boolean requiresGrayscale,
			final boolean requiresRGB) {
		this.setCatalanoFilter(extractFilter);
		this.setRequiresGrayscale(requiresGrayscale);
		this.requiresRGB = requiresRGB;
	}

	@Override
	public DataSet applyFilter(DataSet inputData, boolean copy) {
		if (inputData.getIntermediateInstances() == null || inputData.getIntermediateInstances().size() == 0
				|| inputData.getIntermediateInstances().get(0).rank() < 2)
			throw new IllegalArgumentException(
					"Intermediate instances must have a rank of at least 2 for image processing.");

		// None filter
		if (this.getCatalanoFilter() == null) {
			if (copy)
				return inputData.copy();
			else
				return inputData;
		}

		ColorSpace colorSpace = ImageUtils.determineColorSpace(inputData.getIntermediateInstances().get(0));

		// Assume to deal with FastBitmap instances
		List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
		for (INDArray inst : inputData.getIntermediateInstances()) {
			FastBitmap bitmap = ImageUtils.matrixToFastBitmap(inst, colorSpace);
			if (this.requiresRGB)
				bitmap.toRGB();
			else if (colorSpace != ColorSpace.Grayscale && this.isRequiresGrayscale())
				bitmap.toGrayscale();

			FastBitmap transResult = this.getCatalanoFilter().Extract(bitmap);

			INDArray result = ImageUtils.fastBitmapToMatrix(transResult, colorSpace);
			transformedInstances.add(result);
		}

		return new DataSet(inputData.getInstances(), transformedInstances);
	}
}
