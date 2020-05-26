package ai.libs.jaicore.search.exampleproblems.openshop;

import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.scheduling.Machine;
import ai.libs.jaicore.problems.scheduling.JobSchedulingProblemInput;
import ai.libs.jaicore.problems.scheduling.Operation;
import ai.libs.jaicore.problems.scheduling.Schedule;
import ai.libs.jaicore.problems.scheduling.ScheduleBuilder;

public abstract class OpenShopState {
	private final JobSchedulingProblemInput problem;

	public OpenShopState(final JobSchedulingProblemInput problem) {
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
