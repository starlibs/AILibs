package ai.libs.jaicore.testproblems.openshop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.problems.scheduling.openshop.Job;
import ai.libs.jaicore.problems.scheduling.openshop.Machine;
import ai.libs.jaicore.problems.scheduling.openshop.OpenShopMetric;
import ai.libs.jaicore.problems.scheduling.openshop.OpenShopProblem;
import ai.libs.jaicore.problems.scheduling.openshop.OpenShopProblemBuilder;
import ai.libs.jaicore.problems.scheduling.openshop.Operation;
import ai.libs.jaicore.problems.scheduling.openshop.Schedule;
import ai.libs.jaicore.problems.scheduling.openshop.ScheduleBuilder;

public class OpenShopTest {
	private static final String W_1 = "W1";
	private static final String W_2 = "W2";
	private static final String W_3 = "W3";

	private static final String M_1 = "M1";
	private static final String M_2 = "M2";
	private static final String M_3 = "M3";
	private static final String M_4 = "M4";
	private static final String M_5 = "M5";

	private static final String J_1 = "J1";
	private static final String J_2 = "J2";
	private static final String J_3 = "J3";

	private static final String O_1 = "O1";
	private static final String O_2 = "O2";
	private static final String O_3 = "O3";
	private static final String O_4 = "O4";
	private static final String O_5 = "O5";
	private static final String O_6 = "O6";
	private static final String O_7 = "O7";

	private static OpenShopProblemBuilder problemBuilder;
	private static Schedule solution;

	/* define constant ground truths for the drawn solution */
	private static int flowTimeOfJob1 = 19;
	private static int flowTimeOfJob2 = 28;
	private static int flowTimeOfJob3 = 28;
	private static int tardinessOfJob1 = 9;
	private static int tardinessOfJob2 = 20;
	private static int tardinessOfJob3 = 16;

	@BeforeClass
	public static void createStandardProblemBuilder() {

		OpenShopProblemBuilder builder = new OpenShopProblemBuilder();

		/* setup work centers */
		final int[][] setupMatrix1 = new int[][] {{0, 2, 1}, {1, 0, 2}, {2, 1, 0}};
		final int[][] setupMatrix2 = new int[][] {{0, 2, 1}, {1, 0, 2}, {2, 1, 0}};
		final int[][] setupMatrix3 = new int[][] {{0, 2, 1}, {1, 0, 2}, {2, 1, 0}};
		builder.withWorkcenter(W_1, setupMatrix1).withMachineForWorkcenter(M_1, W_1, 2, 1).withMachineForWorkcenter(M_2, W_1, 2, 0);
		builder.withWorkcenter(W_2, setupMatrix2).withMachineForWorkcenter(M_3, W_2, 8, 0);
		builder.withWorkcenter(W_3, setupMatrix3).withMachineForWorkcenter(M_4, W_3, 2, 1).withMachineForWorkcenter(M_5, W_3, 6, 1);

		/* setup jobs */
		builder.withJob(J_1, 0, 10, 1).withOperationForJob(O_1, J_1, 5, 0, W_1).withOperationForJob(O_2, J_1, 5, 1, W_2).withOperationForJob(O_3, J_1, 4, 0, W_3);
		builder.withJob(J_2, 0, 8, 2).withOperationForJob(O_4, J_2, 6, 1, W_2).withOperationForJob(O_5, J_2, 7, 0, W_1);
		builder.withJob(J_3, 0, 12, 3).withOperationForJob(O_6, J_3, 2, 2, W_3).withOperationForJob(O_7, J_3, 6, 1, W_3);

		/* store this builder */
		problemBuilder = builder;

		/* create random solution for this problem */
		Random random = new Random(0);
		ScheduleBuilder sb = new ScheduleBuilder(builder);
		for (Operation o : builder.getOperations().values()) {
			List<Machine> options = o.getWorkcenter().getMachines();
			sb.assign(o, options.get(random.nextInt(options.size())));
		}
		solution = sb.build();
	}

	@Test
	public void testProblemBuildship() {
		OpenShopProblem problem = problemBuilder.fork().withMetric(OpenShopMetric.TOTALFLOWTIME).build();
		System.out.println(solution.getAsString());
		assertEquals(7, problem.getOperations().size());
		assertEquals(5, problem.getMachines().size());
		assertEquals(3, problem.getJobs().size());
		assertEquals(3, problem.getWorkcenters().size());
	}

