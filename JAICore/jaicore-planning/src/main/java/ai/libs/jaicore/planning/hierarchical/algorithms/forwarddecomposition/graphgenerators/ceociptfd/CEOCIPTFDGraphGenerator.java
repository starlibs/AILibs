package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceociptfd;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.TaskPlannerUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceoctfd.CEOCTFDGraphGenerator;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;

/**
 * Graph Generator for HTN planning where (i) operations have conditional effects, (ii) operations may create new objects, and (iii) method preconditions may contain evaluable predicates.
 *
 * @author fmohr
 *
 */
public class CEOCIPTFDGraphGenerator extends CEOCTFDGraphGenerator {

	public CEOCIPTFDGraphGenerator(final CEOCIPSTNPlanningProblem problem) {
		super(problem);

		/* now overwrite util to get access to the evaluable predicates */
		this.util = new TaskPlannerUtil(problem.getEvaluablePlanningPredicates());
	}

	@Override
	public String toString() {
		return "CEOCIPTFDGraphGenerator [problem=" + this.problem + ", primitiveTasks=" + this.primitiveTasks + "]";
	}

}
