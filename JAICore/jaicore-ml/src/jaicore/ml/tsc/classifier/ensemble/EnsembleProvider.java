package jaicore.ml.tsc.classifier.ensemble;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.RotationForest;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.EuclideanDistance;

/**
 * Class statically providing preconfigured ensembles as commonly used in TSC
 * implementations.
 * 
 * @author Julian Lienen
 *
 */
public class EnsembleProvider {

	/**
	 * Initializes the CAWPE ensemble model consisting of five classifiers (SMO,
	 * KNN, J48, Logistic and MLP) using a majority voting strategy. The ensemble
	 * uses Weka classifiers. It refers to "Heterogeneous ensemble of standard
	 * classification algorithms" (HESCA) as described in Lines, Jason & Taylor,
	 * Sarah & Bagnall, Anthony. (2018). Time Series Classification with HIVE-COTE:
	 * The Hierarchical Vote Collective of Transformation-Based Ensembles. ACM
	 * Transactions on Knowledge Discovery from Data. 12. 1-35. 10.1145/3182382.
	 * 
	 * @param seed
	 *            Seed used within the classifiers and the majority confidence
	 *            voting scheme
	 * @param numFolds
	 *            Number of folds used within the determination of the classifier
	 *            weights for the {@link MajorityConfidenceVote}
	 * @return Returns an initialized (but untrained) ensemble model.
	 * @throws Exception
	 *             Thrown when the initialization has failed
	 */
	public static Classifier provideCAWPEEnsembleModel(final int seed, final int numFolds) throws Exception {
		Classifier[] classifiers = new Classifier[5];

		Vote voter = new MajorityConfidenceVote(numFolds, seed);

		SMO smo = new SMO();
		smo.turnChecksOff();
		smo.setBuildCalibrationModels(true);
		PolyKernel kl = new PolyKernel();
		kl.setExponent(1);
		smo.setKernel(kl);
		smo.setRandomSeed(seed);
		classifiers[0] = smo;

		IBk k = new IBk(100);
		k.setCrossValidate(true);
		EuclideanDistance ed = new EuclideanDistance();
		ed.setDontNormalize(true);
		k.getNearestNeighbourSearchAlgorithm().setDistanceFunction(ed);
		classifiers[1] = k;

		J48 c45 = new J48();
		c45.setSeed(seed);
		classifiers[2] = c45;

		classifiers[3] = new Logistic();

		classifiers[4] = new MultilayerPerceptron();

		voter.setClassifiers(classifiers);
		return voter;
	}

	/**
	 * Initializes the HIVE COTE ensemble consisting of 7 classifiers using a
	 * majority voting strategy as described in J. Lines, S. Taylor and A. Bagnall,
	 * "HIVE-COTE: The Hierarchical Vote Collective of Transformation-Based
	 * Ensembles for Time Series Classification," 2016 IEEE 16th International
	 * Conference on Data Mining (ICDM), Barcelona, 2016, pp. 1041-1046. doi:
	 * 10.1109/ICDM.2016.0133.
	 * 
	 * @param seed
	 *            Seed used within the classifiers and the majority confidence
	 *            voting scheme
	 * @param numFolds
	 *            Number of folds used within the determination of the classifier
	 *            weights for the {@link MajorityConfidenceVote}
	 * @return Returns the initialized (but untrained) HIVE COTE ensemble model.
	 */
	public static Classifier provideHIVECOTEEnsembleModel(final int seed, final int numFolds) {
		Classifier[] classifier = new Classifier[7];

		Vote voter = new MajorityConfidenceVote(5, seed);

		// SMO poly2
		SMO smop = new SMO();
		smop.turnChecksOff();
		smop.setBuildCalibrationModels(true);
		PolyKernel kernel = new PolyKernel();
		kernel.setExponent(2);
		smop.setKernel(kernel);
		smop.setRandomSeed(seed);
		classifier[0] = smop;

		// Random Forest
		RandomForest rf = new RandomForest();
		rf.setSeed(seed);
		rf.setNumIterations(500);
		classifier[1] = rf;

		// Rotation forest
		RotationForest rotF = new RotationForest();
		rotF.setSeed(seed);
		rotF.setNumIterations(100);
		classifier[2] = rotF;

		// NN
		IBk nn = new IBk();
		classifier[3] = nn;

		// Naive Bayes
		NaiveBayes nb = new NaiveBayes();
		classifier[4] = nb;

		// C45
		J48 c45 = new J48();
		c45.setSeed(seed);
		classifier[5] = c45;

		// SMO linear
		SMO smol = new SMO();
		smol.turnChecksOff();
		smol.setBuildCalibrationModels(true);
		PolyKernel linearKernel = new PolyKernel();
		linearKernel.setExponent(1);
		smol.setKernel(linearKernel);
		classifier[6] = smol;

		voter.setClassifiers(classifier);
		return voter;
	}
}