	private OpenShopProblem getSingleOperationProblem(final int availability, final int setupTime, final int processTime, final boolean correctState) {
		OpenShopProblemBuilder pb = new OpenShopProblemBuilder();
		pb.withWorkcenter(W_1, new int[][] {{0, setupTime}, {setupTime, 0}}).withMachineForWorkcenter(M_1, W_1, availability, 0);
		pb.withJob(J_1, 0, 10, 1).withOperationForJob(O_1, J_1, processTime, correctState ? 0 : 1, W_1);
		pb.withMetric(OpenShopMetric.MAKESPAN);
		return pb.build();
	}

	private OpenShopProblem getTwoOperationProblem(final int availability, final int setupTime, final int processTime1, final int processTime2, final boolean correctState) {
		OpenShopProblemBuilder pb = new OpenShopProblemBuilder();
		pb.withWorkcenter(W_1, new int[][] {{0, setupTime}, {setupTime, 0}}).withMachineForWorkcenter(M_1, W_1, availability, 0);
		pb.withJob(J_1, 0, 10, 1).withOperationForJob(O_1, J_1, processTime1, 0, W_1).withOperationForJob(O_2, J_1, processTime2, correctState ? 0 : 1, W_1);
		pb.withMetric(OpenShopMetric.MAKESPAN);
		return pb.build();
	}

	@Test
	public void testNonActiveScheduleDetection() {

		/* create problem with two work centers (one machine each) and two jobs (with two operations each) */
		OpenShopProblemBuilder pb = new OpenShopProblemBuilder();
		pb.withWorkcenter(W_1, new int[][] {{0}}).withMachineForWorkcenter(M_1, W_1, 0, 0);
		pb.withWorkcenter(W_2, new int[][] {{0}}).withMachineForWorkcenter(M_2, W_2, 0, 0);
		int processTime = 1;
		pb.withJob(J_1, 0, 10, 1).withOperationForJob(O_1, J_1, processTime, 0, W_1).withOperationForJob(O_2, J_1, processTime, 0, W_2);
		pb.withJob(J_2, 0, 10, 1).withOperationForJob(O_3, J_2, processTime, 0, W_1).withOperationForJob(O_4, J_2, processTime, 0, W_2);
		pb.withMetric(OpenShopMetric.MAKESPAN);

		/* now create a stupid (non-active) allocation */
		Schedule s = new ScheduleBuilder(pb.build()).assign(O_1, M_1).assign(O_2, M_2).assign(O_4, M_2).assign(O_3, M_1).build();
		System.out.println(s.getGanttAsString());
		assertFalse(s.isActive());
	}

	@Test
	public void testThatTheTimesOfTheOrderedOperationsAreConsistentAndDoNotOverlap() {
		Set<Job> jobs = solution.getAssignments().stream().map(p -> p.getX().getJob()).collect(Collectors.toSet());
		for (Job job : jobs) {
			List<Operation> ops = solution.getOrderOfOperationsForJob(job);
			int earliestPossibleTime = job.getReleaseDate();
			for (Operation op : ops) {
				assertTrue (solution.getStartTimeOfOperation(op) >= earliestPossibleTime);
				assertEquals (solution.getStartTimeOfOperation(op) + op.getProcessTime(), solution.getEndTimeOfOperation(op));
				earliestPossibleTime = solution.getEndTimeOfOperation(op);
			}
		}
	}

	@Test
	public void testProperComputationOfTimesForProperInitializationIfMachineIsDirectlyAvailable() {
		int processTime = 2;
		int setupTime = -1;
		OpenShopProblem problem = this.getSingleOperationProblem(0, setupTime, processTime, true);
		Operation o = problem.getOperation(O_1);
		Schedule schedule = new ScheduleBuilder(problem).assign(o.getName(), M_1).build();
		assertEquals(0, schedule.getSetupStartTimeOfOperation(o));
		assertEquals(0, schedule.getSetupEndTimeOfOperation(o));
		assertEquals(0, schedule.getStartTimeOfOperation(o));
		assertEquals(processTime, schedule.getEndTimeOfOperation(o));
	}

