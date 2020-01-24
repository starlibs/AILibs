package ai.libs.jaicore.ea.algorithm.moea.moeaframework;

import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.junit.Test;

public class MOEAFrameworkAlgorithmTest {

	@Test
	public void testNext() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		Properties imports = new Properties();
		imports.setProperty(IMOEAFrameworkAlgorithmConfig.K_MOEAFRAMEWORK_ALGORITHM_NAME, "NSGAII");
		imports.setProperty(IMOEAFrameworkAlgorithmConfig.K_GENERATIONS, "10");

		IMOEAFrameworkAlgorithmConfig config = ConfigFactory.create(IMOEAFrameworkAlgorithmConfig.class, imports);
		IMOEAFrameworkAlgorithmInput problem = new SimpleTestProblem();
		MOEAFrameworkAlgorithm algo = new MOEAFrameworkAlgorithm(config, problem);
		MOEAFrameworkAlgorithmResult res = algo.call();

		assertTrue("Not a single evaluation was made", algo.getNumberOfEvaluations() > 0);
		assertTrue("Did not evolve anything.", algo.getNumberOfGenerationsEvolved() > 0);
	}

}
