package jaicore.ml.tsc.classifier.ensemble;

import java.util.Arrays;
import java.util.Random;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.Vote;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Vote implementation for majority confidence. The ensemble's distributions of
 * each classifier are aggregated using the sum of each unique values times
 * classifier weights. The classifier weights are determined during training
 * using a CV.
 * 
 * @author Julian Lienen
 *
 */
public class MajorityConfidenceVote extends Vote {
	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = -7128109840679632228L;

	/**
	 * Number of folds used for determining the classifier weights by test accuracy
	 * within a CV on the training data.
	 */
	private int numFolds;

	/**
	 * The classifier weights which are used within distribution for instance
	 * calculation.
	 */
	private double classifierWeights[];

	/**
	 * Seed used within CV for splitting the data.
	 */
	private int seed;

	/**
	 * Constructor for a majority confidence vote ensemble classifier.
	 * 
	 * @param numFolds
	 *            See {@link MajorityConfidenceVote#numFolds}
	 * @param seed
	 *            See {@link MajorityConfidenceVote#seed}
	 */
	public MajorityConfidenceVote(final int numFolds, final int seed) {
		super();
		this.numFolds = numFolds;
	}

	/**
	 * Builds the ensemble by assessing the classifier weights using a cross
	 * validation of each classifier of the ensemble and then training the
	 * classifiers using the complete <code>data</code>.
	 * 
	 * @param data
	 *            Training instances
	 */
	@Override
	public void buildClassifier(final Instances data) throws Exception {

		this.classifierWeights = new double[this.m_Classifiers.length];

		// remove instances with missing class
		Instances newData = new Instances(data);
		newData.deleteWithMissingClass();
		this.m_structure = new Instances(newData, 0);

		// can classifier handle the data?
		this.getCapabilities().testWithFail(data);

		for (int i = 0; i < this.m_Classifiers.length; i++) {
			// XXX kill weka execution
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Thread got interrupted, thus, kill WEKA.");
			}

			// Perform cross validation to determine the classifier weights
			for (int n = 0; n < this.numFolds; n++) {
				Instances train = data.trainCV(this.numFolds, n, new Random(this.seed));
				Instances test = data.testCV(this.numFolds, n);

				this.getClassifier(i).buildClassifier(train);
				Evaluation eval = new Evaluation(train);
				eval.evaluateModel(this.getClassifier(i), test);
				this.classifierWeights[i] += eval.pctCorrect() / 100d;
			}

			this.classifierWeights[i] = Math.pow(this.classifierWeights[i], 2);
			this.classifierWeights[i] /= (double) this.numFolds;

			this.getClassifier(i).buildClassifier(newData);
		}

		// If no classifier predicted something correctly, assume uniform distribution
		if (Arrays.stream(this.classifierWeights).allMatch(d -> d < 0.000001d)) {
			for (int i = 0; i < this.classifierWeights.length; i++) {
				this.classifierWeights[i] = 1d / (double) this.classifierWeights.length;
			}
		}
	}

	/**
	 * Function calculating the distribution for a instance by predicting the
	 * distributions for each classifier and multiplying the result by the
	 * classifier weights. The final result is the sum of each probabilities for
	 * each class.
	 * 
	 * @param instace
	 *            Instance to be predicted
	 * @return Returns the final probability distribution for each class for the
	 *         given <code>instance</code>
	 * 
	 */
	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		double[] probs = new double[instance.numClasses()];
		for (int i = 0; i < probs.length; i++) {
			probs[i] = 1.0;
		}

		int numPredictions = 0;
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			// XXX kill weka execution
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Thread got interrupted, thus, kill WEKA.");
			}
			double[] dist = this.getClassifier(i).distributionForInstance(instance);
			if (Utils.sum(dist) > 0) {
				for (int j = 0; j < dist.length; j++) {
					probs[j] += this.classifierWeights[i] * dist[j];
				}
				numPredictions++;
			}
		}

		for (int i = 0; i < this.m_preBuiltClassifiers.size(); i++) {
			// XXX kill weka execution
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Thread got interrupted, thus, kill WEKA.");
			}
			double[] dist = this.m_preBuiltClassifiers.get(i).distributionForInstance(instance);
			if (Utils.sum(dist) > 0) {
				for (int j = 0; j < dist.length; j++) {
					probs[j] *= dist[j];
				}
				numPredictions++;
			}
		}

		// No predictions?
		if (numPredictions == 0) {
			return new double[instance.numClasses()];
		}

		// Should normalize to get "probabilities"
		if (Utils.sum(probs) > 0) {
			Utils.normalize(probs);
		}


		return probs;
	}
	
	/**
	 * {@inheritDoc}
	 */
	 @Override
	  public double classifyInstance(final Instance instance) throws Exception {
		double result;
		int index;
		double[] dist = this.distributionForInstance(instance);
		if (instance.classAttribute().isNominal()) {
			index = Utils.maxIndex(dist);
			if (dist[index] == 0) {
				result = Utils.missingValue();
			} else {
				result = index;
			}
		} else if (instance.classAttribute().isNumeric()) {
			result = dist[0];
		} else {
			result = Utils.missingValue();
		}
		return result;
	 }
}
