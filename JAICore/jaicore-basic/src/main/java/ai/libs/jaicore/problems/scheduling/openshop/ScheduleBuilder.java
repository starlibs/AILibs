package ai.libs.jaicore.problems.scheduling.openshop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;

public class ScheduleBuilder {
	private final OpenShopProblem problem;
	private final Set<String> assignedOperations = new HashSet<>();
	private final List<Pair<Operation, Machine>> assignments = new ArrayList<>();

	public ScheduleBuilder(final OpenShopProblemBuilder builder) {
		this(builder.fork().withMetric(OpenShopMetric.TOTALFLOWTIME).build());
	}

	public ScheduleBuilder(final OpenShopProblem problem) {
		super();
		this.problem = problem;
	}

	public OpenShopProblem getProblem() {
		return this.problem;
	}

	public List<Pair<Operation, Machine>> getOrder() {
		return this.assignments;
	}

	public ScheduleBuilder assign(final Operation o, final Machine m) {
		if (this.assignedOperations.contains(o.getName())) {
			throw new IllegalArgumentException("Operation " + o.getName() + " has already been assigned.");
		}
		if (!o.getWorkcenter().getMachines().contains(m)) {
			throw new IllegalArgumentException("Cannot assign operation " + o.getName() + " to machine " + m.getMachineID() + ", because that machine is in work center " + m.getWorkcenter().getWorkcenterID() + ", but the operation must be executed in work center " + o.getWorkcenter().getWorkcenterID());
		}
		this.assignments.add(new Pair<>(o, m));
		this.assignedOperations.add(o.getName());
		return this;
	}

	public ScheduleBuilder assign(final String o, final String m) {
		return this.assign(this.problem.getOperations().get(o), this.problem.getMachines().get(m));
	}


	public Schedule build() {
		Collection<Operation> unassignedOperations = SetUtil.difference(this.assignments.stream().map(Pair::getX).collect(Collectors.toSet()), this.problem.getOperations().values());
		if (!unassignedOperations.isEmpty()) {
			throw new UnsupportedOperationException("Cannot create partial schedules at the moment.");
		}
		return new Schedule(this.assignments);
	}
}
