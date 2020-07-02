package ai.libs.jaicore.problems.scheduling;

import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;

public interface ISchedule {
	public List<Pair<Operation, Machine>> getAssignments();

	public List<Operation> getOperationsAssignedToMachine(final Machine m);

	public Machine getMachineToWhichOperationHasBeenAssigned(final Operation o);

	public List<Operation> getOrderOfOperationsForJob(final Job job);

	public int getStartTimeOfOperation(final Operation o);

	public int getEndTimeOfOperation(final Operation o);

	public int getSetupStartTimeOfOperation(final Operation o);

	public int getSetupEndTimeOfOperation(final Operation o);

	public int getJobFinishTime(final Job job);

	public int getJobFlowTime(final Job job);

	public int getJobTardiness(final Job job);
}
