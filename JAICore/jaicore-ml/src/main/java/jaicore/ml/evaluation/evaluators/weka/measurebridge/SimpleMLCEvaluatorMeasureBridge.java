package jaicore.ml.evaluation.evaluators.weka.measurebridge;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.IMeasure;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Result;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class SimpleMLCEvaluatorMeasureBridge extends AbstractEvaluatorMeasureBridge<double[], Double> {

	private Double minErrorSeen = null;

	public SimpleMLCEvaluatorMeasureBridge(final IMeasure<double[], Double> basicEvaluator) {
		super(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(final Classifier pl, final Instances trainingData, final Instances validationData) throws ObjectEvaluationFailedException, InterruptedException {
		Double error = null;
		try {
			Result res = Evaluation.evaluateModel((MultiLabelClassifier) pl, trainingData, validationData, "PCutL");

			List<double[]> ypred = Arrays.stream(res.allPredictions()).collect(Collectors.toList());
			List<double[]> y = Arrays.stream(res.allTrueValues()).map(x -> Arrays.stream(x).mapToDouble(j -> j).toArray()).collect(Collectors.toList());

			error = this.getBasicEvaluator().calculateAvgMeasure(ypred, y);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new ObjectEvaluationFailedException("Could not evaluate classifier " + pl.getClass().getName(), e1);
		}

		if ((error + "").equals("NaN")) {
			throw new ObjectEvaluationFailedException("Classifier " + pl.getClass().getName() + " could not be evalauted. Please refer to the previous logs for more detailed information.");
		}

		if (this.minErrorSeen == null || error < this.minErrorSeen) {
			this.minErrorSeen = error;
		}
		System.out.println(Thread.currentThread().getName() + ": " + WekaUtil.printNestedWekaClassifier(pl) + " | Error: " + error + " Min Error so far: " + this.minErrorSeen);

		return error;
	}

}
