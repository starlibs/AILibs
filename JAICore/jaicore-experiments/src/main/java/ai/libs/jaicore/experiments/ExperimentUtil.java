package ai.libs.jaicore.experiments;

import java.util.ArrayList;
import java.util.List;

public class ExperimentUtil {

	public static String getProgressQuery(final String tablename) {
		return getProgressQuery(tablename, 1);
	}

	public static String getProgressQuery(final String tablename, final int numberOfParallelJobs) {

		/* create sub-queries */
		List<String> subQueries = new ArrayList<>();
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as \"open\" FROM `jobscheduling` WHERE time_started is null");
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as \"running\" FROM `jobscheduling` WHERE time_started is not null and time_end is null");
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as finished, AVG(TIMESTAMPDIFF(SECOND, time_started, time_end)) as avgRuntimeFinished  FROM `jobscheduling` WHERE time_started is not null and time_end is not null");
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as failed FROM `jobscheduling` where exception is not null");
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as total FROM `jobscheduling`");

		/* create main query */
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT open, CONCAT(ROUND(100 * open / total, 2), \"%\") as \"open (rel)\", running, CONCAT(ROUND(100 * running / total, 2), \"%\") as \"running (rel)\", finished, CONCAT(ROUND(100 * finished / total, 2), \"%\") as \"finished (rel)\", failed, total, CONCAT(ROUND(avgRuntimeFinished), \"s\") as \"Average Time of Finished\", CONCAT(ROUND(avgRuntimeFinished * open / " + numberOfParallelJobs + "), \"s\") as \"ETA\" FROM ");
		for (int t = 1; t < subQueries.size(); t++) {
			sb.append("(");
			sb.append(subQueries.get(t - 1));
			sb.append(") as t");
			sb.append(t);
			sb.append(" NATURAL JOIN ");
		}
		sb.append("(");
		sb.append(subQueries.get(subQueries.size() - 1));
		sb.append(") as t" + subQueries.size());
		return sb.toString();
	}

	public static void main(final String[] args) {
		System.out.println(getProgressQuery("jobscheduling", 500));
	}
}
