package ai.libs.jaicore.problems.scheduling;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Gonzalo Mejia
 * @author Felix Mohr
 *
 * @version 4.0
 */

public class JobSchedulingProblemInput implements IJobSchedulingInput {

	private final Map<String, Job> jobs;
	private final Map<String, Workcenter> workcenters;
	private final Map<String, Operation> operations;
	private final Map<String, Machine> machines;
	private final JobShopMetric metric;
	private final int latestArrivalTime; // this is for problems in which the arrival time is a decision variable. Then we typically have an upper bound on the arrival time

	public JobSchedulingProblemInput(final Collection<Job> jobs, final Collection<Workcenter> workcenters, final Collection<Operation> operations, final Collection<Machine> machines, final JobShopMetric metric,
			final int latestArrivalTime) {
		this.jobs = new HashMap<>();
		jobs.forEach(j -> this.jobs.put(j.getJobID(), j));
		this.workcenters = new HashMap<>();
		workcenters.forEach(j -> this.workcenters.put(j.getWorkcenterID(), j));
		this.operations = new HashMap<>();
		operations.forEach(j -> this.operations.put(j.getName(), j));
		this.machines = new HashMap<>();
		machines.forEach(j -> this.machines.put(j.getMachineID(), j));
		this.metric = metric;
		this.latestArrivalTime = latestArrivalTime;
	}

	public JobSchedulingProblemInput(final Map<String, Job> jobs, final Map<String, Workcenter> workcenters, final Map<String, Operation> operations, final Map<String, Machine> machines, final JobShopMetric metric,
			final int latestArrivalTime) {
		super();
		this.jobs = jobs;
		this.workcenters = workcenters;
		this.operations = operations;
		this.machines = machines;
		this.metric = metric;
		this.latestArrivalTime = latestArrivalTime;
	}

	@Override
	public Collection<Job> getJobs() {
		return this.jobs.values();
	}

	@Override
	public Collection<Workcenter> getWorkcenters() {
		return this.workcenters.values();
	}

	@Override
	public Collection<Operation> getOperations() {
		return this.operations.values();
	}

	@Override
	public Collection<Machine> getMachines() {
		return this.machines.values();
	}

	@Override
	public JobShopMetric getMetric() {
		return this.metric;
	}

	public int getLatestArrivalTime() {
		return this.latestArrivalTime;
	}

	@Override
	public Workcenter getWorkcenter(final String workcenterId) {
		return this.workcenters.get(workcenterId);
	}

	@Override
	public Machine getMachine(final String machineId) {
		return this.machines.get(machineId);
	}

	@Override
	public Operation getOperation(final String operationId) {
		return this.operations.get(operationId);
	}

	@Override
	public Job getJob(final String jobId) {
		return this.jobs.get(jobId);
	}

	@Override
	public double getScoreOfSchedule(final ISchedule s) {
		Objects.requireNonNull(s);
		return this.metric.getScore(this, s);
	}

	public int getTotalProcessingTime() {
		return this.operations.values().stream().map(Operation::getProcessTime).reduce((a, b) -> a + b).get();
	}

	public void printWorkcenters(final OutputStream out) throws IOException {
		StringBuilder sb = new StringBuilder();
		int n = this.workcenters.size();
		sb.append("Number of work centers \t" + n);
		sb.append("\n\n");
		for (Workcenter w : this.workcenters.values().stream().sorted((w1, w2) -> w1.getWorkcenterID().compareTo(w2.getWorkcenterID())).collect(Collectors.toList())) {
			sb.append(w.getWorkcenterID());
			sb.append(" (" + w.getMachines().size() + " machines)");
			sb.append("\n");
			for (Machine m : w.getMachines()) {
				sb.append("Machine: \t" + m.getMachineID());
				sb.append("\tAvailability: " + m.getAvailableDate() + "\t Init state: ");
				sb.append(m.getInitialState());
				sb.append("\n");
			}
			sb.append("\n");
		}
		out.write(sb.toString().getBytes());
	}

	/**
	 * printJobs writes the job information to an output writer
	 *
	 * @param out
	 *            BufferedWriter
	 * @throws IOException
	 */
	public void printJobs(final OutputStream out) throws IOException {
		StringBuilder sb = new StringBuilder();
		int n = this.jobs.size();
		sb.append("Number of Jobs \t" + n);
		sb.append("\n\n");
		for (Job j : this.jobs.values().stream().sorted((j1, j2) -> j1.getJobID().compareTo(j2.getJobID())).collect(Collectors.toList())) {
			sb.append(j.getJobID());
			sb.append("\n");
			sb.append("Release Date \t" + j.getReleaseDate());
			sb.append("\n");
			sb.append("Due Date \t" + j.getDueDate());
			sb.append("\n");
			sb.append("Weight \t \t" + j.getWeight());
			sb.append("\n");
			for (Operation op : j.getOperations()) {
				sb.append("Operation: \t" + op.getName());
				sb.append("\tWC: " + op.getWorkcenter().getWorkcenterID());
				sb.append("\tProcess time: " + op.getProcessTime() + "\t Status: ");
				sb.append(op.getStatus());
				sb.append("\n");
			}
			sb.append("\n");
		}
		out.write(sb.toString().getBytes());

	}
}