package ai.libs.hasco.test;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

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
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.OCIPMethod;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;

public class NewReductionSandbox {

    private static final File problemFileNormal = new File("../../../JAICore/jaicore-components/testrsc/simpleproblem.json");
    private static final String reqInterfaceNormal = "IFace";
    private static final File problemFileListInterface = new File("../../../JAICore/jaicore-components/testrsc/list_required_interface_reduction.json");

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
        htnProblem.getDomain().getMethods().forEach(m -> System.out.println(this.prettifyMethod((OCIPMethod) m)));
    }

    private String prettifyMethod(final OCIPMethod method) {
        String tasknet = String.join("\n\t\t", method.getNetwork().getItems().stream().map(i -> i.toString()).collect(Collectors.toSet()));

        return method.getName() + "(" + method.getParameters() + "): \n" +
                "\ttask-name: " + method.getTask() + "\n" +
                "\tpre-condition: " + method.getPrecondition() + "\n" +
                "\ttask-network: \n\t\t" + tasknet + "\n" +
                "\toutputs: " + method.getOutputs() + "\n" +
                "\teval-pre-condition: " + method.getEvaluablePrecondition() + "\n";
    }
}
