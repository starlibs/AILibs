package jaicore.ml.dyadranking.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

/**
 * Creates simple rankings for testing purposes.
 * 
 * @author Jonas Hanselle, Mirko Jürgens, Helena Graf, Michael Braun
 *
 */
public class DyadRankingInstanceSupplier {

	/**
	 * Creates a random dyad ranking instance (with 2 alternatives and 2 instances)
	 * @param maxLength the amount of dyads
	 * @return
	 */
	public static DyadRankingInstance getDyadRankingInstance(int maxLength, int seed) {
		List<Dyad> dyads = new ArrayList<Dyad>();
		
		int actualLength = new Random(seed).nextInt(maxLength+1);
		for(int i = 0; i < actualLength; i++) {
			Dyad dyad = DyadSupplier.getRandomDyad(i, 2, 2);
			dyads.add(dyad);
		}
		Comparator<Dyad> comparator = complexDyadRanker();
		Collections.sort(dyads, comparator);
		return new DyadRankingInstance(dyads);
	}

	public static Comparator<Dyad> complexDyadRanker() {
		Comparator<Dyad> comparator = new Comparator<Dyad>() {
			@Override
			public int compare(Dyad d1, Dyad d2) {
				Vector scoreVecI = d1.getInstance();
				Vector scoreVecA = d1.getAlternative();
				Vector scoreVecI2 = d2.getInstance();
				Vector scoreVecA2 = d2.getAlternative();
				double score1 = Math.pow(scoreVecI.getValue(0), 2) + Math.pow(scoreVecI.getValue(1), 2) - Math.pow(scoreVecA.getValue(0), 2) - Math.pow(scoreVecA.getValue(1), 2);
				double score2 = Math.pow(scoreVecI2.getValue(0), 2) + Math.pow(scoreVecI2.getValue(1), 2) - Math.pow(scoreVecA2.getValue(0), 2) - Math.pow(scoreVecA2.getValue(1), 2);
				return score1 - score2 == 0 ? 0 : (score1 - score2 > 0 ? 1 : -1);
			}
		};
		return comparator;
	}
	
	public static DyadRankingDataset getDyadRankingDataset(int maxLengthDyadRankingInstance, int length) {
		DyadRankingDataset dataset = new DyadRankingDataset();
		for(int i = 0; i < length; i++) {
			dataset.add(DyadRankingInstanceSupplier.getDyadRankingInstance(maxLengthDyadRankingInstance, i));
		}
		return dataset;
	}

}
