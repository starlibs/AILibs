package ai.libs.jaicore.problems.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.sets.Pair;

/**
 * @author gmejia
 * @author Felix Mohr
 *
 * @version 4.0
 *
 */
public class Schedule implements ISchedule {

	private final List<Pair<Operation, Machine>> assignments;
	private final Map<Machine, List<Operation>> assignmentPerMachine = new HashMap<>();
	private final Map<Job, Integer> arrivalTimes = new HashMap<>();
	private final Map<Operation, Integer> startTimes = new HashMap<>();
	private final Map<Operation, Integer> endTimes = new HashMap<>();
	private final Map<Operation, Integer> setupStartTimes = new HashMap<>();
	private final Map<Operation, Integer> setupEndTimes = new HashMap<>();

	Schedule(final IJobSchedulingInput problemInput, final List<Pair<Operation, Machine>> assignments, final IScheduleComputer schedulingComputer) {
		super();
		this.assignments = assignments;
		this.assignments.forEach(p -> this.assignmentPerMachine.computeIfAbsent(p.getY(), m -> new ArrayList<>()).add(p.getX()));
		schedulingComputer.fillTimes(problemInput, assignments, this.arrivalTimes, this.startTimes, this.endTimes, this.setupStartTimes, this.setupEndTimes);
		for (Operation o : problemInput.getOperations()) {
			if (!this.arrivalTimes.containsKey(o.getJob())) {
				throw new IllegalStateException("No arrival time defined for job " + o.getJob().getJobID());
			}
		}
	}

	@Override
	public List<Pair<Operation, Machine>> getAssignments() {
		return this.assignments;
	}

	@Override
	public List<Operation> getOperationsAssignedToMachine(final Machine m) {
		return this.assignmentPerMachine.get(m);
	}

	@Override
	public List<Operation> getOrderOfOperationsForJob(final Job job) {
		return this.assignments.stream().map(Pair::getX).filter(o -> o.getJob().equals(job)).collect(Collectors.toList());
	}

	@Override
	public int getStartTimeOfOperation(final Operation o) {
		return this.startTimes.get(o);
	}

	@Override
	public int getEndTimeOfOperation(final Operation o) {
		return this.endTimes.get(o);
	}

	@Override
	public int getSetupStartTimeOfOperation(final Operation o) {
		return this.setupStartTimes.get(o);
	}

	@Override
	public int getSetupEndTimeOfOperation(final Operation o) {
		return this.setupEndTimes.get(o);
	}

	@Override
	public int getJobFinishTime(final Job job) {
		return job.getOperations().stream().map(this::getEndTimeOfOperation).max(Double::compare).get();
	}

	/**
	 *
	 * @param job
	 * @return
	 */
	@Override
	public int getJobFlowTime(final Job job) {
		return this.getJobFinishTime(job) - this.arrivalTimes.get(job); // the arrival times can be part of the SCHEDULE (being decisions) rather than part of the job definition
	}

	@Override
	public int getJobTardiness(final Job job) {
		return Math.max(0, this.getJobFinishTime(job) - job.getDueDate());
	}

