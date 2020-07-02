package ai.libs.jaicore.problems.scheduling.computers;

import java.util.Map;

import ai.libs.jaicore.problems.scheduling.ASchedulingComputer;
import ai.libs.jaicore.problems.scheduling.Job;
import ai.libs.jaicore.problems.scheduling.Machine;
import ai.libs.jaicore.problems.scheduling.Operation;

public class DefaultSchedulingComputer extends ASchedulingComputer {

	@Override
	public int getTimeWhenMachineBecomesAvailableForOperation(final Map<Job, Integer> arrivalTimes, final Map<Machine, Integer> machineReadiness, final Machine m) {
		return machineReadiness.computeIfAbsent(m, Machine::getAvailableDate);
	}

	@Override
	public int timeWhenOperationArrivesAtMachine(final Map<Job, Integer> arrivalTimes, final Map<Machine, Integer> machineReadiness, final Map<Job, Integer> jobReadyness, final Operation o, final Machine m) {
		return jobReadyness.computeIfAbsent(o.getJob(), Job::getReleaseDate); // maybe add travel time here
	}
}
