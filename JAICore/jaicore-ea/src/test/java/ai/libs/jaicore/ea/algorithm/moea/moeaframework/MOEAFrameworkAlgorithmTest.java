package ai.libs.jaicore.ea.algorithm.moea.moeaframework;

import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.ea.algorithm.moea.moeaframework.IMOEAFrameworkAlgorithmConfig;
import ai.libs.jaicore.ea.algorithm.moea.moeaframework.IMOEAFrameworkAlgorithmInput;
import ai.libs.jaicore.ea.algorithm.moea.moeaframework.MOEAFrameworkAlgorithm;
import ai.libs.jaicore.ea.algorithm.moea.moeaframework.MOEAFrameworkAlgorithmResult;
import ai.libs.jaicore.ea.algorithm.moea.moeaframework.util.MOEAFrameworkUtil;

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