	@Test
	public void testProperComputationOfTimesForInproperInitializationIfMachineIsDirectlyAvailable() {
		int processTime = 2;
		int setupTime = 3;
		OpenShopProblem problem = this.getSingleOperationProblem(0, setupTime, processTime, false);
		Operation o = problem.getOperation(O_1);
		Schedule schedule = new ScheduleBuilder(problem).assign(o.getName(), M_1).build();
		assertEquals(0, schedule.getSetupStartTimeOfOperation(o));
		assertEquals(setupTime, schedule.getSetupEndTimeOfOperation(o));
		assertEquals(setupTime, schedule.getStartTimeOfOperation(o));
		assertEquals(setupTime + processTime, schedule.getEndTimeOfOperation(o));
	}

	@Test
	public void testProperComputationOfTimesForProperPreviousStateDueToOtherOperationIfMachineIsDirectlyAvailable() {
		int processTime1 = 2;
		int processTime2 = 5;
		int setupTime = 3;
		OpenShopProblem problem = this.getTwoOperationProblem(0, setupTime, processTime1, processTime2, true);
		Schedule schedule = new ScheduleBuilder(problem).assign(O_1, M_1).assign(O_2, M_1).build();
		Operation o = problem.getOperation(O_2);
		int finishTimeOfFirstJob = schedule.getEndTimeOfOperation(problem.getOperation(O_1));
		assertEquals(processTime1, finishTimeOfFirstJob);
		assertEquals(finishTimeOfFirstJob, schedule.getSetupStartTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob, schedule.getSetupEndTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob, schedule.getStartTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob + processTime2, schedule.getEndTimeOfOperation(o));
	}

	@Test
	public void testProperComputationOfTimesForInproperPreviousStateDueToOtherOperationIfMachineIsDirectlyAvailable() {
		int processTime1 = 2;
		int processTime2 = 5;
		int setupTime = 3;
		OpenShopProblem problem = this.getTwoOperationProblem(0, setupTime, processTime1, processTime2, false);
		Schedule schedule = new ScheduleBuilder(problem).assign(O_1, M_1).assign(O_2, M_1).build();
		Operation o = problem.getOperation(O_2);
		int finishTimeOfFirstJob = schedule.getEndTimeOfOperation(problem.getOperation(O_1));
		assertEquals(processTime1, finishTimeOfFirstJob);
		assertEquals(finishTimeOfFirstJob, schedule.getSetupStartTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob + setupTime, schedule.getSetupEndTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob + setupTime, schedule.getStartTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob + setupTime + processTime2, schedule.getEndTimeOfOperation(o));
	}

	@Test
	public void testProperComputationOfTimesForProperInitializationIfMachineGetsAvailableAfterJobRelease() {
		int processTime = 2;
		int setupTime = -1;
		int availability = 5;
		OpenShopProblem problem = this.getSingleOperationProblem(availability, setupTime, processTime, true);
		Operation o = problem.getOperation(O_1);
		Schedule schedule = new ScheduleBuilder(problem).assign(o.getName(), M_1).build();
		assertEquals(availability, schedule.getSetupStartTimeOfOperation(o));
		assertEquals(availability, schedule.getSetupEndTimeOfOperation(o));
		assertEquals(availability, schedule.getStartTimeOfOperation(o));
		assertEquals(availability + processTime, schedule.getEndTimeOfOperation(o));
	}

	@Test
	public void testProperComputationOfTimesForInproperInitializationIfMachineGetsAvailableAfterJobRelease() {
		int processTime = 2;
		int setupTime = 3;
		int availability = 5;
		OpenShopProblem problem = this.getSingleOperationProblem(availability, setupTime, processTime, false);
		Operation o = problem.getOperation(O_1);
		Schedule schedule = new ScheduleBuilder(problem).assign(o.getName(), M_1).build();
		assertEquals(availability, schedule.getSetupStartTimeOfOperation(o));
		assertEquals(availability + setupTime, schedule.getSetupEndTimeOfOperation(o));
		assertEquals(availability + setupTime, schedule.getStartTimeOfOperation(o));
		assertEquals(availability + setupTime + processTime, schedule.getEndTimeOfOperation(o));
	}

