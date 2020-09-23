package ai.libs.jaicore.problems.scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.problems.scheduling.computers.DefaultSchedulingComputer;

public class ScheduleBuilder {
	private final IJobSchedulingInput problem;
	private final Set<String> assignedOperations = new HashSet<>();
	private final List<Pair<Operation, Machine>> assignments = new ArrayList<>();
	private IScheduleComputer schedulingComputer = new DefaultSchedulingComputer();

	public ScheduleBuilder(final JobSchedulingProblemBuilder builder) {
		this(builder.fork().withMetric(JobShopMetric.TOTALFLOWTIME).build());
	}

	public ScheduleBuilder(final IJobSchedulingInput problem) {
		super();
		this.problem = problem;
	}

	public IJobSchedulingInput getProblem() {
		return this.problem;
	}

	public List<Pair<Operation, Machine>> getOrder() {
		return this.assignments;
	}

	public ScheduleBuilder assign(final Operation o, final Machine m) {
		Objects.requireNonNull(o);
		Objects.requireNonNull(m);
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
		return this.assign(this.problem.getOperation(o), this.problem.getMachine(m));
	}

	public ScheduleBuilder withSchedulingComputer(final IScheduleComputer schedulingComputer) {
		this.schedulingComputer = schedulingComputer;
		return this;
	}


	public Schedule build() {
		Collection<Operation> unassignedOperations = SetUtil.difference(this.problem.getOperations(), this.assignments.stream().map(Pair::getX).collect(Collectors.toSet()));
		if (!unassignedOperations.isEmpty()) {
			throw new UnsupportedOperationException("Cannot create partial schedules at the moment. Unassigned operations: " + unassignedOperations);
		}
		return new Schedule(this.problem, this.assignments, this.schedulingComputer);
	}
}
