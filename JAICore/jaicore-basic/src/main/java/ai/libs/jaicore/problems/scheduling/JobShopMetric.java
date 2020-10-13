package ai.libs.jaicore.problems.scheduling;

import java.util.function.BiFunction;

public enum JobShopMetric {
	TOTALFLOWTIME((p, s) -> (double)p.getJobs().stream().mapToInt(s::getJobFlowTime).reduce((r, c) -> r +  c).getAsInt()),
	TOTALTARDINESS((p, s) -> (double)p.getJobs().stream().mapToInt(s::getJobTardiness).reduce((r, c) -> r +  c).getAsInt()),
	TOTALFLOWTIME_WEIGHTED((p, s) -> (double)p.getJobs().stream().mapToInt(j -> j.getWeight() * s.getJobFlowTime(j)).reduce((r, c) -> r +  c).getAsInt()),
	TOTALTARDINESS_WEIGHTED((p, s) -> (double)p.getJobs().stream().mapToInt(j -> j.getWeight() * s.getJobTardiness(j)).reduce((r, c) -> r +  c).getAsInt()),
	MAKESPAN((p, s) -> (double) p.getOperations().stream().map(s::getEndTimeOfOperation).max(Double::compare).get()), // gets the latest point of time when any operation finishes
	MAXTARDINESS((p, s) -> (double)p.getJobs().stream().mapToInt(s::getJobTardiness).max().getAsInt()),
	NUM_TARDY_JOB((p, s) -> (double)p.getJobs().stream().filter(j -> s.getJobTardiness(j) > 0).count());

	private final BiFunction<IJobSchedulingInput, ISchedule, Double> metricFunction;

	private JobShopMetric(final BiFunction<IJobSchedulingInput, ISchedule, Double> metricFunction) {
		this.metricFunction = metricFunction;
	}

	public double getScore(final JobSchedulingProblemInput problem, final ISchedule schedule) {
		if (problem.getJobs().isEmpty()) {
			throw new IllegalArgumentException("Cannot get score for a problem that has no jobs!");
		}
		return this.metricFunction.apply(problem, schedule);
	}
}
