package ai.libs.jaicore.problems.scheduling.computers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.scheduling.IJobSchedulingInput;
import ai.libs.jaicore.problems.scheduling.IScheduleComputer;
import ai.libs.jaicore.problems.scheduling.Job;
import ai.libs.jaicore.problems.scheduling.Machine;
import ai.libs.jaicore.problems.scheduling.Operation;

public class DefaultSchedulingComputer implements IScheduleComputer {

	@Override
	public void fillTimes(final IJobSchedulingInput problemInput, final List<Pair<Operation, Machine>> assignments, final Map<Job, Integer> arrivalTimes, final Map<Operation, Integer> startTimes, final Map<Operation, Integer> endTimes, final Map<Operation, Integer> setupStartTimes, final Map<Operation, Integer> setupEndTimes) {

		startTimes.clear();
		endTimes.clear();
		setupStartTimes.clear();
		setupEndTimes.clear();

		/* set all arrival times to the ones set in the job definitions */
		problemInput.getJobs().forEach(j -> arrivalTimes.put(j, j.getReleaseDate()));

		/* compute stats for the operations */
		Map<Job, Integer> jobReadyness = new HashMap<>();
		Map<Machine, Integer> machineReadyness = new HashMap<>();
		Map<Machine, Integer> machineStates = new HashMap<>();
		for (Pair<Operation, Machine> p : assignments) {

			/* get times when job can arrive at the machine and time when machine can start to consider the job */
			Machine m = p.getY();
			Operation o = p.getX();
			int timeWhenMachineBecomesAvailableForOperation = machineReadyness.computeIfAbsent(m, Machine::getAvailableDate);
			int timeWhenJobArrivesAtMachine = jobReadyness.computeIfAbsent(o.getJob(), Job::getReleaseDate); // maybe add travel time here

			/* compute required setup time */
			int actualMachineState = machineStates.computeIfAbsent(m, Machine::getInitialState);
			int requiredMachineState = o.getStatus();
			int setupTime = (actualMachineState == requiredMachineState) ? 0 : m.getWorkcenter().getSetupMatrix()[actualMachineState][requiredMachineState];
			int timeWhenMachineIsReadyToProcessOperation = timeWhenMachineBecomesAvailableForOperation + setupTime;

			/* compute and memorize operation stats */
			int startTime = Math.max(timeWhenMachineIsReadyToProcessOperation, timeWhenJobArrivesAtMachine);
			int endTime = startTime + o.getProcessTime();
			setupStartTimes.put(o, timeWhenMachineBecomesAvailableForOperation);
			setupEndTimes.put(o, timeWhenMachineIsReadyToProcessOperation);
			startTimes.put(o, startTime);
			endTimes.put(o, endTime);

			/* update machine state and readyness and job readyness */
			machineReadyness.put(m, endTime);
			machineStates.put(m, o.getStatus());
			jobReadyness.put(o.getJob(), endTime);
		}
	}
}
