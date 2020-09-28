package ai.libs.jaicore.ml.weka.classification.learner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;

public class WekaClassifier extends AWekaLearner<ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> implements IWekaClassifier {

	public static WekaClassifier createPipeline(final String searcher, final List<String> searcherOptions, final String evaluator, final List<String> evaluatorOptions, final String classifier, final List<String> classifierOptions)
			throws Exception {
		ASSearch search = searcher != null ? ASSearch.forName(searcher, searcherOptions.toArray(new String[0])) : null;
		ASEvaluation eval = evaluator != null ? ASEvaluation.forName(evaluator, evaluatorOptions.toArray(new String[0])) : null;
		Classifier c = AbstractClassifier.forName(classifier, classifierOptions.toArray(new String[0]));
		return new WekaClassifier(new MLPipeline(search, eval, c));
	}

	public static WekaClassifier createBaseClassifier(final String name, final List<String> options) {
		return new WekaClassifier(name, options.toArray(new String[0]));
	}

	public WekaClassifier(final String name, final String[] options) {
		super(name, options);
	}

	public WekaClassifier(final Classifier classifier) {
		super(classifier);
		this.wrappedLearner = classifier;
		this.name = classifier.getClass().getName();
	}

	@Override
	public ISingleLabelClassification predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		try {
			Map<Integer, Double> distribution = new HashMap<>();
			double[] dist = this.wrappedLearner.distributionForInstance(this.getWekaInstance(xTest).getElement());
			IntStream.range(0, dist.length).forEach(x -> distribution.put(x, dist[x]));
			return new SingleLabelClassification(distribution);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

	@Override
	protected ISingleLabelClassificationPredictionBatch getPredictionListAsBatch(final List<ISingleLabelClassification> predictionList) {
		return new SingleLabelClassificationPredictionBatch(predictionList);
	}

}