	@Test
	public void testProperComputationOfTimesForProperPreviousStateDueToOtherOperationIfMachineGetsAvailableAfterJobRelease() {
		int processTime1 = 2;
		int processTime2 = 5;
		int setupTime = 3;
		int availability = 5;
		OpenShopProblem problem = this.getTwoOperationProblem(availability, setupTime, processTime1, processTime2, true);
		Schedule schedule = new ScheduleBuilder(problem).assign(O_1, M_1).assign(O_2, M_1).build();
		Operation o = problem.getOperation(O_2);
		int finishTimeOfFirstJob = schedule.getEndTimeOfOperation(problem.getOperation(O_1));
		assertEquals(availability + processTime1, finishTimeOfFirstJob);
		assertEquals(finishTimeOfFirstJob, schedule.getSetupStartTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob, schedule.getSetupEndTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob, schedule.getStartTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob + processTime2, schedule.getEndTimeOfOperation(o));
	}

	@Test
	public void testProperComputationOfTimesForInproperPreviousStateDueToOtherOperationIfMachineGetsAvailableAfterJobRelease() {
		int processTime1 = 2;
		int processTime2 = 5;
		int setupTime = 3;
		int availability = 5;
		OpenShopProblem problem = this.getTwoOperationProblem(availability, setupTime, processTime1, processTime2, false);
		Schedule schedule = new ScheduleBuilder(problem).assign(O_1, M_1).assign(O_2, M_1).build();
		Operation o = problem.getOperation(O_2);
		int finishTimeOfFirstJob = schedule.getEndTimeOfOperation(problem.getOperation(O_1));
		assertEquals(availability + processTime1, finishTimeOfFirstJob);
		assertEquals(finishTimeOfFirstJob, schedule.getSetupStartTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob + setupTime, schedule.getSetupEndTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob + setupTime, schedule.getStartTimeOfOperation(o));
		assertEquals(finishTimeOfFirstJob + setupTime + processTime2, schedule.getEndTimeOfOperation(o));
	}

	@Test
	public void testTotalFlowTimeMetric() {
		OpenShopProblem problem = problemBuilder.fork().withMetric(OpenShopMetric.TOTALFLOWTIME).build();
		double score = problem.getScoreOfSchedule(solution);
		assertEquals(flowTimeOfJob1 + flowTimeOfJob2 + flowTimeOfJob3, score, 0.01);
	}

	@Test
	public void testTotalWeightedFlowTimeMetric() {
		OpenShopProblem problem = problemBuilder.fork().withMetric(OpenShopMetric.TOTALFLOWTIME_WEIGHTED).build();
		double score = problem.getScoreOfSchedule(solution);
		assertEquals(flowTimeOfJob1 + 2 * flowTimeOfJob2 + 3 * flowTimeOfJob3, score, 0.01);
	}

	@Test
	public void testTotalTardinessMetric() {
		OpenShopProblem problem = problemBuilder.fork().withMetric(OpenShopMetric.TOTALTARDINESS).build();
		double score = problem.getScoreOfSchedule(solution);
		assertEquals(tardinessOfJob1 + tardinessOfJob2 + tardinessOfJob3, score, 0.01);
	}

	@Test
	public void testTotalWeightedTardinessMetric() {
		OpenShopProblem problem = problemBuilder.fork().withMetric(OpenShopMetric.TOTALTARDINESS_WEIGHTED).build();
		double score = problem.getScoreOfSchedule(solution);
		assertEquals(tardinessOfJob1 + 2 * tardinessOfJob2 + 3 * tardinessOfJob3, score, 0.01);
	}

	@Test
	public void testMaxTardinessMetric() {
		OpenShopProblem problem = problemBuilder.fork().withMetric(OpenShopMetric.MAXTARDINESS).build();
		double score = problem.getScoreOfSchedule(solution);
		assertEquals(Math.max(tardinessOfJob1, Math.max(tardinessOfJob2, tardinessOfJob3)), score, 0.01);
	}

	@Test
	public void testNumTardyJobsMetric() {
		OpenShopProblem problem = problemBuilder.fork().withMetric(OpenShopMetric.NUM_TARDY_JOB).build();
		double score = problem.getScoreOfSchedule(solution);
		assertEquals((tardinessOfJob1 > 0 ? 1 : 0) + (tardinessOfJob2 > 0 ? 1 : 0) + (tardinessOfJob3 > 0 ? 1 : 0), score, 0.01);
	}

	@Test
	public void testMakeSpanMetric() {
		OpenShopProblem problem = problemBuilder.fork().withMetric(OpenShopMetric.MAKESPAN).build();
		double score = problem.getScoreOfSchedule(solution);
		assertEquals(28, score, 0.01);
	}
}
