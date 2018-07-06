package autofe.algorithm.hasco.filter.image;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.IApplyInPlace;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

public class CatalanoInPlaceFilter extends AbstractCatalanoFilter<IApplyInPlace> {

	public CatalanoInPlaceFilter(final IApplyInPlace filter, final boolean requiresGrayscale) {
		this.setCatalanoFilter(filter);
		this.setRequiresGrayscale(requiresGrayscale);
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
		List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
		for (INDArray inst : inputData.getIntermediateInstances()) {
			FastBitmap bitmap = ImageUtils.matrixToFastBitmap(inst, colorSpace);

			if (this.isRequiresGrayscale() && colorSpace != ColorSpace.Grayscale)
				bitmap.toGrayscale();

			this.getCatalanoFilter().applyInPlace(bitmap);
			INDArray result = ImageUtils.fastBitmapToMatrix(bitmap, colorSpace);
			transformedInstances.add(result);
		}
		return new DataSet(inputData.getInstances(), transformedInstances);
	}

	@Override
	public String toString() {
		if (this.getCatalanoFilter() != null)
			return "CatalanoWrapperFilter [catalanoFilter=" + this.getCatalanoFilter().getClass().getSimpleName()
					+ ", requiresGrayscale=" + this.isRequiresGrayscale() + "]";
		else {
			return "CatalanoWrapperFilter (empty)";
		}
	}

}
