package ai.libs.jaicore.planning.hierarchical.testproblems.nesteddichotomies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCOperation;
import ai.libs.jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.ceocstn.OCMethod;
import ai.libs.jaicore.planning.hierarchical.problems.rtn.RTNMethod;
import ai.libs.jaicore.planning.hierarchical.problems.rtn.RTNPlanningDomain;
import ai.libs.jaicore.planning.hierarchical.problems.rtn.RTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.rtn.RTaskNetwork;
import ai.libs.jaicore.planning.hierarchical.problems.rtn.StateReducer;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class RTNNDProblemGenerator {
	
	public static RTNPlanningProblem getNestedDichotomyCreationProblem(String rootClusterName, List<String> classes) {
		
		/* get problem on the basis of the standard CEOC STN problem */
		CEOCSTNPlanningProblem problem = ai.libs.jaicore.planning.hierarchical.testproblems.nesteddichotomies.CEOCSTNNDProblemGenerator.getNestedDichotomyCreationProblem(rootClusterName, classes, true, 1, 1);
		
		/* now derive RTN methods from the CEOC STN methods */
		Collection<? extends CEOCOperation> operations = problem.getDomain().getOperations();
		Collection<RTNMethod> methods = new ArrayList<>();
		for (OCMethod method : problem.getDomain().getMethods()) {
			Map<Literal,StateReducer> reducers = new HashMap<>();
			
			RTaskNetwork network = new RTaskNetwork(method.getNetwork(), reducers);
			RTNMethod rtnmethod = new RTNMethod(method.getName(), method.getParameters(), method.getTask(), method.getPrecondition(), network, method.isLonely(), method.getOutputs());
			methods.add(rtnmethod);
		}
		RTNPlanningDomain domain = new RTNPlanningDomain(operations, methods);
		RTaskNetwork network = new RTaskNetwork(new TaskNetwork("refine('" + rootClusterName + "')"), new HashMap<>());
		return new RTNPlanningProblem(domain, problem.getInit(), network);
	}
}
