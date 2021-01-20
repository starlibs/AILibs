package ai.libs.jaicore.ml.hpo.ga;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.sets.PartialOrderedSet;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IEvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.model.Interface;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.Parameter;

public class SimpleGeneticAlgorithmTest {

	public static final double ALLOWED_DELTA = 0.1;

	private interface SolutionTester {
		public boolean acceptSolution(SimpleGeneticAlgorithm algo, IComponentInstance solution);
	}

	public static Arguments getSimpleProblem() {
		PartialOrderedSet<IParameter> parameters = new PartialOrderedSet<>();
		parameters.add(new Parameter("x", new NumericParameterDomain(false, -10, 10), 1));
		Component comp = new Component("A", Arrays.asList("RI"), new ArrayList<IRequiredInterfaceDefinition>(), parameters, new ArrayList<>());
		IComponentInstance ci = ComponentUtil.getDefaultParameterizationOfComponent(comp);

		IObjectEvaluator<IComponentInstance, Double> evaluator = (o) -> {
			double val = Math.pow(0.75 * (Double.parseDouble(o.getParameterValues().get("x")) - 5), 2);
			return val;
		};

		IComponentInstanceHPOGAInput input = new ComponentInstanceHPOGAInput(ci, evaluator);

		SolutionTester tester = (a, r) -> {
			double xParam = Double.parseDouble(r.getParameterValue("x"));
			return xParam > 0 && Math.abs(5 - xParam) < ALLOWED_DELTA;
		};

		return Arguments.of(ConfigFactory.create(ISimpleGeneticAlgorithmConfig.class), input, tester);
	}

	public static Arguments getSimpleNestedProblem() {
		PartialOrderedSet<IParameter> parameters = new PartialOrderedSet<>();
		parameters.add(new Parameter("x", new NumericParameterDomain(false, -10, 10), 1));

		PartialOrderedSet<IParameter> parameters2 = new PartialOrderedSet<>();
		parameters2.add(new Parameter("y", new NumericParameterDomain(false, -10, 10), -1));

		IRequiredInterfaceDefinition reqI = new Interface("N", "B", false, false, true, 1, 1);
		IComponent comp = new Component("A", Arrays.asList("RI"), Arrays.asList(reqI), parameters, new ArrayList<>());
		IComponent nestedComp = new Component("B", Arrays.asList("RI2"), new ArrayList<IRequiredInterfaceDefinition>(), parameters2, new ArrayList<>());

		IComponentInstance ci = ComponentUtil.getDefaultParameterizationOfComponent(comp);
		ci.getSatisfactionOfRequiredInterfaces().put("N", Arrays.asList(ComponentUtil.getDefaultParameterizationOfComponent(nestedComp)));

		IObjectEvaluator<IComponentInstance, Double> evaluator = (o) -> {
			double xParam = Double.parseDouble(o.getParameterValue("x"));
			double yParam = Double.parseDouble(o.getSatisfactionOfRequiredInterfaces().get("N").get(0).getParameterValue("y"));
			return Math.pow((xParam - 5) * (yParam - 3), 2);
		};
		IComponentInstanceHPOGAInput input = new ComponentInstanceHPOGAInput(ci, evaluator);

		SolutionTester tester = (a, r) -> {
			double xParam = Double.parseDouble(r.getParameterValue("x"));
			double yParam = Double.parseDouble(r.getSatisfactionOfRequiredInterfaces().get("N").get(0).getParameterValue("y"));
			boolean res = true;
			res &= xParam > 0 && Math.abs(5 - xParam) < ALLOWED_DELTA;
			res &= yParam > 0 && Math.abs(3 - yParam) < ALLOWED_DELTA;
			return res;
		};
		return Arguments.of(ConfigFactory.create(ISimpleGeneticAlgorithmConfig.class), input, tester);
	}

