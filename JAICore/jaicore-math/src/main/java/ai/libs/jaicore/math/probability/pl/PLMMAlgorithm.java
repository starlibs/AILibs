package ai.libs.jaicore.math.probability.pl;

import java.util.List;
import java.util.Random;

import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * This is the MM algorithm for Plackett-Luce as described in
 *
 * @article{hunter2004,
  title={MM algorithms for generalized Bradley-Terry models},
  author={Hunter, David R and others},
  journal={The annals of statistics},
  volume={32},
  number={1},
  pages={384--406},
  year={2004},
  publisher={Institute of Mathematical Statistics}
}

 *
 * @author felix
 *
 */
public class PLMMAlgorithm extends AAlgorithm<PLInferenceProblem, PLSkillMap> {

	private final int numRankings;
	private final int numObjects;
	private final IntList winVector;
	private DoubleList skillVector;
	private final Object[] objects;

	protected PLMMAlgorithm(final PLInferenceProblem input) {
		super(input);
		this.numRankings = this.getInput().getRankings().size();
		this.numObjects = this.getInput().getN();
		for (List<?> ranking : input.getRankings()) {
			if (ranking.size() != this.numObjects) {
				throw new UnsupportedOperationException("This MM implementation only supports full rankings!");
			}
		}
		this.winVector = this.getWinVector();
		this.skillVector = new DoubleArrayList();
		for (int i = 0; i < this.winVector.size(); i++) {
			this.skillVector.add(i + 1);
		}
		this.skillVector = this.normalizeSkillVector(this.skillVector);
		this.objects = this.getInput().getComparedObjects().toArray();
		System.out.println(this.winVector);
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		DoubleList lastSkillVector = null;
		DoubleList skillVector = this.skillVector;
		double epsilon = 0.00000001;
		double currentPerf = Double.MAX_VALUE * -1;
		double lastPerf = Double.MAX_VALUE * -1;
		do {
			System.out.println(skillVector);
			this.checkMinorization(skillVector, new Random(0));
			skillVector = this.normalizeSkillVector(this.getUpdatedSkillVector(skillVector));
			lastPerf = currentPerf;
			currentPerf = this.evaluateLogLikelihood(skillVector);
			if (lastSkillVector != null) {
				System.out.println(currentPerf);
				if (currentPerf < this.evaluateLogLikelihood(lastSkillVector)) {
					System.err.println("New value is WORSE than old value!");

					/* check whether the new skill vector is better in the surrogate */
					double performanceOfNewSkillVectorInSurrogate = this.evaluateMinorant(skillVector, lastSkillVector);
					double performanceOfOldSkillVectorInSurrogate = this.evaluateMinorant(lastSkillVector, lastSkillVector);
					if (performanceOfNewSkillVectorInSurrogate < performanceOfOldSkillVectorInSurrogate) {
						System.err.println("New skill vector is NOT better than the old one even on the surrogate.");
					}
				}
			}
			lastSkillVector = skillVector;
			System.out.println(lastPerf + " vs " + currentPerf);
		}
		while (currentPerf - lastPerf > epsilon);
		return null;
	}

	private DoubleList normalizeSkillVector(final DoubleList skillVector) {
		double sum = 0;
		for (double d : skillVector) {
			sum += d;
		}
		if (sum < 0) {
			sum *= -1;
		}
		DoubleList copy = new DoubleArrayList();
		for (double d : skillVector) {
			copy.add(d / sum);
		}
		return copy;
	}

	private double evaluateLogLikelihood(final DoubleList skillVector) {
		double sum = 0;
		for (int j = 0; j < this.numRankings; j++) {
			List<?> ranking = this.getInput().getRankings().get(j);
			for (int i = 0; i < this.numObjects; i++) {
				sum += Math.log(this.getSkillOfRankedObject(ranking, i, skillVector)) - Math.log(this.getSumOfSkillsOfItemsInRankingAtLeastAsBadAsThreshold(ranking, i, skillVector));
			}
		}
		return sum;
	}

