package ai.libs.jaicore.problems.scheduling.openshop;

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
public class Schedule {

	private final List<Pair<Operation, Machine>> assignments;
	private final Map<Machine, List<Operation>> assignmentPerMachine = new HashMap<>();
	private final Map<Operation, Integer> startTimes = new HashMap<>();
	private final Map<Operation, Integer> endTimes = new HashMap<>();
	private final Map<Operation, Integer> setupStartTimes = new HashMap<>();
	private final Map<Operation, Integer> setupEndTimes = new HashMap<>();

	Schedule(final List<Pair<Operation, Machine>> assignments) {
		super();
		this.assignments = assignments;
		this.assignments.forEach(p -> this.assignmentPerMachine.computeIfAbsent(p.getY(), m -> new ArrayList<>()).add(p.getX()));

		/* compute stats for the operations */
		Map<Job, Integer> jobReadyness = new HashMap<>();
		Map<Machine, Integer> machineReadyness = new HashMap<>();
		Map<Machine, Integer> machineStates= new HashMap<>();
		for (Pair<Operation, Machine> p : assignments) {

			/* get times when job can arrive at the machine and time when machine can start to consider the job */
			Machine m = p.getY();
			Operation o = p.getX();
			int timeWhenMachineBecomesAvailableForOperation = machineReadyness.computeIfAbsent(m, machine -> machine.getAvailableDate());
			int timeWhenJobArrivesAtMachine = jobReadyness.computeIfAbsent(o.getJob(), j -> j.getReleaseDate()); // maybe add travel time here

			/* compute required setup time */
			int actualMachineState = machineStates.computeIfAbsent(m, machine -> machine.getInitialState());
			int requiredMachineState = o.getStatus();
			int setupTime = (actualMachineState == requiredMachineState) ? 0 : m.getWorkcenter().getSetupMatrix()[actualMachineState][requiredMachineState];
			int timeWhenMachineIsReadyToProcessOperation = timeWhenMachineBecomesAvailableForOperation + setupTime;

			/* compute and memorize operation stats */
			int startTime = Math.max(timeWhenMachineIsReadyToProcessOperation, timeWhenJobArrivesAtMachine);
			int endTime = startTime + o.getProcessTime();
			this.setupStartTimes.put(o, timeWhenMachineBecomesAvailableForOperation);
			this.setupEndTimes.put(o, timeWhenMachineIsReadyToProcessOperation);
			this.startTimes.put(o, startTime);
			this.endTimes.put(o, endTime);

			/* update machine state and readyness and job readyness */
			machineReadyness.put(m, endTime);
			machineStates.put(m, o.getStatus());
			jobReadyness.put(o.getJob(), endTime);
		}
	}

	public List<Pair<Operation, Machine>> getAssignments() {
		return this.assignments;
	}

	public List<Operation> getOperationsAssignedToMachine(final Machine m) {
		return this.assignmentPerMachine.get(m);
	}

	public List<Operation> getOrderOfOperationsForJob(final Job job) {
		return this.assignments.stream().map(p -> p.getX()).filter(o -> o.getJob().equals(job)).collect(Collectors.toList());
	}

	public int getStartTimeOfOperation(final Operation o) {
		return this.startTimes.get(o);
	}

	public int getEndTimeOfOperation(final Operation o) {
		return this.endTimes.get(o);
	}

	public int getSetupStartTimeOfOperation(final Operation o) {
		return this.setupStartTimes.get(o);
	}

	public int getSetupEndTimeOfOperation(final Operation o) {
		return this.setupEndTimes.get(o);
	}

	public int getJobFinishTime(final Job job) {
		return job.getOperations().stream().map(o -> this.getEndTimeOfOperation(o)).max((t1,t2) -> Double.compare(t1, t2)).get();
	}

	/**
	 * @TODO This is from Gonzalo's implementation, but is this really correct? Probably we have to reduce the release time
	 *
	 * @param job
	 * @return
	 */
	public int getJobFlowTime(final Job job) {
		return this.getJobFinishTime(job) - job.getReleaseDate();
	}

	public int getJobTardiness(final Job job) {
		return Math.max(0, this.getJobFinishTime(job) - job.getDueDate());
	}

	public String getGanttAsString() {
		StringBuilder sb = new StringBuilder();
		List<Workcenter> workcenters = this.assignmentPerMachine.keySet().stream().map(m -> m.getWorkcenter()).collect(Collectors.toSet()).stream().sorted((w1,w2) -> w1.getWorkcenterID().compareTo(w2.getWorkcenterID())).collect(Collectors.toList());
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

}