package ai.libs.jaicore.ml.hpo.multifidelity.hyperband;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.algorithm.AOptimizer;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.model.EvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.ml.hpo.multifidelity.MultiFidelitySoftwareConfigurationProblem;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.Hyperband.HyperbandSolutionCandidate;
import ai.libs.jaicore.ml.hpo.multifidelity.hyperband.Hyperband.MultiFidelityScore;

/**
 * HyperBand is a simple but effective hyper-parameter optimization technique, heavily relying on a technique called successive halving.
 * Given a maximum amount of allocatable resources r_max and an integer parameter eta > 1, it allocates resources in a clever way, racing
 * randomly sampled solution candidates with increasing resources for more promising ones.
 *
 * For more details, refer to the published paper by Li et al. from 2018:
 * Hyperband: A Novel Bandit-Based Approach to Hyperparameter Optimization. In: Journal of Machine Learning research 18 (2018) 1-52
 *
 * @author mwever
 *
 */
public class Hyperband extends AOptimizer<MultiFidelitySoftwareConfigurationProblem<Double>, HyperbandSolutionCandidate, MultiFidelityScore> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Hyperband.class);

	public class MultiFidelityScore implements Comparable<MultiFidelityScore> {

		private final double r;
		private final double score;

		public MultiFidelityScore(final double r, final double score) {
			this.r = r;
			this.score = score;
		}

		@Override
		public int compareTo(final MultiFidelityScore o) {
			// compare budgets: the more the better (later round)
			int compareBudget = Double.compare(o.r, this.r);

			// if budget is not equal return the score evaluated on larger budget
			if (compareBudget != 0) {
				return compareBudget;
			} else {
				// compare scores: the smaller the better (loss minimization)
				return Double.compare(this.score, o.score);
			}
		}

		@Override
		public String toString() {
			return "(" + this.r + ";" + this.score + ")";
		}
	}

	public class HyperbandSolutionCandidate implements EvaluatedSoftwareConfigurationSolution<MultiFidelityScore> {
		private ComponentInstance ci;
		private MultiFidelityScore score;

		public HyperbandSolutionCandidate(final ComponentInstance ci, final double r, final double score) {
			this.ci = ci;
			this.score = new MultiFidelityScore(r, score);
		}

		@Override
		public MultiFidelityScore getScore() {
			return this.score;
		}

		@Override
		public ComponentInstance getComponentInstance() {
			return this.ci;
		}

		@Override
		public String toString() {
			return "c:" + this.score;
		}
	}

	private double eta;
	private double r_max;
	private double crashedEvaluationScore;

	// total budget B
	private double b;
	// number of brackets s_max
	private int s_max;

	private Random rand;

	private ExecutorService pool = null;

	public Hyperband(final IHyperbandConfig config, final MultiFidelitySoftwareConfigurationProblem<Double> problem) {
		super(config, problem);
		this.rand = new Random(config.getSeed());
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			this.eta = this.getConfig().getEta();
			this.r_max = this.getInput().getCompositionEvaluator().getMaxBudget();
			this.crashedEvaluationScore = this.getConfig().getCrashScore();

			if (this.getConfig().getIterations().equals("auto")) {
				this.s_max = (int) Math.floor(MathExt.logBase(this.r_max, this.eta));
			} else {
				this.s_max = Integer.parseInt(this.getConfig().getIterations());
			}
			this.b = (this.s_max + 1) * this.r_max;

			if (this.getConfig().cpus() > 1) {
				this.pool = Executors.newFixedThreadPool(this.getConfig().cpus());
			}
			LOGGER.info("Initialized HyperBand with eta={}, r_max={}, s_max={}, b={} and parallelizing with {} cpu cores.", this.eta, this.r_max, this.s_max, this.b, this.getConfig().cpus());
			return super.activate();
		case INACTIVE:
			throw new AlgorithmException("Algorithm has already finished.");
		default:
		case ACTIVE:
			for (int s = this.s_max; s >= 0; s--) {
				int n = (int) Math.ceil((this.b / this.r_max) * (Math.pow(this.eta, s) / (s + 1)));
				double r = (this.r_max) * Math.pow(this.eta, -s);
				LOGGER.info("Execute round {} of HyperBand with n={}, r={}", (this.s_max - s + 1), n, r);

				// sample random configurations
				List<ComponentInstance> t = this.getNCandidates(n);
				// begin successive halving with (n,r) inner loop
				for (int i = 0; i <= s; i++) {
					int n_i = (int) Math.floor(n / Math.pow(this.eta, i));
					double r_i = (r * Math.pow(this.eta, i));

					// evaluated candidates
					List<HyperbandSolutionCandidate> evaluatedCandidates = this.evaluate(t, r_i);

					// sort, update best seen solution
					evaluatedCandidates.sort((o1, o2) -> o1.getScore().compareTo(o2.getScore()));
					this.updateBestSeenSolution(evaluatedCandidates.get(0));

					// select top k
					t.clear();
					int k = (int) Math.floor(n_i / this.eta);
					IntStream.range(0, k).mapToObj(x -> evaluatedCandidates.get(x).getComponentInstance()).forEach(t::add);
				}
			}

			if (this.pool != null) {
				this.pool.shutdownNow();
			}
			return super.terminate();
		}
	}

	private List<HyperbandSolutionCandidate> evaluate(final List<ComponentInstance> t, final double budget) throws InterruptedException {
		Lock lock = new ReentrantLock();
		List<HyperbandSolutionCandidate> candidateList = new ArrayList<>(t.size());

		Semaphore sem = new Semaphore(0);
		List<Runnable> runnables = new ArrayList<>(t.size());

		for (ComponentInstance ci : t) {
			runnables.add(new Runnable() {
				@Override
				public void run() {
					double score;
					try {
						score = Hyperband.this.getInput().getCompositionEvaluator().evaluate(ci, budget);
					} catch (InterruptedException e) {
						return;
					} catch (ObjectEvaluationFailedException e) {
						score = Hyperband.this.crashedEvaluationScore;
					}

					lock.lock();
					try {
						candidateList.add(new HyperbandSolutionCandidate(ci, budget, score));
					} finally {
						lock.unlock();
						sem.release();
					}
				}
			});
		}

		if (this.pool != null) {
			runnables.stream().forEach(this.pool::submit);
			sem.acquire(t.size());
		} else {
			runnables.stream().forEach(x -> x.run());
		}

		return candidateList;
	}

	private List<ComponentInstance> getNCandidates(final int n) {
		List<ComponentInstance> ciList = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			ciList.add(ComponentInstanceUtil.sampleRandomComponentInstance(this.getInput().getRequiredInterface(), this.getInput().getComponents(), this.rand));
		}
		return ciList;
	}

	@Override
	public IHyperbandConfig getConfig() {
		return (IHyperbandConfig) super.getConfig();
	}

}