	private double evaluateMinorant(final DoubleList skillVector, final DoubleList parameterSkillVector) {
		double sum = 0;
		for (int j = 0; j < this.numRankings; j++) {
			List<?> ranking = this.getInput().getRankings().get(j);
			for (int i = 0; i < this.numObjects; i++) {
				sum += Math.log(this.getSkillOfRankedObject(ranking, i, skillVector)) - this.getSumOfSkillsOfItemsInRankingAtLeastAsBadAsThreshold(ranking, i, skillVector) / this.getSumOfSkillsOfItemsInRankingAtLeastAsBadAsThreshold(ranking, i, parameterSkillVector);
			}
		}
		return sum;
	}

	private boolean checkMinorization(final DoubleList parameterSkillVector, final Random random) {
		int n = parameterSkillVector.size();
		for (int i = 0; i < 10000; i++) {
			DoubleList mutatedSkillVector = new DoubleArrayList();
			for (int j = 0; j < n; j++) {
				mutatedSkillVector.add(random.nextDouble() * random.nextInt(100));
			}
			double orig = this.evaluateLogLikelihood(mutatedSkillVector);
			double minor = this.evaluateMinorant(mutatedSkillVector, parameterSkillVector);
			if (minor > orig) {
				System.err.println(minor + " > " + orig);
				return false;
			}
		}
		return true;
	}

	private int getGlobalIndexOfRankedObject(final List<?> ranking, final int indexOfObjectInRanking) {
		Object objectAtThisPosition = ranking.get(indexOfObjectInRanking);
		return this.getInput().getComparedObjects().indexOf(objectAtThisPosition);
	}

	private double getSkillOfRankedObject(final List<?> ranking, final int indexOfObjectInRanking, final DoubleList skillVector) {
		double skill = skillVector.getDouble(this.getGlobalIndexOfRankedObject(ranking, indexOfObjectInRanking));
		//		System.out.println("Skill of " + indexOfObjectInRanking + "-th object in " + ranking + " based on vector " + skillVector + ": " + skill);
		return skill;
	}

	private DoubleList getUpdatedSkillVector(final DoubleList skillVector) {
		DoubleList updatedVector = new DoubleArrayList();
		for (int t = 0; t < this.numObjects; t++) {
			double denominator = 0;
			for (int j = 0; j < this.numRankings; j++) {
				List<?> ranking = this.getInput().getRankings().get(j);
				for (int i = 0; i < this.numObjects; i++) {
					if (this.isObjectRankedAtLeastAsBadAsIndex(ranking, this.objects[t], i)) {
						denominator += (1 / this.getSumOfSkillsOfItemsInRankingAtLeastAsBadAsThreshold(ranking, i, skillVector));
					}
				}
			}
			if (denominator == 0) {
				throw new IllegalStateException("");
			}
			updatedVector.add(this.numRankings / denominator);
		}
		return updatedVector;
	}

	private double getSumOfSkillsOfItemsInRankingAtLeastAsBadAsThreshold(final List<?> ranking, final int positionThreshold, final DoubleList skillVector) {
		double sum = 0;
		for (int s = positionThreshold; s < this.numObjects; s++) {
			sum += this.getSkillOfRankedObject(ranking, s, skillVector);
		}
		return sum;
	}

	private boolean isObjectRankedAtLeastAsBadAsIndex(final List<?> ranking, final Object object, final int positionThreshold) {
		if (!ranking.contains(object)) {
			throw new IllegalArgumentException("Object " + object + " not contained in ranking " + ranking);
		}
		boolean delta = ranking.indexOf(object) >= positionThreshold;
		return delta;
	}

	private IntList getWinVector() {
		IntList wins = new IntArrayList();
		for (int t = 0; t < this.numObjects; t ++) {
			Object item = this.getInput().getComparedObjects().get(t);
			int w = 0;
			for (List<?> ranking : this.getInput().getRankings()) {
				if (ranking.indexOf(item) < ranking.size() - 1) {
					w ++;
				}
			}
			wins.add(w);
		}
		return wins;
	}

	@Override
	public PLSkillMap call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		this.next();
		return null;
	}

}
