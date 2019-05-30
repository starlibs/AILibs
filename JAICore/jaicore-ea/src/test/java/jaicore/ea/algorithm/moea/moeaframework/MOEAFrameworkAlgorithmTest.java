package jaicore.ea.algorithm.moea.moeaframework;

import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ea.algorithm.moea.moeaframework.util.MOEAFrameworkUtil;

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

		System.out.println("Number of Evaluations: " + algo.getNumberOfEvaluations());
		System.out.println("Number of Generations: " + algo.getNumberOfGenerationsEvolved());

		System.out.println(MOEAFrameworkUtil.populationToString(res.getResult()));

	}

}
