package ai.libs.jaicore.problems.scheduling.openshop;

import java.util.Random;
import java.util.function.IntBinaryOperator;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

public class OpenShopProblemGenerator {

	private int seed = 0;
	private int numWorkcenters = 0;
	private AbstractRealDistribution distributionOfMachinesPerWorkcenter = new NormalDistribution(3, 1);
	private int numJobs = 0;
	private AbstractRealDistribution distributionOfOperationsPerJob = new NormalDistribution(3, 1);
	private IntBinaryOperator setupTimeGenerator = (i1, i2) -> Math.abs(i1 - i2);
	private AbstractRealDistribution distributionOfToolsInWorkcenter = new NormalDistribution(3, 1);
	private AbstractRealDistribution distributionOfMachineAvailability = new NormalDistribution(5, 3);
	private AbstractRealDistribution distributionOfJobReleases= new UniformRealDistribution(0, 20);
	private AbstractRealDistribution distributionOfJobDuration = new UniformRealDistribution(10, 20);
	private AbstractRealDistribution distributionOfOperationProcessTime = new UniformRealDistribution(1, 20);
	private OpenShopMetric metric;

	public OpenShopProblem generate() {

		/* sanity check */
		if (this.numWorkcenters <= 0) {
			throw new UnsupportedOperationException("Positive number of work centers required.");
		}
		if (this.numJobs <= 0) {
			throw new UnsupportedOperationException("Positive number of work centers required.");
		}
		if (this.metric == null) {
			throw new UnsupportedOperationException("Metric must be set.");
		}
		OpenShopProblemBuilder builder = new OpenShopProblemBuilder();

		/* seed all the distributions */
		Random rand = new Random(this.seed);
		this.distributionOfMachinesPerWorkcenter.reseedRandomGenerator(this.seed);
		this.distributionOfOperationsPerJob.reseedRandomGenerator(this.seed);
		this.distributionOfToolsInWorkcenter.reseedRandomGenerator(this.seed);
		this.distributionOfMachineAvailability.reseedRandomGenerator(this.seed);
		this.distributionOfJobReleases.reseedRandomGenerator(this.seed);
		this.distributionOfJobDuration.reseedRandomGenerator(this.seed);
		this.distributionOfOperationProcessTime.reseedRandomGenerator(this.seed);

		/* create work centers */
		int mId = 0;
		for (int i = 0; i < this.numWorkcenters; i++) {
			int numTools = this.sampleMinInteger(1, this.distributionOfToolsInWorkcenter);
			int[][] setupMatrix = new int[numTools][numTools];
			for (int j = 0; j < numTools; j++) {
				for (int k = 0; k < numTools; k++) {
					setupMatrix[j][k] = this.setupTimeGenerator.applyAsInt(j, k);
				}
			}
			String wcName = "WC" + (i + 1);
			builder.withWorkcenter(wcName, setupMatrix);
			int numMachines = this.sampleMinInteger(1, this.distributionOfMachinesPerWorkcenter);
			for (int j = 0; j < numMachines; j++) {
				mId ++;
				builder.withMachineForWorkcenter("M" + mId, wcName, this.sampleMinInteger(0, this.distributionOfMachineAvailability), rand.nextInt(numTools));
			}
		}

		/* create jobs */
		int opId = 0;
		for (int j = 0; j < this.numJobs; j++) {
			String jobId = "J" + (j+1);
			int releaseDate = this.sampleMinInteger(0, this.distributionOfJobReleases);
			int dueDate = releaseDate + this.sampleMinInteger(0, this.distributionOfJobDuration);
			builder.withJob(jobId, releaseDate, dueDate, rand.nextInt(10));
			int numOps = this.sampleMinInteger(1, this.distributionOfOperationsPerJob);
			for (int k = 0; k < numOps; k++) {
				opId ++;
				String wcId = "WC" + (rand.nextInt(this.numWorkcenters) + 1);
				int numTools = builder.getWorkcenter(wcId).getSetupMatrix().length;
				builder.withOperationForJob("O" + opId, jobId, this.sampleMinInteger(1, this.distributionOfOperationProcessTime), rand.nextInt(numTools), wcId);
			}
		}
		builder.withMetric(this.metric);
		return builder.build();
	}

	private int sampleMinInteger(final int min, final AbstractRealDistribution dist) {
		return Math.max(min, (int)Math.round(dist.sample()));
	}

	public int getNumWorkcenters() {
		return this.numWorkcenters;
	}

	public void setNumWorkcenters(final int numWorkcenters) {
		this.numWorkcenters = numWorkcenters;
	}

	public int getNumJobs() {
		return this.numJobs;
	}

	public void setNumJobs(final int numJobs) {
		this.numJobs = numJobs;
	}

	public OpenShopMetric getMetric() {
		return this.metric;
	}

	public void setMetric(final OpenShopMetric metric) {
		this.metric = metric;
	}

	public int getSeed() {
		return this.seed;
	}

	public void setSeed(final int seed) {
		this.seed = seed;
	}
}
