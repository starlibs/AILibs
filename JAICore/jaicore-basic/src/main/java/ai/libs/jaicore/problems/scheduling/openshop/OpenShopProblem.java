package ai.libs.jaicore.problems.scheduling.openshop;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Gonzalo Mejia
 * @author Felix Mohr
 *
 * @version 4.0
 */

public class OpenShopProblem {

	private final Map<String, Job> jobs;
	private final Map<String, Workcenter> workcenters;
	private final Map<String, Operation> operations;
	private final Map<String, Machine> machines;
	private final OpenShopMetric metric;

	public OpenShopProblem(final Map<String, Job> jobs, final Map<String, Workcenter> workcenters, final Map<String, Operation> operations, final Map<String, Machine> machines, final OpenShopMetric metric) {
		super();
		this.jobs = jobs;
		this.workcenters = workcenters;
		this.operations = operations;
		this.machines = machines;
		this.metric = metric;
	}

	public Map<String, Job> getJobs() {
		return Collections.unmodifiableMap(this.jobs);
	}

	public Map<String, Workcenter> getWorkcenters() {
		return Collections.unmodifiableMap(this.workcenters);
	}

	public Map<String, Operation> getOperations() {
		return Collections.unmodifiableMap(this.operations);
	}

	public Map<String, Machine> getMachines() {
		return Collections.unmodifiableMap(this.machines);
	}

	public OpenShopMetric getMetric() {
		return this.metric;
	}


	public Workcenter getWorkcenter(final String workcenterId) {
		return this.workcenters.get(workcenterId);
	}

	public Machine getMachine(final String machineId) {
		return this.machines.get(machineId);
	}

	public Operation getOperation(final String operationId) {
		return this.operations.get(operationId);
	}

	public Job getJob(final String jobId) {
		return this.jobs.get(jobId);
	}

	public double getScoreOfSchedule(final Schedule s) {
		return this.metric.getScore(this, s);
	}

	public void printWorkcenters(final OutputStream out) throws IOException {
		StringBuilder sb = new StringBuilder();
		int n = this.workcenters.size();
		sb.append("Number of work centers \t" + n);
		sb.append("\n\n");
		for (Workcenter w : this.workcenters.values().stream().sorted((w1,w2) -> w1.getWorkcenterID().compareTo(w2.getWorkcenterID())).collect(Collectors.toList())) {
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
		for (Job j : this.jobs.values().stream().sorted((j1,j2) -> j1.getJobID().compareTo(j2.getJobID())).collect(Collectors.toList())) {
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