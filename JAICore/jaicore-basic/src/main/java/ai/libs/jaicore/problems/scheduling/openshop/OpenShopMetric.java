package ai.libs.jaicore.problems.scheduling.openshop;

import java.util.function.BiFunction;

public enum OpenShopMetric {
	TOTALFLOWTIME((p, s) -> (double)p.getJobs().values().stream().mapToInt(s::getJobFlowTime).reduce((r, c) -> r +  c).getAsInt()),
	TOTALTARDINESS((p, s) -> (double)p.getJobs().values().stream().mapToInt(s::getJobTardiness).reduce((r, c) -> r +  c).getAsInt()),
	TOTALFLOWTIME_WEIGHTED((p, s) -> (double)p.getJobs().values().stream().mapToInt(j -> j.getWeight() * s.getJobFlowTime(j)).reduce((r, c) -> r +  c).getAsInt()),
	TOTALTARDINESS_WEIGHTED((p, s) -> (double)p.getJobs().values().stream().mapToInt(j -> j.getWeight() * s.getJobTardiness(j)).reduce((r, c) -> r +  c).getAsInt()),
	MAKESPAN((p, s) -> (double) p.getOperations().values().stream().map(s::getEndTimeOfOperation).max(Double::compare).get()), // gets the latest point of time when any operation finishes
	MAXTARDINESS((p, s) -> (double)p.getJobs().values().stream().mapToInt(s::getJobTardiness).max().getAsInt()),
	NUM_TARDY_JOB((p, s) -> (double)p.getJobs().values().stream().filter(j -> s.getJobTardiness(j) > 0).count());

	private final BiFunction<OpenShopProblem, Schedule, Double> metricFunction;

	private OpenShopMetric(final BiFunction<OpenShopProblem, Schedule, Double> metricFunction) {
		this.metricFunction = metricFunction;
	}

	public double getScore(final OpenShopProblem problem, final Schedule schedule) {
		return this.metricFunction.apply(problem, schedule);
	}
}
