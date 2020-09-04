package ai.libs.hasco.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Ignore;
import org.junit.Test;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.hasco.core.reduction.softcomp2planning.HASCOReduction;
import ai.libs.jaicore.basic.algorithm.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCOperation;
import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.BlindForwardDecompositionHTNPlanner;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.OCIPMethod;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;

public class NewReductionSandbox {
	// For easy navigation within the console.
	private static int methodCounter = 1;
	private static int operationCounter = 1;

	private static final File problemFileNormal = new File("../../../JAICore/jaicore-components/testrsc/simpleproblem.json");
	private static final String reqInterfaceNormal = "IFace";
	private static final File problemFileListInterface = new File("../../../JAICore/jaicore-components/testrsc/list_required_interface_reduction.json");

	@Ignore
	@Test
	public void test() throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* load original software configuration problem */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(problemFileNormal, reqInterfaceNormal, n -> 0.0);

		/* derive HTN planning problem*/
		HASCOReduction<Double> reduction = new HASCOReduction<>();
		CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, Double> htnProblem = reduction.encodeProblem(problem);
		htnProblem.getDomain().getMethods().forEach(m -> System.out.println(this.prettifyMethod((OCIPMethod) m)));

		/* solve the HTN planning problem */
		BlindForwardDecompositionHTNPlanner<Double> algo = new BlindForwardDecompositionHTNPlanner<>(htnProblem, n -> 0.0);
		IEvaluatedPlan<Double> plan = algo.call();

		/* reproduce the configuration solution from plan */
		System.out.println("Solution plan:");
		plan.getActions().forEach(a -> System.out.println("\t" + a.getEncoding()));
		Monom finalState = HASCOUtil.getFinalStateOfPlan(htnProblem.getInit(), plan);
		System.out.println("Final state: ");
		finalState.forEach(l -> System.out.println("\t- " + l));

		/* your work :D (later) */

	}

	@Test
	public void listIFace() throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		/* load original software configuration problem */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(problemFileListInterface, reqInterfaceNormal, n -> 0.0);

		/* derive HTN planning problem*/
		HASCOReduction<Double> reduction = new HASCOReduction<>();
		CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, Double> htnProblem = reduction.encodeProblem(problem);
		System.out.println("<<<=======================| Init State |=======================>>>");
		htnProblem.getInit().forEach(l -> System.out.println(l));
		System.out.println("<<<=======================| Initial Task Network |=======================>>>");
		System.out.println(htnProblem.getNetwork().getLineBasedStringRepresentation());
		System.out.println("<<<=======================| Methods |=======================>>>");
		htnProblem.getDomain().getMethods().forEach(m -> System.out.println(this.prettifyMethod((OCIPMethod) m)));
		System.out.println("<<<=======================| Operations |=======================>>>");
		htnProblem.getDomain().getOperations().forEach(o -> System.out.println(this.prettifyOperation((CEOCOperation) o)));

		/* solve the HTN planning problem */
		BlindForwardDecompositionHTNPlanner<Double> algo = new BlindForwardDecompositionHTNPlanner<>(htnProblem, n -> 0.0);
		//		algo.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		int seenPlans = 0;
		while (algo.hasNext()) {
			IAlgorithmEvent event = algo.nextWithException();
			if (event instanceof ASolutionCandidateFoundEvent) {

				Plan plan = (Plan)((ASolutionCandidateFoundEvent) event).getSolutionCandidate();

				/* reproduce the configuration solution from plan */
				System.out.println("Solution plan:");
				plan.getActions().forEach(a -> System.out.println("\t" + a.getEncoding()));
				Monom finalState = HASCOUtil.getFinalStateOfPlan(htnProblem.getInit(), plan);
				System.out.println("Final state: ");
				finalState.forEach(l -> System.out.println("\t- " + l));

				seenPlans ++;
			}
		}

		int expectedPlans = 10001010;
		assertEquals(expectedPlans, seenPlans);
	}

	private String prettifyMethod(final OCIPMethod method) {
		String tasknet = method.getNetwork().getLineBasedStringRepresentation();

		return methodCounter++ + ") " + method.getName() + "(" + method.getParameters() + ")\n" +
		"\ttask-name: " + method.getTask() + "\n" +
		"\tpre-condition: " + method.getPrecondition() + "\n" +
		"\ttask-network: \n\t" + tasknet + "\n" +
		"\toutputs: " + method.getOutputs() + "\n" +
		"\teval-pre-condition: " + method.getEvaluablePrecondition() + "\n";
	}

	private String prettifyOperation(final CEOCOperation operation) {
		return operationCounter++ + ") " + operation.getName() + "(" + operation.getParams() + ")\n" +
				"\tpre-comndition: " + operation.getPrecondition() + "\n" +
				"\tadd-list: " + operation.getAddLists() + "\n" +
				"\tdelete-list: " + operation.getDeleteLists() + "\n";
	}
}
