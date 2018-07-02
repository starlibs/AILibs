package autofe.algorithm.hasco.filter.image;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.IApplyInPlace;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

public class CatalanoWrapperFilter implements IFilter {

	private IApplyInPlace catalanoFilter;
	private boolean requiresGrayscale;

	public CatalanoWrapperFilter(final IApplyInPlace filter, final boolean requiresGrayscale) {
		this.catalanoFilter = filter;
		this.requiresGrayscale = requiresGrayscale;
	}

	@Override
	public DataSet applyFilter(DataSet inputData, boolean copy) {
		if (inputData.getIntermediateInstances() == null || inputData.getIntermediateInstances().size() == 0
				|| inputData.getIntermediateInstances().get(0).rank() < 2)
			throw new IllegalArgumentException(
					"Intermediate instances must have a rank of at least 2 for image processing.");

		// None filter
		if (this.catalanoFilter == null) {
			if (copy)
				return inputData.copy();
			else
				return inputData;
		}

		ColorSpace colorSpace = ImageUtils.determineColorSpace(inputData.getIntermediateInstances().get(0));
		List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
		for (INDArray inst : inputData.getIntermediateInstances()) {
			FastBitmap bitmap = ImageUtils.matrixToFastBitmap(inst, colorSpace);

			if (this.requiresGrayscale && colorSpace != ColorSpace.Grayscale)
				bitmap.toGrayscale();

			this.catalanoFilter.applyInPlace(bitmap);
			INDArray result = ImageUtils.fastBitmapToMatrix(bitmap, colorSpace);
			transformedInstances.add(result);
		}
		return new DataSet(inputData.getInstances(), transformedInstances);
	}

	@Override
	public String toString() {
		if (this.catalanoFilter != null)
			return "CatalanoWrapperFilter [catalanoFilter=" + catalanoFilter.getClass().getSimpleName()
					+ ", requiresGrayscale=" + requiresGrayscale + "]";
		else {
			return "CatalanoWrapperFilter (empty)";
		}
	}

}
