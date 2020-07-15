package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

import ai.libs.jaicore.basic.sets.Pair;

public abstract class AAreaUnderCurvePerformanceMeasure extends ASingleLabelPredictionPerformanceMeasure {

	private final int positiveClass;

	public AAreaUnderCurvePerformanceMeasure(final int positiveClass) {
		super();
		this.positiveClass = positiveClass;
	}

	public AAreaUnderCurvePerformanceMeasure() {
		this(0);
	}

	public Object getPositiveClass() {
		return this.positiveClass;
	}

	public List<Pair<Double, Integer>> getPredictionList(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		List<Pair<Double, Integer>> predictionsList = new ArrayList<>(expected.size());
		IntStream.range(0, expected.size()).mapToObj(x -> new Pair<>(predicted.get(x).getProbabilityOfLabel(this.positiveClass), (int) expected.get(x))).forEach(predictionsList::add);
		Collections.sort(predictionsList, (o1, o2) -> o2.getX().compareTo(o1.getX()));
		return predictionsList;
	}

	/**
	 * Computes the area under the curve coordinates, assuming a linear interpolation between the coordinates.
	 *
	 * @param curveCoordinates The points of the curve in ascending order (according to x-axis).
	 * @return The area under the curve.
	 */
	protected double getAreaUnderCurve(final List<Pair<Double, Double>> curveCoordinates) {
		double area = 0.0;
		for (int i = 1; i < curveCoordinates.size(); i++) {
			Pair<Double, Double> prev = curveCoordinates.get(i - 1);
			Pair<Double, Double> cur = curveCoordinates.get(i);

			double deltaX = cur.getX() - prev.getX();
			double deltaY = cur.getY() - prev.getY();
			area += prev.getY() * deltaX + deltaX * deltaY / 2;
		}
		return area;
	}

	@Override
	public double score(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		this.checkConsistency(expected, predicted);

		// parse predictions into a handy format
		List<Pair<Double, Integer>> predictionsList = this.getPredictionList(expected, predicted);

		// Compute roc curve coordinates
		List<Pair<Double, Double>> curveCoordinates = new ArrayList<>(predictionsList.size());
		int tp = 0;
		int fp = 0;
		int fn = (int) predictionsList.stream().filter(x -> x.getY() == this.getPositiveClass()).count();
		int tn = predictionsList.size() - fn;
		curveCoordinates.add(new Pair<>(this.getXValue(tp, fp, tn, fn), this.getYValue(tp, fp, tn, fn)));

		double currentThreshold = 1.0;
		int currentIndex = 0;
		while (currentIndex < predictionsList.size()) {
			while (currentIndex < predictionsList.size() && currentThreshold <= predictionsList.get(currentIndex).getX()) {
				Pair<Double, Integer> pred = predictionsList.get(currentIndex);
				if (pred.getY() == this.getPositiveClass()) {
					tp++;
					fn--;
				} else {
					fp++;
					tn--;
				}
				currentIndex++;
			}
			curveCoordinates.add(new Pair<>(this.getXValue(tp, fp, tn, fn), this.getYValue(tp, fp, tn, fn)));
			if (currentIndex >= predictionsList.size()) {
				break;
			}
			currentThreshold = predictionsList.get(currentIndex).getX();
		}

		// compute area under the curve
		return this.getAreaUnderCurve(curveCoordinates);
	}

	public abstract double getXValue(int tp, int fp, int tn, int fn);

	public abstract double getYValue(int tp, int fp, int tn, int fn);

}
