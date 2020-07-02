package ai.libs.jaicore.problems.scheduling;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JobSchedulingProblemBuilder {

	private final Map<String, Workcenter> workcenters = new HashMap<>();
	private final Map<String, Job> jobs = new HashMap<>();
	private final Map<String, Machine> machines = new HashMap<>();
	private final Map<String, Operation> operations = new HashMap<>();
	private JobShopMetric metric;
	private int latestArrivalTime = -1;

	public JobSchedulingProblemBuilder fork() {
		JobSchedulingProblemBuilder copy = new JobSchedulingProblemBuilder();
		copy.workcenters.putAll(this.workcenters);
		copy.jobs.putAll(this.jobs);
		copy.machines.putAll(this.machines);
		copy.operations.putAll(this.operations);
		copy.metric = this.metric;
		return copy;
	}

	public JobSchedulingProblemBuilder withWorkcenter(final String workcenterID, final int[][] setupMatrix) {
		if (this.workcenters.containsKey(workcenterID)) {
			throw new IllegalArgumentException("Workcenter with id " + workcenterID + " already exists.");
		}
		this.workcenters.put(workcenterID, new Workcenter(workcenterID, setupMatrix));
		return this;
	}

	public JobSchedulingProblemBuilder singleStaged() {
		return this.withWorkcenter("W", null);
	}

	public JobSchedulingProblemBuilder withParallelMachines(final int k) {
		this.machines.clear();
		for (Entry<String, Workcenter> wcEntry : this.workcenters.entrySet()) {
			for (int i = 0; i < k; i++) {
				String machineName = wcEntry.getKey() + "_M" + i;
				this.withMachineForWorkcenter(machineName, wcEntry.getKey(), 0, 0);
			}
		}
		return this;
	}

	public JobSchedulingProblemBuilder withJob(final String jobID, final int releaseDate, final int dueDate, final int weight) {
		if (this.jobs.containsKey(jobID)) {
			throw new IllegalArgumentException("A job with ID " + jobID + " has already been defined.");
		}
		this.jobs.put(jobID, new Job(jobID, releaseDate, dueDate, weight));
		return this;
	}

	public JobSchedulingProblemBuilder withOperationForJob(final String operationId, final String jobId, final int processTime, final int status, final String wcId) {
		Workcenter wc = this.workcenters.get(wcId);
		Job job = this.jobs.get(jobId);
		if (wc == null) {
			throw new IllegalArgumentException("No workcenter with id " + wcId + " has been defined!");
		}
		if (job == null) {
			throw new IllegalArgumentException("No job with id " + jobId + " has been defined!");
		}
		if (this.operations.containsKey(operationId)) {
			throw new IllegalArgumentException("There is already an operation with name \"" + operationId + "\" defined (in job " + this.operations.get(operationId).getJob().getJobID() + ")");
		}
		this.operations.put(operationId, new Operation(operationId, processTime, status, job, wc)); // the constructor automatically adds the operation to the job
		return this;
	}

	public JobSchedulingProblemBuilder withMachineForWorkcenter(final String machineId, final String wcId, final int availability, final int initialState) {
		Workcenter wc = this.workcenters.get(wcId);
		if (wc == null) {
			throw new IllegalArgumentException("No workcenter with id " + wcId + " has been defined!");
		}
		if (this.machines.containsKey(machineId)) {
			throw new IllegalArgumentException("Machine with id " + machineId + " has already been defined (for work center " + this.machines.get(machineId).getWorkcenter().getWorkcenterID() + ")");
		}
		this.machines.put(machineId, new Machine(machineId, availability, initialState, wc)); // the constructor automatically adds the machine to the work center
		return this;
	}

	public JobSchedulingProblemBuilder withMetric(final JobShopMetric metric) {
		this.metric = metric;
		return this;
	}

	public JobSchedulingProblemBuilder withLatestArrivalTime(final int latestArrivalTime) {
		this.latestArrivalTime = latestArrivalTime;
		return this;
	}

	public IJobSchedulingInput build() {
		if (this.metric == null) {
			throw new IllegalStateException("No metric for schedule evaluation has been defined.");
		}
		return new JobSchedulingProblemInput(new HashMap<>(this.jobs), new HashMap<>(this.workcenters), new HashMap<>(this.operations), new HashMap<>(this.machines), this.metric, this.latestArrivalTime);
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
}