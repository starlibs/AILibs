package ai.libs.jaicore.problems.scheduling.openshop;

import java.util.function.BiFunction;

public enum OpenShopMetric {
	TOTALFLOWTIME((p, s) -> {
		return (double)p.getJobs().values().stream().mapToInt(j -> s.getJobFlowTime(j)).reduce((r, c) -> r +  c).getAsInt();
	}),
	TOTALTARDINESS((p, s) -> {
		return (double)p.getJobs().values().stream().mapToInt(j -> s.getJobTardiness(j)).reduce((r, c) -> r +  c).getAsInt();
	}),
	TOTALFLOWTIME_WEIGHTED((p, s) -> {
		return (double)p.getJobs().values().stream().mapToInt(j -> j.getWeight() * s.getJobFlowTime(j)).reduce((r, c) -> r +  c).getAsInt();
	}),
	TOTALTARDINESS_WEIGHTED((p, s) -> {
		return (double)p.getJobs().values().stream().mapToInt(j -> j.getWeight() * s.getJobTardiness(j)).reduce((r, c) -> r +  c).getAsInt();
	}),
	MAKESPAN((p, s) -> { // gets the latest point of time when any operation finishes
		return (double) p.getOperations().values().stream().map(o -> s.getEndTimeOfOperation(o)).max((t1, t2) -> Double.compare(t1, t2)).get();
	}),
	MAXTARDINESS((p, s) -> {
		return (double)p.getJobs().values().stream().mapToInt(j -> s.getJobTardiness(j)).max().getAsInt();
	}),
	NUM_TARDY_JOB((p, s) -> {
		return (double)p.getJobs().values().stream().filter(j -> s.getJobTardiness(j) > 0).count();
	});

	private final BiFunction<OpenShopProblem, Schedule, Double> metricFunction;

	private OpenShopMetric(final BiFunction<OpenShopProblem, Schedule, Double> metricFunction) {
		this.metricFunction = metricFunction;
	}

	public double getScore(final OpenShopProblem problem, final Schedule schedule) {
		return this.metricFunction.apply(problem, schedule);
	}
}
