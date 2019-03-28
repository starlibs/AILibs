package de.upb.crc901.mlpipeline_evaluation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.IMeasure;
import jaicore.ml.evaluation.IInstancesClassifier;
import jaicore.ml.evaluation.evaluators.weka.AbstractEvaluatorMeasureBridge;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * A measure bridge for uploading some simple intermediate results. Uploads
 * averages of an MCCV, and a simplified component representation.
 * 
 * @author Helena Graf
 *
 */
public class SimpleUploaderMeasureBridge extends AbstractEvaluatorMeasureBridge<Double, Double> {

	private Logger logger = LoggerFactory.getLogger(SimpleUploaderMeasureBridge.class);

	/**
	 * Uploader used to upload the results
	 */
	private SimpleResultsUploader uploader;

	/**
	 * Construct a bridge using the given uploader. Basic evaluator must then be set
	 * later before it can be used.
	 * 
	 * @param simpleResultsUploader
	 */
	public SimpleUploaderMeasureBridge(SimpleResultsUploader simpleResultsUploader) {
		this.uploader = simpleResultsUploader;
	}

	/**
	 * Construct a brdige using the given uploader and basic evaluator.
	 * 
	 * @param basicEvaluator
	 * @param uploader
	 */
	public SimpleUploaderMeasureBridge(IMeasure<Double, Double> basicEvaluator, SimpleResultsUploader uploader) {
		super(basicEvaluator);
		this.uploader = uploader;
	}

	/**
	 * Uploads the found classifier with the given result, phase, and time it took
	 * to evaluate the classifier.
	 * 
	 * @param classifier
	 *            the found pipeline
	 * @param result
	 *            the error of the pipeline
	 * @param phase
	 *            the phase in which it was found (search or selection)
	 * @param time
	 *            how long it took too evaluate the classifier
	 */
	public void receiveFinalResult(Classifier classifier, double result, String phase, long time) {
		try {
			uploader.uploadResult((MLPipeline) classifier, time, result, phase);
		} catch (SQLException e) {
			logger.warn("Could not upload ci!");
		}
	}

	@Override
	public Double evaluateSplit(Classifier classifier, Instances trainingData, Instances validationData)
			throws Exception {
		List<Double> actual = WekaUtil.getClassesAsList(validationData);
		List<Double> predicted = new ArrayList<>();
		classifier.buildClassifier(trainingData);
		if (classifier instanceof IInstancesClassifier) {
			for (double prediction : ((IInstancesClassifier) classifier).classifyInstances(validationData)) {
				predicted.add(prediction);
			}
		} else {
			for (Instance inst : validationData) {
				predicted.add(classifier.classifyInstance(inst));
			}
		}

		return this.basicEvaluator.calculateAvgMeasure(actual, predicted);
	}

}
