package jaicore.ml.tsc.classifier;

import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.TimeSeriesInstance;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeType;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ShapeletTransformAlgorithm.Shapelet;

public class ShapeletTransformClassifier
		extends TSClassifier<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset> {

	private List<Shapelet> shapelets;

	public ShapeletTransformClassifier() {
		super(new ShapeletTransformAlgorithm(0, null));
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

	private TimeSeriesDataset shapeletTransform(final TimeSeriesDataset dataSet) {

		// TODO: Deal with multivariate (assuming univariate for now)
		TimeSeriesAttributeType tsAttType = (TimeSeriesAttributeType) dataSet.getAttributeTypes().get(0);
		INDArray timeSeries = dataSet.getMatrixForAttributeType(tsAttType);

		INDArray transformedTS = Nd4j.create(timeSeries.shape()[0], this.shapelets.size());

		for (int i = 0; i < timeSeries.shape()[0]; i++) {
			for (int j = 0; j < shapelets.size(); j++) {
				transformedTS.putScalar(new int[] { i, j }, ShapeletTransformAlgorithm
						.getMinimumDistanceAmongAllSubsequences(shapelets.get(j), timeSeries.getRow(i)));
			}
		}

		dataSet.updateTimeSeriesMatrix(tsAttType, transformedTS);
		return dataSet;

	}

}