	public String getAsString() {
		StringBuilder sb = new StringBuilder();
		List<Workcenter> workcenters = this.assignmentPerMachine.keySet().stream().map(Machine::getWorkcenter).collect(Collectors.toSet()).stream().sorted((w1, w2) -> w1.getWorkcenterID().compareTo(w2.getWorkcenterID()))
				.collect(Collectors.toList());
		for (Workcenter wc : workcenters) {
			sb.append("-------------------------------------------------------------------------------------------------\n");
			sb.append(wc.getWorkcenterID());
			sb.append("\n-------------------------------------------------------------------------------------------------\n");
			for (Machine m : wc.getMachines()) {
				sb.append("\t" + m.getMachineID() + " (init state " + m.getInitialState() + "): ");
				List<Operation> ops = this.getOperationsAssignedToMachine(m);
				if (ops != null) {
					StringBuilder opSB = new StringBuilder();
					for (Operation o : ops) {
						if (opSB.length() != 0) {
							opSB.append(" -> ");
						}
						int setupStartTime = this.setupStartTimes.get(o);
						int setupEndTime = this.setupEndTimes.get(o);
						if (setupStartTime != setupEndTime) {
							opSB.append("modify state to " + o.getStatus() + " -> ");
						}
						opSB.append(o.getName() + " (of job " + o.getJob().getJobID() + " from " + this.startTimes.get(o) + " to " + this.endTimes.get(o) + ")");
					}
					sb.append(opSB);
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public String getGanttAsString() {
		StringBuilder sb = new StringBuilder();
		List<Workcenter> workcenters = this.assignmentPerMachine.keySet().stream().map(Machine::getWorkcenter).collect(Collectors.toSet()).stream().sorted((w1, w2) -> w1.getWorkcenterID().compareTo(w2.getWorkcenterID()))
				.collect(Collectors.toList());
		for (Workcenter wc : workcenters) {
			sb.append("-------------------------------------------------------------------------------------------------\n");
			sb.append(wc.getWorkcenterID());
			sb.append("\n-------------------------------------------------------------------------------------------------\n");
			for (Machine m : wc.getMachines()) {
				sb.append("\t" + m.getMachineID() + " (init state " + m.getInitialState() + "): ");
				List<Operation> ops = this.getOperationsAssignedToMachine(m);
				int curTime = 0;
				if (ops != null) {
					StringBuilder opSB = new StringBuilder();
					int lastStatus = m.getInitialState();
					for (Operation o : ops) {
						boolean needsSetup = false;
						if (m.getWorkcenter().getSetupMatrix() != null) {
							sb.append(lastStatus + " -> " + o.getStatus() + ": " + m.getWorkcenter().getSetupMatrix()[lastStatus][o.getStatus()]);
							needsSetup = m.getWorkcenter().getSetupMatrix()[lastStatus][o.getStatus()] != 0;
						}


						lastStatus = o.getStatus();
						int setupStartTime = this.setupStartTimes.get(o);
						int setupEndTime = this.setupEndTimes.get(o);
						int startTime = this.startTimes.get(o);
						int endTime = this.endTimes.get(o);
						sb.append(o.getName() + " (" + m.getMachineID() + "): " + setupStartTime + ", " + setupEndTime + ", " + startTime + ", " + endTime);

						/*  */
						sb.append(needsSetup);
						if (needsSetup) {
							while (curTime < setupStartTime) {
								sb.append(" ");
								curTime++;
							}
							sb.append("|");
							curTime++;
							while (curTime < setupEndTime) {
								sb.append("+");
								curTime++;
							}

							while (curTime < startTime) {
								sb.append("?");
								curTime++;
							}
						} else {
							while (curTime < startTime) {
								sb.append(" ");
								curTime++;
							}
						}
						sb.append("|");
						int spaceForLabeling = endTime - startTime;
						int whiteSpace = Math.max(0, spaceForLabeling - o.getName().length());
						int whiteSpaceLeft = whiteSpace / 2;
						int startLabeling = startTime + whiteSpaceLeft;
						while (curTime < startLabeling) {
							sb.append(" ");
							curTime++;
						}
						sb.append(o.getName());
						curTime += o.getName().length();
						while (curTime < endTime) {
							sb.append(" ");
							curTime++;
						}
						if (endTime - startTime > 1) {
							sb.append("|");
						}
					}
					sb.append(opSB);
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * A schedule is active if no operation can be scheduled earlier without any other change. This method determines whether the schedule is active.
	 *
	 * @return
	 */
	public boolean isActive() {
		for (Operation o : this.setupEndTimes.keySet()) {
			if (this.canOperationBeScheduledEarlierWithoutAnyOtherEffect(o)) {
				return false;
			}
		}
		return true;
	}

	public boolean canOperationBeScheduledEarlierWithoutAnyOtherEffect(final Operation o) {

		/* first condition: there is enough free time inbetween other operations of the JOB earlier */
		Job j = o.getJob();
		int requiredTime = o.getProcessTime();
		List<Operation> otherOpsOfJobStartingEarlier = j.getOperations().stream().filter(op -> this.endTimes.containsKey(op) && this.endTimes.get(op) <= this.startTimes.get(o))
				.sorted((o1, o2) -> Integer.compare(this.startTimes.get(o1), this.startTimes.get(o2))).collect(Collectors.toList());

		/* if this is the first operation in the job, just look if we can allocate it earlier onto some of the machines in the WC */
		if (otherOpsOfJobStartingEarlier.isEmpty()) {
			int start = 0;
			int end = this.endTimes.get(o);
			for (Machine m : o.getWorkcenter().getMachines()) {
				if (this.getEarliestTimeWhenMachineIsFreeForDurationInInterval(m, start, end, o) != -1) {
					return true;
				}
			}
		}

		/* if there are other operation earlier, look whether we can get this operation squeezed somewhere in between */
		else {
			int endTimeOfLast = 0;
			for (Operation otherOp : otherOpsOfJobStartingEarlier) {
				int timeOfThis = this.endTimes.get(otherOp);

				/* now check if we have enough time inbetween the operations of the job to put the operation here */
				if (timeOfThis - endTimeOfLast > requiredTime) {

					/* if this is the case, check whether the machine is free at any time in that slot */
					int start = endTimeOfLast;
					int end = timeOfThis;
					for (Machine m : o.getWorkcenter().getMachines()) {
						if (this.getEarliestTimeWhenMachineIsFreeForDurationInInterval(m, start, end, o) != -1) {
							return true;
						}
					}
				}
				endTimeOfLast = this.endTimes.get(otherOp);
			}
		}
		return false;
	}

	public int getEarliestTimeWhenMachineIsFreeForDurationInInterval(final Machine m, final int start, final int end, final Operation op) {
		List<Operation> opsOnMachineDuringThatTime = this.getOperationsAssignedToMachine(m).stream()
				.filter(o -> this.setupStartTimes.get(o) > start && this.setupStartTimes.get(o) < end || this.endTimes.get(o) > start && this.endTimes.get(o) < end).collect(Collectors.toList());
		int currentOffset = Math.max(start, m.getAvailableDate());
		for (Operation o : opsOnMachineDuringThatTime) {
			int requiredSetupTimeIfInsertedHere = m.getWorkcenter().getSetupMatrix()[op.getStatus()][o.getStatus()];
			if (this.startTimes.get(o) - requiredSetupTimeIfInsertedHere - currentOffset >= o.getProcessTime()) {
				return currentOffset;
			}
			currentOffset = this.endTimes.get(o);
		}
		return -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.assignments == null) ? 0 : this.assignments.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Schedule other = (Schedule) obj;
		if (this.assignments == null) {
			if (other.assignments != null) {
				return false;
			}
		} else if (!this.assignments.equals(other.assignments)) {
			return false;
		}
		return true;
	}

	@Override
	public Machine getMachineToWhichOperationHasBeenAssigned(final Operation o) {
		for (Pair<Operation, Machine> assignment : this.assignments) {
			if (assignment.getX().equals(o)) {
				return assignment.getY();
			}
		}
		return null;
	}
}