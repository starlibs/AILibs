package ai.libs.jaicore.math.probability.pl;

import java.util.List;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.shorts.ShortList;

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
public class PLMMAlgorithm extends AAlgorithm<PLInferenceProblem, DoubleList> {

	private final List<ShortList> rankings;
	private final int numRankings;
	private final int numObjects;
	private final IntList winVector;
	private DoubleList skillVector;

	public PLMMAlgorithm(final PLInferenceProblem input) {
		this(input, null, null);
	}

	public PLMMAlgorithm(final PLInferenceProblem input, final IOwnerBasedAlgorithmConfig config) {
		this(input, null, config);
	}

	public PLMMAlgorithm(final PLInferenceProblem input, final DoubleList skillVector, final IOwnerBasedAlgorithmConfig config) {
		super(config, input);
		this.numRankings = this.getInput().getRankings().size();
		this.numObjects = this.getInput().getNumObjects();
		if (this.numObjects < 2) {
			throw new IllegalArgumentException("Cannot create PL-Algorithm for choice problems with only one option.");
		}
		this.rankings = input.getRankings();
		for (ShortList ranking : this.rankings) {
			if (ranking.size() != this.numObjects) {
				throw new UnsupportedOperationException("This MM implementation only supports full rankings!");
			}
		}
		this.skillVector = skillVector != null ? skillVector : getDefaultSkillVector(this.numObjects);
		this.winVector = this.getWinVector();
	}

	public static DoubleList getDefaultSkillVector(final int n) {
		DoubleList skillVector = new DoubleArrayList();
		double p = 1.0 / n;
		for (int i = 0; i < n; i++) {
			skillVector.add(p);
		}
		return skillVector;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		DoubleList lastSkillVector = null;
		double epsilon = 0.00001;
		double diffToLast = 0;
		do {
			this.skillVector = this.normalizeSkillVector(this.getUpdatedSkillVectorImproved(this.skillVector));
			if (lastSkillVector != null) {
				diffToLast = 0;
				for (int i = 0; i < this.numObjects; i++) {
					diffToLast += Math.abs(this.skillVector.getDouble(i) - lastSkillVector.getDouble(i));
				}
			}
			else {
				diffToLast = Double.MAX_VALUE;
			}
			lastSkillVector = this.skillVector;
		}
		while (diffToLast > epsilon);
		return null;
	}

	private DoubleList normalizeSkillVector(final DoubleList skillVector) {
		double sum = 0;
		for (double d : skillVector) {
			if (Double.isNaN(d)) {
				throw new IllegalArgumentException("Skill vector has NaN entry: " + skillVector);
			}
			sum += d;
		}
		if (sum < 0) {
			sum *= -1;
		}
		if (sum == 0) {
			throw new IllegalArgumentException("Cannot normalize null skill vector: " + skillVector);
		}
		DoubleList copy = new DoubleArrayList();
		for (double d : skillVector) {
			copy.add(d / sum);
		}
		return copy;
	}

	private double getSkillOfRankedObject(final ShortList ranking, final int indexOfObjectInRanking, final DoubleList skillVector) {
		return skillVector.getDouble(ranking.getShort(indexOfObjectInRanking));
	}

	private DoubleList getUpdatedSkillVectorImproved(final DoubleList skillVector) {
		DoubleList updatedVector = new DoubleArrayList();

		/* first create tree of accumulated skills (stored in 2D-array (not a matrix)) */
		double[][] accumulatedSkills = new double[this.numRankings][];
		for (int j = 0; j < this.numRankings; j++) {
			ShortList ranking = this.rankings.get(j);
			double[] accumulatedSkillsForThisRanking = new double[ranking.size() - 1];
			int numNodes = accumulatedSkillsForThisRanking.length;
			accumulatedSkills[j] = accumulatedSkillsForThisRanking;

			/* compute the delta-conditioned sums of the denominator in (30) */
			accumulatedSkillsForThisRanking[numNodes - 1] = this.getSkillOfRankedObject(ranking, this.numObjects - 1, skillVector) + this.getSkillOfRankedObject(ranking, this.numObjects - 2, skillVector);
			for (int i = this.numObjects - 3; i >= 0; i--) {
				accumulatedSkillsForThisRanking[i] = accumulatedSkillsForThisRanking[i + 1] + this.getSkillOfRankedObject(ranking, i, skillVector);
			}
			for (int i = 0; i < numNodes; i++) {
				accumulatedSkillsForThisRanking[i] = 1 / accumulatedSkillsForThisRanking[i];
			}
		}

		/* now compute the updated skill values for each t base on the accumulated skills structure */
		for (short t = 0; t < this.numObjects; t++) {
			double denominator = 0;

			for (int j = 0; j < this.numRankings; j++) {
				ShortList ranking = this.rankings.get(j);
				double[] accumulatedSkillsForThisRanking = accumulatedSkills[j];
				for (int i = 0; i < accumulatedSkillsForThisRanking.length; i++) {
					denominator += accumulatedSkillsForThisRanking[i];
					if (ranking.getShort(i) == t) {
						break;
					}
				}
			}
			if (denominator == 0) {
				throw new IllegalStateException("Denominator in PL-model must not be null.");
			}
			updatedVector.add(this.winVector.getInt(t) / denominator);
		}
		return updatedVector;
	}

	private IntList getWinVector() {
		IntList wins = new IntArrayList();
		for (short t = 0; t < this.numObjects; t ++) {
			int w = 0;
			for (ShortList ranking : this.getInput().getRankings()) {
				if (ranking.indexOf(t) < ranking.size() - 1) {
					w ++;
				}
			}
			wins.add(w);
		}
		return wins;
	}

	@Override
	public DoubleList call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		this.next();
		if (this.skillVector.size() != this.numObjects) {
			throw new IllegalStateException("Have " + this.skillVector.size() + " skills (" + this.skillVector + ") for " + this.numObjects + " objects.");
		}
		for (double d : this.skillVector) {
			if (Double.isNaN(d)) {
				throw new IllegalStateException("Illegal skill return value: " + this.skillVector);
			}
		}
		return this.skillVector;
	}

	public DoubleList getSkillVector() {
		return this.skillVector;
	}

}
