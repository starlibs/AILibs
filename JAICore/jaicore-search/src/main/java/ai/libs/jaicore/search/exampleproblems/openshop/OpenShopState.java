package ai.libs.jaicore.search.exampleproblems.openshop;

import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.scheduling.openshop.Machine;
import ai.libs.jaicore.problems.scheduling.openshop.OpenShopProblem;
import ai.libs.jaicore.problems.scheduling.openshop.Operation;
import ai.libs.jaicore.problems.scheduling.openshop.Schedule;
import ai.libs.jaicore.problems.scheduling.openshop.ScheduleBuilder;

public abstract class OpenShopState {
	private final OpenShopProblem problem;

	public OpenShopState(final OpenShopProblem problem) {
		super();
		this.problem = problem;
	}

	public abstract List<Pair<Operation, Machine>> getPartialAssignment();

	public Schedule getSchedule() {
		ScheduleBuilder sb = new ScheduleBuilder(this.problem);
		for (Pair<Operation, Machine> p : this.getPartialAssignment()) {
			sb.assign(p.getX(), p.getY());
		}
		return sb.build();
	}
}
