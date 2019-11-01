package ai.libs.reduction.single.confusion;

import java.util.ArrayList;
import java.util.Collection;

public class AConfusionBasedAlgorithm {
	protected Collection<Integer> incrementCluster(final Collection<Integer> cluster, final double[][] confusionMatrix, final Collection<Integer> blackList) {
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

	protected int getPenaltyOfCluster(final Collection<Integer> cluster, final double[][] confusionMatrix) {
		int sum = 0;
		for (int i : cluster) {
			for (int j : cluster) {
				if (i != j) {
					sum += confusionMatrix[i][j];
				}
			}
		}
		return sum;
	}


	protected int getLeastConflictingClass(final double[][] confusionMatrix, final Collection<Integer> blackList) {

		/* compute least conflicting class */
		int leastConflictingClass = -1;
		int leastKnownScore = Integer.MAX_VALUE;
		for (int i = 0; i < confusionMatrix.length; i++) {
			if (blackList.contains(i)) {
				continue;
			}
			int sum = 0;
			for (int j = 0; j < confusionMatrix.length; j++) {
				if (i != j) {
					sum += confusionMatrix[i][j];
				}
			}
			if (sum < leastKnownScore) {
				leastKnownScore = sum;
				leastConflictingClass = i;
			}
		}
		return leastConflictingClass;
	}
}
