package autofe.algorithm.hasco.filter.image;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.Texture.BinaryPattern.IBinaryPattern;
import Catalano.Imaging.Texture.BinaryPattern.RobustLocalBinaryPattern;
import Catalano.Imaging.Texture.BinaryPattern.UniformLocalBinaryPattern;
import Catalano.Imaging.Tools.ImageHistogram;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

public class CatalanoBinaryPatternFilter extends AbstractCatalanoFilter<IBinaryPattern> {

	/**
	 *
	 */
	private static final long serialVersionUID = 9139886872471194592L;

	public CatalanoBinaryPatternFilter(final String name) {
		super(name);
	}

	@Override
	public DataSet applyFilter(final DataSet inputData, final boolean copy) {
		if (inputData.getIntermediateInstances() == null || inputData.getIntermediateInstances().size() == 0 || inputData.getIntermediateInstances().get(0).rank() < 2) {
			throw new IllegalArgumentException("Intermediate instances must have a rank of at least 2 for image processing.");
		}

		// None filter
		if (this.getCatalanoFilter() == null) {
			if (copy) {
				return inputData.copy();
			} else {
				return inputData;
			}
		}

		ColorSpace colorSpace = ImageUtils.determineColorSpace(inputData.getIntermediateInstances().get(0));

		// Assume to deal with FastBitmap instances
		List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
		for (INDArray inst : inputData.getIntermediateInstances()) {
			FastBitmap bitmap = ImageUtils.matrixToFastBitmap(inst, colorSpace);
			if (colorSpace != ColorSpace.Grayscale && this.isRequiresGrayscale()) {
				bitmap.toGrayscale();
			}

			ImageHistogram imageHistogram = this.getCatalanoFilter().ComputeFeatures(bitmap);

			INDArray result = ImageUtils.imageHistorgramToMatrix(imageHistogram);
			transformedInstances.add(result);
		}

		return new DataSet(inputData.getInstances(), transformedInstances);
	}

	public boolean isRequiresGrayscale() {
		switch (this.getName()) {
		/* Binary pattern */
		case "NoneExtractor":
			return false;
		case "UniformLocalBinaryPattern":
			return true;
		case "RobustLocalBinaryPattern":
			return true;
		// case "ExtractNormalizedRGBChannel":
		// return new CatalanoExtractFilter(new ExtractNormalizedRGBChannel(), false,
		// true);
		default:
			return false;
		}
	}

	public IBinaryPattern getCatalanoFilter() {
		switch (this.getName()) {
		/* Binary pattern */
		case "NoneExtractor":
			return null;
		case "UniformLocalBinaryPattern":
			return new UniformLocalBinaryPattern();
		case "RobustLocalBinaryPattern":
			return new RobustLocalBinaryPattern();
		// case "ExtractNormalizedRGBChannel":
		// return new CatalanoExtractFilter(new ExtractNormalizedRGBChannel(), false,
		// true);
		default:
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		return sb.toString();
	}
}
