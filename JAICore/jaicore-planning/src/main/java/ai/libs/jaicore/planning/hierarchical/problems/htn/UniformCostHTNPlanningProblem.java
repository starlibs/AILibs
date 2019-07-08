package ai.libs.jaicore.planning.hierarchical.problems.htn;

public class UniformCostHTNPlanningProblem extends CostSensitiveHTNPlanningProblem<IHTNPlanningProblem, Integer> {

	private static final long serialVersionUID = -547118545330012925L;

	public UniformCostHTNPlanningProblem(final IHTNPlanningProblem corePlanningProblem) {
		super(corePlanningProblem, n -> n.getActions().size());
	}
}
