package ai.libs.reduction.single.confusion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class ConfusionBasedGreedyOptimizingAlgorithm {

	private static Logger logger = LoggerFactory.getLogger(ConfusionBasedGreedyOptimizingAlgorithm.class);

	public MCTreeNodeReD buildClassifier(final Instances data, final Collection<String> pClassifierNames) throws Exception {

		if (logger.isInfoEnabled()) {
			logger.info("START: {}", data.relationName());
		}
		int seed = 0;
		List<Instances> split = WekaUtil.getStratifiedSplit(data, seed, .7f);
		int numClasses = data.numClasses();

		/* compute confusion matrices for each classifier */
		logger.info("Computing confusion matrices ...");
		Map<String, double[][]> confusionMatrices = new HashMap<>();
		for (String classifier : pClassifierNames) {
			logger.info("\t{} ...", classifier);
			try {
				Classifier c = AbstractClassifier.forName(classifier, null);
				c.buildClassifier(split.get(0));
				Evaluation eval = new Evaluation(split.get(0));
				eval.evaluateModel(c, split.get(1));
				confusionMatrices.put(classifier, eval.confusionMatrix());
			} catch (Exception e) {
				logger.error("Could not train classifier: {}", e);
			}
		}
		logger.info("done");

		/* compute zero-conflict sets for each classifier */
		Map<String, Collection<Collection<Integer>>> zeroConflictSets = new HashMap<>();
		for (Entry<String, double[][]> entry : confusionMatrices.entrySet()) {
			zeroConflictSets.put(entry.getKey(), this.getZeroConflictSets(entry.getValue()));
		}

		/* greedily identify triplets */
		Collection<List<String>> classifierPairs = SetUtil.cartesianProduct(confusionMatrices.keySet(), 2);
		int leastSeenMistakes = Integer.MAX_VALUE;
		String bestLeft = null;
		String bestRight = null;
		String bestInner = null;
		Collection<Integer> bestLeftClasses = null;
		Collection<Integer> bestRightClasses = null;
		int numPair = 0;
		for (List<String> classifierPair : classifierPairs) {
			numPair++;
			String c1 = classifierPair.get(0);
			String c2 = classifierPair.get(1);
			logger.info("\tConsidering {}/{} ({}/{})", c1, c2, numPair, classifierPairs.size());
			double[][] cm1 = confusionMatrices.get(c1);
			double[][] cm2 = confusionMatrices.get(c2);
			Collection<Collection<Integer>> z1 = zeroConflictSets.get(c1);
			Collection<Collection<Integer>> z2 = zeroConflictSets.get(c2);

			/* create candidate split */
			int sizeOfBestCombo = 0;
			Collection<Integer> bestZ1 = null;
			Collection<Integer> bestZ2 = null;
			for (Collection<Integer> zeroSet1 : z1) {
				for (Collection<Integer> zeroSet2 : z2) {
					Collection<Integer> coveredClassesOfThisPair = SetUtil.union(zeroSet1, zeroSet2);
					if (coveredClassesOfThisPair.size() > sizeOfBestCombo) {
						sizeOfBestCombo = coveredClassesOfThisPair.size();
						bestZ1 = zeroSet1;
						bestZ2 = zeroSet2;
					}
				}
			}

			/* greedily complete these candidates */
			for (int cId = 0; cId < numClasses; cId++) {
				if (!bestZ1.contains(cId) && !bestZ2.contains(cId)) {

					/* compute effect of adding this class to the respective clusters */
					Collection<Integer> newBestZ1 = new ArrayList<>(bestZ1);
					newBestZ1.add(cId);
					int p1 = this.getPenaltyOfCluster(newBestZ1, cm1);
					Collection<Integer> newBestZ2 = new ArrayList<>(bestZ2);
					newBestZ2.add(cId);
					int p2 = this.getPenaltyOfCluster(newBestZ2, cm2);

					if (p1 < p2) {
						bestZ1 = newBestZ1;
					} else {
						bestZ2 = newBestZ2;
					}
				}
			}
			int p1 = this.getPenaltyOfCluster(bestZ1, cm1);
			int p2 = this.getPenaltyOfCluster(bestZ2, cm2);

			/* create the split problem */
			Map<String, String> classMap = new HashMap<>();
			for (int i1 : bestZ1) {
				classMap.put(data.classAttribute().value(i1), "l");
			}
			for (int i2 : bestZ2) {
				classMap.put(data.classAttribute().value(i2), "r");
			}
			Instances newData = WekaUtil.getRefactoredInstances(data, classMap);
			List<Instances> binaryInnerSplit = WekaUtil.getStratifiedSplit(newData, seed, .7f);

			/* now identify the classifier that can best separate these two clusters */
			for (String classifier : pClassifierNames) {
				try {
					logger.info("\t\tConsidering {}/{}/{}", c1, c2, classifier);
					Classifier c = AbstractClassifier.forName(classifier, null);

					c.buildClassifier(binaryInnerSplit.get(0));
					Evaluation eval = new Evaluation(newData);
					eval.evaluateModel(c, binaryInnerSplit.get(1));
					int mistakes = (int) eval.incorrect();
					int overallMistakes = p1 + p2 + mistakes;
					if (overallMistakes < leastSeenMistakes) {
						leastSeenMistakes = overallMistakes;
						logger.info("New best system: {}/{}/{} with {}", c1, c2, classifier, leastSeenMistakes);
						bestLeftClasses = bestZ1;
						bestRightClasses = bestZ2;
						bestLeft = c1;
						bestRight = c2;
						bestInner = classifier;
					}
				} catch (Exception e) {
					logger.error("Encountered error: {}", e);
				}
			}
		}

		if (bestLeftClasses == null) {
			throw new IllegalStateException("Best left classes must not be null");
		}

		/* now create MCTreeNode with choices */
		MCTreeNodeReD tree = new MCTreeNodeReD(bestInner, bestLeftClasses.stream().map(i -> data.classAttribute().value(i)).collect(Collectors.toList()), bestLeft,
				bestRightClasses.stream().map(i -> data.classAttribute().value(i)).collect(Collectors.toList()), bestRight);
		tree.buildClassifier(data);
		return tree;
	}

	private int getLeastConflictingClass(final double[][] confusionMatrix, final Collection<Integer> blackList) {

		/* compute least conflicting class */
		int leastConflictingClass = -1;
		int leastKnownScore = Integer.MAX_VALUE;
		for (int i = 0; i < confusionMatrix.length; i++) {
			if (blackList.contains(i)) {
				continue;
			}
			int sum = 0;
			for (int j = 0; j < confusionMatrix.length; j++) {
				sum += confusionMatrix[i][j];
			}
			if (sum < leastKnownScore) {
				leastKnownScore = sum;
				leastConflictingClass = i;
			}
		}
		return leastConflictingClass;
	}

	private Collection<Collection<Integer>> getZeroConflictSets(final double[][] confusionMatrix) {
		Collection<Integer> blackList = new ArrayList<>();
		Collection<Collection<Integer>> partitions = new ArrayList<>();

		int leastConflictingClass = -1;
		do {
			leastConflictingClass = this.getLeastConflictingClass(confusionMatrix, blackList);
			if (leastConflictingClass >= 0) {
				Collection<Integer> cluster = new ArrayList<>();
				cluster.add(leastConflictingClass);
				do {
					cluster = this.incrementCluster(cluster, confusionMatrix, blackList);
					if (cluster.contains(-1)) {
						throw new IllegalStateException("Computed illegal cluster: " + cluster);
					}
				} while (this.getPenaltyOfCluster(cluster, confusionMatrix) == 0);
				blackList.addAll(cluster);
				partitions.add(cluster);
			}
		} while (leastConflictingClass >= 0);

		return partitions;
	}

	private Collection<Integer> incrementCluster(final Collection<Integer> cluster, final double[][] confusionMatrix, final Collection<Integer> blackList) {
		int leastSeenPenalty = Integer.MAX_VALUE;
		int choice = -1;
		for (int cId = 0; cId < confusionMatrix.length; cId++) {
			if (cluster.contains(cId) || blackList.contains(cId)) {
				continue;
			}
			int addedPenalty = 0;
			for (int i = 0; i < confusionMatrix.length; i++) {
				addedPenalty += confusionMatrix[i][cId];
				addedPenalty += confusionMatrix[cId][i];
			}
			if (addedPenalty < leastSeenPenalty) {
				leastSeenPenalty = addedPenalty;
				choice = cId;
			}
		}
		Collection<Integer> newCluster = new ArrayList<>(cluster);
		if (choice < 0) {
			return newCluster;
		}
		newCluster.add(choice);
		return newCluster;
	}

	private int getPenaltyOfCluster(final Collection<Integer> cluster, final double[][] confusionMatrix) {
		int sum = 0;
		for (int i : cluster) {
			for (int j : cluster) {
				sum += confusionMatrix[i][j];
			}
		}
		return sum;
	}
}
