package jaicore.ml.tsc.classifier;

import java.util.List;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.TimeSeriesInstance;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ShapeletTransformAlgorithm.Shapelet;
import weka.classifiers.Classifier;

public class ShapeletTransformClassifier
		extends TSClassifier<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset> {

	private List<Shapelet> shapelets;
	private int seed;
	private Classifier classifier;

	public ShapeletTransformClassifier(final int k, final int seed) {
		super(new ShapeletTransformAlgorithm(k, null, seed));
		this.seed = seed;
	}

	public List<Shapelet> getShapelets() {
		return shapelets;
	}

	public void setShapelets(List<Shapelet> shapelets) {
		this.shapelets = shapelets;
	}

	@Override
	public CategoricalAttributeValue predict(TimeSeriesInstance instance) throws PredictionException {
		// TODO Auto-generated method stub

		return null;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
}
