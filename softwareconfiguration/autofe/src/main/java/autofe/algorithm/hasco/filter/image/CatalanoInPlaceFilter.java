package autofe.algorithm.hasco.filter.image;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.FastBitmap.ColorSpace;
import Catalano.Imaging.IApplyInPlace;
import Catalano.Imaging.Filters.DifferenceEdgeDetector;
import Catalano.Imaging.Filters.Emboss;
import Catalano.Imaging.Filters.Erosion;
import Catalano.Imaging.Filters.ExtractBoundary;
import Catalano.Imaging.Filters.FastVariance;
import Catalano.Imaging.Filters.GaborFilter;
import Catalano.Imaging.Filters.GaussianBlur;
import Catalano.Imaging.Filters.HighBoost;
import Catalano.Imaging.Filters.HomogenityEdgeDetector;
import Catalano.Imaging.Filters.HorizontalRunLengthSmoothing;
import Catalano.Imaging.Filters.ImageNormalization;
import Catalano.Imaging.Filters.ImageQuantization;
import Catalano.Imaging.Filters.IsotropicCompassEdgeDetector;
import Catalano.Imaging.Filters.KirschCompassEdgeDetector;
import Catalano.Imaging.Filters.MorphologicGradientImage;
import Catalano.Imaging.Filters.RobertsCrossEdgeDetector;
import Catalano.Imaging.Filters.SobelCompassEdgeDetector;
import Catalano.Imaging.Filters.SobelEdgeDetector;
import Catalano.Imaging.Filters.WeightedMedian;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

public class CatalanoInPlaceFilter extends AbstractCatalanoFilter<IApplyInPlace> {

	/**
	 *
	 */
	private static final long serialVersionUID = -3362311698548885139L;

	public CatalanoInPlaceFilter(final String name) {
		super(name);
	}

	@Override
	public DataSet applyFilter(final DataSet inputData, final boolean copy) throws InterruptedException {
		ImageUtils.checkInputData(inputData);

		// None filter
		if (this.getCatalanoFilter() == null) {
			prepareData(inputData, copy);
		}

		ColorSpace colorSpace = sampleColorSpace(inputData);
		List<INDArray> transformedInstances = new ArrayList<>(inputData.getIntermediateInstances().size());
		for (INDArray inst : inputData.getIntermediateInstances()) {
			checkInterrupt();

			FastBitmap bitmap = ImageUtils.matrixToFastBitmap(inst, colorSpace);

			if (this.isRequiresGrayscale() && colorSpace != ColorSpace.Grayscale) {
				bitmap.toGrayscale();
			}

			this.getCatalanoFilter().applyInPlace(bitmap);
			INDArray result = ImageUtils.fastBitmapToMatrix(bitmap, colorSpace);
			transformedInstances.add(result);
		}
		return new DataSet(inputData.getInstances(), transformedInstances);
	}

	@Override
	public String toString() {
		if (this.getCatalanoFilter() != null) {
			return "CatalanoInPlaceFilter [catalanoFilter=" + this.getName() + ", requiresGrayscale="
					+ ((this.getName() != null) ? this.isRequiresGrayscale() : "NaN") + "]";
		} else {
			return "CatalanoInPlaceFilter (empty)";
		}
	}

	public boolean isRequiresGrayscale() {
		switch (this.getName()) {
		case "GaussianBlur":
		case "Erosion":
		case "FastVariance":
		case "Emboss":
		case "HighBoost":
		case "KirschCompassEdgeDetector":
		case "IsotropicCompassEdgeDetector":
		case "SobelCompassEdgeDetector":
		case "WeightedMedian":
		case "NonePreprocessor":
			return false;
		case "SobelEdgeDetector":
		case "ExtractBoundary":
		case "DifferenceEdgeDetector":
		case "GaborFilter":
		case "HomogenityEdgeDetector":
		case "HorizontalRunLengthSmoothing":
		case "ImageQuantization":
		case "ImageNormalization":
		case "MorphologicGradientImage":
		case "RobertsCrossEdgeDetector":
			return true;
		default:
			return false;
		}
	}

	public IApplyInPlace getCatalanoFilter() {
		if (this.getName() == null) {
			return null;
		}

		switch (this.getName()) {
		case "GaussianBlur":
			return new GaussianBlur();
		case "SobelEdgeDetector":
			return new SobelEdgeDetector();
		case "ExtractBoundary":
			return new ExtractBoundary();
		case "DifferenceEdgeDetector":
			return new DifferenceEdgeDetector();
		case "Erosion":
			return new Erosion();
		case "FastVariance":
			return new FastVariance();
		case "Emboss":
			return new Emboss();
		case "GaborFilter":
			return new GaborFilter();
		case "HighBoost":
			return new HighBoost();
		case "HomogenityEdgeDetector":
			return new HomogenityEdgeDetector();
		case "HorizontalRunLengthSmoothing":
			return new HorizontalRunLengthSmoothing();
		case "ImageQuantization":
			return new ImageQuantization();
		case "ImageNormalization":
			return new ImageNormalization();
		case "MorphologicGradientImage":
			return new MorphologicGradientImage();
		case "KirschCompassEdgeDetector":
			return new KirschCompassEdgeDetector();
		case "IsotropicCompassEdgeDetector":
			return new IsotropicCompassEdgeDetector();
		case "RobertsCrossEdgeDetector":
			return new RobertsCrossEdgeDetector();
		case "SobelCompassEdgeDetector":
			return new SobelCompassEdgeDetector();
		case "WeightedMedian":
			return new WeightedMedian();
		case "NonePreprocessor":
			return null;
		default:
			// Return identity
			return null;
		}
	}

	@Override
	public CatalanoInPlaceFilter clone() throws CloneNotSupportedException {
		return new CatalanoInPlaceFilter(this.getName());
	}
}
