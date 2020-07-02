package ai.libs.jaicore.problems.scheduling;

import java.util.List;
import java.util.Map;

import ai.libs.jaicore.basic.sets.Pair;

public interface IScheduleComputer {
	public void fillTimes(IJobSchedulingInput problemInput, List<Pair<Operation, Machine>> assignments, Map<Job, Integer> arrivalTimes, Map<Operation, Integer> startTimes, Map<Operation, Integer> endTimes, Map<Operation, Integer> setupStartTimes, Map<Operation, Integer> setupEndTimes);
}