	public static Arguments getSimpleNestedListProblem() {
		PartialOrderedSet<IParameter> parameters = new PartialOrderedSet<>();
		parameters.add(new Parameter("x", new NumericParameterDomain(false, -10, 10), 1));
		parameters.add(new Parameter("z", new CategoricalParameterDomain(Arrays.asList("A", "B", "C", "D")), "B"));

		PartialOrderedSet<IParameter> parameters2 = new PartialOrderedSet<>();
		parameters2.add(new Parameter("y", new NumericParameterDomain(false, -10, 10), -1));

		IRequiredInterfaceDefinition reqI = new Interface("N", "B", false, false, true, 2, 2);
		IComponent comp = new Component("A", Arrays.asList("RI"), Arrays.asList(reqI), parameters, new ArrayList<>());
		IComponent nestedComp = new Component("B", Arrays.asList("RI2"), new ArrayList<IRequiredInterfaceDefinition>(), parameters2, new ArrayList<>());

		IComponentInstance ci = ComponentUtil.getDefaultParameterizationOfComponent(comp);
		ci.getSatisfactionOfRequiredInterfaces().put("N", Arrays.asList(ComponentUtil.getDefaultParameterizationOfComponent(nestedComp), ComponentUtil.getDefaultParameterizationOfComponent(nestedComp)));

		IObjectEvaluator<IComponentInstance, Double> evaluator = (o) -> {
			double xParam = Double.parseDouble(o.getParameterValue("x"));
			String zParam = o.getParameterValue("z");
			double yParam = Double.parseDouble(o.getSatisfactionOfRequiredInterfaces().get("N").get(0).getParameterValue("y"));
			double y2Param = Double.parseDouble(o.getSatisfactionOfRequiredInterfaces().get("N").get(1).getParameterValue("y"));
			double val = Math.pow((xParam - 5) * (yParam - 3) * (y2Param - 1), 2);
			switch (zParam) {
			case "A":
				val += 0.1;
				break;
			case "D":
				val += 0.2;
				break;
			case "C":
				val += 0.3;
				break;
			}
			return val;
		};
		IComponentInstanceHPOGAInput input = new ComponentInstanceHPOGAInput(ci, evaluator);

		SolutionTester tester = (a, r) -> {
			double xParam = Double.parseDouble(r.getParameterValue("x"));
			String zParam = r.getParameterValue("z");
			double yParam = Double.parseDouble(r.getSatisfactionOfRequiredInterfaces().get("N").get(0).getParameterValue("y"));
			double y2Param = Double.parseDouble(r.getSatisfactionOfRequiredInterfaces().get("N").get(1).getParameterValue("y"));
			boolean res = true;
			res &= xParam > 0 && Math.abs(5 - xParam) < ALLOWED_DELTA;
			res &= yParam > 0 && Math.abs(3 - yParam) < ALLOWED_DELTA;
			res &= y2Param > 0 && Math.abs(1 - y2Param) < ALLOWED_DELTA;
			res &= zParam.equals("B");
			return res;
		};
		return Arguments.of(ConfigFactory.create(ISimpleGeneticAlgorithmConfig.class), input, tester);
	}

	public static Arguments getMaxEvaluationsProblem() {
		PartialOrderedSet<IParameter> parameters = new PartialOrderedSet<>();
		parameters.add(new Parameter("x", new NumericParameterDomain(false, -10, 10), 1));
		Component comp = new Component("A", Arrays.asList("RI"), new ArrayList<IRequiredInterfaceDefinition>(), parameters, new ArrayList<>());
		IComponentInstance ci = ComponentUtil.getDefaultParameterizationOfComponent(comp);

		IObjectEvaluator<IComponentInstance, Double> evaluator = (o) -> {
			return Math.pow(0.75 * (Double.parseDouble(o.getParameterValues().get("x")) - 5), 2);
		};

		IComponentInstanceHPOGAInput input = new ComponentInstanceHPOGAInput(ci, evaluator);
		SolutionTester tester = (a, r) -> {
			return a.getNumEvaluations() == 20;
		};

		ISimpleGeneticAlgorithmConfig config = ConfigFactory.create(ISimpleGeneticAlgorithmConfig.class);
		config.setProperty(ISimpleGeneticAlgorithmConfig.K_MAX_EVALUATIONS, "20");

		return Arguments.of(config, input, tester);
	}

	public static Stream<Arguments> getGAProblems() {
		return Stream.of(/*getSimpleProblem(), getSimpleNestedProblem(), getSimpleNestedListProblem(),*/ getMaxEvaluationsProblem());
	}

	@Test
	public void testAnytimeSimpleGeneticAlgorithm() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		ISimpleGeneticAlgorithmConfig config = ConfigFactory.create(ISimpleGeneticAlgorithmConfig.class);
		config.setProperty(ISimpleGeneticAlgorithmConfig.K_MAX_GENERATIONS, "-1");
		config.setProperty(ISimpleGeneticAlgorithmConfig.K_MAX_RUNTIME, "5000");

		PartialOrderedSet<IParameter> parameters = new PartialOrderedSet<>();
		parameters.add(new Parameter("x", new NumericParameterDomain(false, -10, 10), 1));
		Component comp = new Component("A", Arrays.asList("RI"), new ArrayList<IRequiredInterfaceDefinition>(), parameters, new ArrayList<>());
		IComponentInstance ci = ComponentUtil.getDefaultParameterizationOfComponent(comp);

		IObjectEvaluator<IComponentInstance, Double> evaluator = (o) -> {
			double val = Math.pow(0.75 * (Double.parseDouble(o.getParameterValues().get("x")) - 5), 2);
			return val;
		};

		IComponentInstanceHPOGAInput input = new ComponentInstanceHPOGAInput(ci, evaluator);

		SimpleGeneticAlgorithm algo = new SimpleGeneticAlgorithm(config, input);
		long startTime = System.currentTimeMillis();
		algo.call();
		long duration = System.currentTimeMillis() - startTime;

		assertTrue(duration > 4000 && duration < 6000, "Runtime was not on time (plus/minus a second), so it was " + duration + "ms instead of " + 5000 + "ms");
	}

	@ParameterizedTest
	@MethodSource("getGAProblems")
	public void testSimpleGeneticAlgorithm(final ISimpleGeneticAlgorithmConfig config, final IComponentInstanceHPOGAInput input, final SolutionTester tester)
			throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		SimpleGeneticAlgorithm algo = new SimpleGeneticAlgorithm(config, input);
		IEvaluatedSoftwareConfigurationSolution<Double> solution = algo.call();
		System.out.println(ComponentInstanceUtil.getComponentInstanceString(solution.getComponentInstance()));
		assertTrue(tester.acceptSolution(algo, solution.getComponentInstance()), "Solution deviates for at least one parameter by more than " + ALLOWED_DELTA + " from the expected results.");
	}

}
