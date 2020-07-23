package ai.libs.jaicore.problems.scheduling.computers;

import java.util.List;
import java.util.Map;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.scheduling.ASchedulingComputer;
import ai.libs.jaicore.problems.scheduling.IJobSchedulingInput;
import ai.libs.jaicore.problems.scheduling.Job;
import ai.libs.jaicore.problems.scheduling.JobSchedulingProblemInput;
import ai.libs.jaicore.problems.scheduling.Machine;
import ai.libs.jaicore.problems.scheduling.Operation;

public class DefaultSchedulingComputer extends ASchedulingComputer {

	@Override
	public void fillTimes(final IJobSchedulingInput problemInput, final List<Pair<Operation, Machine>> assignments, final Map<Job, Integer> arrivalTimes, final Map<Operation, Integer> startTimes, final Map<Operation, Integer> endTimes, final Map<Operation, Integer> setupStartTimes,
			final Map<Operation, Integer> setupEndTimes) {

		if (!(problemInput instanceof JobSchedulingProblemInput)) {
			throw new IllegalArgumentException();
		}
		JobSchedulingProblemInput cProblemInput = (JobSchedulingProblemInput)problemInput;

		/* set all arrival times to the ones set in the job definitions */
		problemInput.getJobs().forEach(j -> arrivalTimes.put(j, j.getReleaseDate()));
		super.fillTimes(cProblemInput, assignments, arrivalTimes, startTimes, endTimes, setupStartTimes, setupEndTimes);
	}

	@Override
	public int getTimeWhenMachineBecomesAvailableForOperation(final Map<Job, Integer> arrivalTimes, final Map<Machine, Integer> machineReadiness, final Machine m) {
		return machineReadiness.computeIfAbsent(m, Machine::getAvailableDate);
	}

	@Override
	public int timeWhenOperationArrivesAtMachine(final Map<Job, Integer> arrivalTimes, final Map<Machine, Integer> machineReadiness, final Map<Job, Integer> jobReadyness, final Operation o, final Machine m) {
		return jobReadyness.computeIfAbsent(o.getJob(), Job::getReleaseDate); // maybe add travel time here
	}
}
