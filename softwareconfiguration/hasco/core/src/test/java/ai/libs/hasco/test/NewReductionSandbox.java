package ai.libs.hasco.test;

import java.io.File;
import java.io.IOException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.hasco.core.reduction.softcomp2planning.HASCOReduction;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.BlindForwardDecompositionHTNPlanner;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;

public class NewReductionSandbox {

	private static final File problemFileNormal = new File("../../../JAICore/jaicore-components/testrsc/simpleproblem.json");
	private static final String reqInterfaceNormal = "IFace";
	private static final File problemFileListInterface = null;

	@Test
	public void test() throws IOException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* load original software configuration problem */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(problemFileNormal, reqInterfaceNormal, n -> 0.0);

		/* derive HTN planning problem*/
		HASCOReduction<Double> reduction = new HASCOReduction<>();
		CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, Double> htnProblem = reduction.encodeProblem(problem);
		htnProblem.getDomain().getMethods().forEach(m -> System.out.println(m));

		/* solve the HTN planning problem */
		BlindForwardDecompositionHTNPlanner algo = new BlindForwardDecompositionHTNPlanner(htnProblem, n -> 0.0);
		IEvaluatedPlan<Double> plan = (IEvaluatedPlan<Double>)algo.call();

		/* reproduce the configuration solution from plan */
		System.out.println("Solution plan:");
		plan.getActions().forEach(a -> System.out.println("\t" + a.getEncoding()));
		Monom finalState = HASCOUtil.getFinalStateOfPlan(htnProblem.getInit(), plan);
		System.out.println("Final state: ");
		finalState.forEach(l -> System.out.println("\t- " + l));

		/* your work :D (later) */
	}

}
