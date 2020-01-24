package ai.libs.jaicore.math.bayesianinference;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import ai.libs.jaicore.basic.Tester;

public class BayesianNetworkTester extends Tester {

	/* define the variable names */
	private static final String V_ASTHMA = "Asthma";
	private static final String V_COPD = "COPD";
	private static final String V_REDUCTION = "Reduction";
	private static final String V_COUGH = "Cough";
	private static final String V_DYSPNEA = "Dyspnea";


	@Test
	public void testHospitalProblem() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* define number of dummy variables */
		int numDummyVars = 0;
		this.logger.info("Attempting the hospital problem with {} dummy variables.", numDummyVars);

		/* define the query */
		Map<String, Boolean> evidences = new HashMap<>();
		evidences.put(V_DYSPNEA, true);
		Collection<String> queryVariables = Arrays.asList(V_COPD);
		this.logger.info("Creating the problem.");
		BayesianInferenceProblem prob = this.getModifiedHospitalProblem(queryVariables, evidences, numDummyVars);
		this.logger.info("Created a Bayesian Network with {} nodes and the respective queries.", prob.getNetwork().getNet().getItems().size());

		/* create a solver and solve the problem */
		ABayesianInferenceAlgorithm solver = new EnumerationBasedBayesianInferenceSolver(prob);
		this.logger.info("Now running solver {} on the problem.", solver.getClass().getName());
		long start = System.currentTimeMillis();
		DiscreteProbabilityDistribution distribution = solver.call();
		int runtime = (int)(System.currentTimeMillis() - start);
		this.logger.info("Time to solution: {}", runtime);
		this.checkDistribution(distribution);
	}

	public BayesianInferenceProblem getHospitalProblem(final Collection<String> queryVars, final Map<String, Boolean> evidence, final int dummyVars) {

		/* define the Bayesian Network */
		BayesNet bn = new BayesNet();
		bn.addNode(V_ASTHMA);
		bn.addProbability(V_ASTHMA, 0.08);
		bn.addNode(V_COPD);
		bn.addProbability(V_COPD, 0.09);
		bn.addNode(V_REDUCTION);
		bn.addDependency(V_REDUCTION, V_ASTHMA);
		bn.addDependency(V_REDUCTION, V_COPD);
		bn.addProbability(V_REDUCTION, Arrays.asList(), 0.1);
		bn.addProbability(V_REDUCTION, Arrays.asList(V_ASTHMA), 0.5);
		bn.addProbability(V_REDUCTION, Arrays.asList(V_COPD), 0.85);
		bn.addProbability(V_REDUCTION, Arrays.asList(V_ASTHMA, V_COPD), 0.95);
		bn.addNode(V_COUGH);
		bn.addDependency(V_COUGH, V_REDUCTION);
		bn.addProbability(V_COUGH, Arrays.asList(), 0.21);
		bn.addProbability(V_COUGH, Arrays.asList(V_REDUCTION), 0.63);
		bn.addNode(V_DYSPNEA);
		bn.addDependency(V_DYSPNEA, V_REDUCTION);
		bn.addProbability(V_DYSPNEA, Arrays.asList(), 0.001);
		bn.addProbability(V_DYSPNEA, Arrays.asList(V_REDUCTION), 0.63);

		for (int i = 0; i < dummyVars; i++) {
			bn.addNode("ART" + i);
			bn.addProbability("ART" + i, 1.0 / (i + 2));
		}

		/* formalize the problem */
		return new BayesianInferenceProblem(bn, evidence, queryVars);
	}

	public BayesianInferenceProblem getModifiedHospitalProblem(final Collection<String> queryVars, final Map<String, Boolean> evidence, final int dummyVars) {

		/* define the Bayesian Network */
		BayesNet bn = new BayesNet();
		bn.addNode(V_ASTHMA);
		bn.addProbability(V_ASTHMA, 0.08);
		bn.addNode(V_COPD);
		bn.addProbability(V_COPD, 0.09);
		bn.addNode(V_REDUCTION);
		bn.addDependency(V_REDUCTION, V_ASTHMA);
		bn.addDependency(V_REDUCTION, V_COPD);
		bn.addProbability(V_REDUCTION, Arrays.asList(), 0.1);
		bn.addProbability(V_REDUCTION, Arrays.asList(V_ASTHMA), 0.5);
		bn.addProbability(V_REDUCTION, Arrays.asList(V_COPD), 0.85);
		bn.addProbability(V_REDUCTION, Arrays.asList(V_ASTHMA, V_COPD), 0.95);
		bn.addNode(V_COUGH);
		bn.addDependency(V_COUGH, V_REDUCTION);
		bn.addProbability(V_COUGH, Arrays.asList(), 0.21);
		bn.addProbability(V_COUGH, Arrays.asList(V_REDUCTION), 0.63);
		bn.addNode(V_DYSPNEA);
		bn.addDependency(V_DYSPNEA, V_REDUCTION);
		bn.addDependency(V_DYSPNEA, V_COPD);
		bn.addProbability(V_DYSPNEA, Arrays.asList(), 0.01);
		bn.addProbability(V_DYSPNEA, Arrays.asList(V_REDUCTION, V_COPD), 0.9);
		bn.addProbability(V_DYSPNEA, Arrays.asList(V_COPD), 0.3);
		bn.addProbability(V_DYSPNEA, Arrays.asList(V_REDUCTION), 0.1);

		for (int i = 0; i < dummyVars; i++) {
			bn.addNode("ART" + i);
			bn.addProbability("ART" + i, 1.0 / (i + 2));
		}

		/* formalize the problem */
		return new BayesianInferenceProblem(bn, evidence, queryVars);
	}

	@Test
	public void solveStandardExample() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* define the variable names */
		final String V_BURGLARY = "Burglary";
		final String V_EARTHQUAKE = "Earthquake";
		final String V_ALARM = "Alarm";
		final String V_JOHN = "John calls";
		final String V_MARY = "Mary calls";

		/* define the Bayesian Network */
		BayesNet bn = new BayesNet();
		bn.addNode(V_BURGLARY);
		bn.addProbability(V_BURGLARY, 0.001);
		bn.addNode(V_EARTHQUAKE);
		bn.addProbability(V_EARTHQUAKE, 0.002);
		bn.addNode(V_ALARM);
		bn.addDependency(V_ALARM, V_BURGLARY);
		bn.addDependency(V_ALARM, V_EARTHQUAKE);
		bn.addProbability(V_ALARM, Arrays.asList(), 0.001);
		bn.addProbability(V_ALARM, Arrays.asList(V_BURGLARY), 0.94);
		bn.addProbability(V_ALARM, Arrays.asList(V_EARTHQUAKE), 0.29);
		bn.addProbability(V_ALARM, Arrays.asList(V_BURGLARY, V_EARTHQUAKE), 0.95);
		bn.addNode(V_JOHN);
		bn.addDependency(V_JOHN, V_ALARM);
		bn.addProbability(V_JOHN, Arrays.asList(), 0.05);
		bn.addProbability(V_JOHN, Arrays.asList(V_ALARM), 0.9);
		bn.addNode(V_MARY);
		bn.addDependency(V_MARY, V_ALARM);
		bn.addProbability(V_MARY, Arrays.asList(), 0.01);
		bn.addProbability(V_MARY, Arrays.asList(V_ALARM), 0.7);

		/* define the query */
		Map<String, Boolean> evidences = new HashMap<>();
		evidences.put(V_JOHN, true);
		evidences.put(V_MARY, true);
		Collection<String> queryVariables = Arrays.asList(V_BURGLARY);

		/* formalize the problem */
		BayesianInferenceProblem prob = new BayesianInferenceProblem(bn, evidences, queryVariables);

		/* solve the query */
		EnumerationBasedBayesianInferenceSolver solver = new EnumerationBasedBayesianInferenceSolver(prob);
		DiscreteProbabilityDistribution distribution = solver.call();
		this.checkDistribution(distribution);
	}

	private void checkDistribution(final DiscreteProbabilityDistribution distribution) {
		this.logger.info("Probabilities are: {}", new DiscreteProbabilityDistributionPrinter().getTable(distribution));
		double sum = distribution.getProbabilities().values().stream().reduce((s,a) -> s + a).get();
		assertEquals(1.0, sum, 0.0001);
	}
}
