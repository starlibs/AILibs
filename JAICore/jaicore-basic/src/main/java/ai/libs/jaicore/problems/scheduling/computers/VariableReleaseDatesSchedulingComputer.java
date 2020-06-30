package ai.libs.jaicore.problems.scheduling.computers;

import java.util.List;
import java.util.Map;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.scheduling.IJobSchedulingInput;
import ai.libs.jaicore.problems.scheduling.Job;
import ai.libs.jaicore.problems.scheduling.JobSchedulingProblemInput;
import ai.libs.jaicore.problems.scheduling.Machine;
import ai.libs.jaicore.problems.scheduling.Operation;

/**
 * The unique aspect of this computer is that Job release dates are ignored. Instead, release dates are automatically derived from the assignments and the latest arrival date: The release date is set to the minimum of the processing time
 * and the latest allowed arrival time.
 *
 * @author Felix Mohr
 *
 */
public class VariableReleaseDatesSchedulingComputer extends DefaultSchedulingComputer {

	private int latestArrivalTime;

	@Override
	public void fillTimes(final IJobSchedulingInput problemInput, final List<Pair<Operation, Machine>> assignments, final Map<Job, Integer> arrivalTimes, final Map<Operation, Integer> startTimes, final Map<Operation, Integer> endTimes, final Map<Operation, Integer> setupStartTimes,
			final Map<Operation, Integer> setupEndTimes) {

		if (!(problemInput instanceof JobSchedulingProblemInput)) {
			throw new IllegalArgumentException();
		}
		JobSchedulingProblemInput cProblemInput = (JobSchedulingProblemInput)problemInput;
		this.latestArrivalTime = cProblemInput.getLatestArrivalTime();
		super.fillTimes(cProblemInput, assignments, arrivalTimes, startTimes, endTimes, setupStartTimes, setupEndTimes);
	}

	@Override
	public int timeWhenOperationArrivesAtMachine(final Map<Job, Integer> arrivalTimes, final Map<Machine, Integer> machineReadiness, final Map<Job, Integer> jobReadyness, final Operation o, final Machine m) {
		Job job = o.getJob();
		int timeWhenMachineBecomesAvailableForOperation = super.getTimeWhenMachineBecomesAvailableForOperation(arrivalTimes, machineReadiness, m);
		int timeWhenJobArrivesAtMachine;
		if (!arrivalTimes.containsKey(job)) { // this is the first operation of assigned for this job (then use it as the arrival time for the job)
			timeWhenJobArrivesAtMachine = Math.min(timeWhenMachineBecomesAvailableForOperation, this.latestArrivalTime);
			arrivalTimes.put(job, timeWhenJobArrivesAtMachine);
		}
		else {
			timeWhenJobArrivesAtMachine = Math.max(timeWhenMachineBecomesAvailableForOperation, arrivalTimes.get(job));
		}
		return timeWhenJobArrivesAtMachine;
	}
}