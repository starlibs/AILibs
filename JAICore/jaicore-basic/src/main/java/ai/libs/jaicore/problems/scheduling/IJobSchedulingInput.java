package ai.libs.jaicore.problems.scheduling;

import java.util.Collection;

public interface IJobSchedulingInput {

	public Collection<Job> getJobs();

	public Job getJob(String jobId);

	public Collection<Operation> getOperations();

	public Operation getOperation(String opId);

	public Collection<Workcenter> getWorkcenters();

	public Workcenter getWorkcenter(String wcId);

	public Collection<Machine> getMachines();

	public Machine getMachine(String mId);

	public JobShopMetric getMetric();

	public double getScoreOfSchedule(final ISchedule s);
}
