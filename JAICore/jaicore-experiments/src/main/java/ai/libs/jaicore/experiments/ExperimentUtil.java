package ai.libs.jaicore.experiments;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.experiments.databasehandle.AExperimenterSQLHandle;

public class ExperimentUtil {

	private ExperimentUtil() {
		/* no instantiation desired */
	}

	public static String getProgressQuery(final String tablename) {
		return getProgressQuery(tablename, 0);
	}

	public static String getProgressQuery(final String tablename, final int numberOfParallelJobs) {

		/* create sub-queries */
		List<String> subQueries = new ArrayList<>();
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as \"open\" FROM `" + tablename + "` WHERE time_started is null");
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as \"running\" FROM `" + tablename + "` WHERE time_started is not null and time_end is null");
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as finished, AVG(TIMESTAMPDIFF(SECOND, time_started, time_end)) as avgRuntimeFinished  FROM `" + tablename + "` WHERE time_started is not null and time_end is not null");
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as successful FROM `" + tablename + "` where time_end is not null and exception is null");
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as failed FROM `" + tablename + "` where exception is not null");
		subQueries.add("SELECT \"aux\" as pk, COUNT(*) as total FROM `" + tablename + "`");

		/* create main query */
		StringBuilder sb = new StringBuilder();
		sb.append(
				"SELECT total, open, CONCAT(ROUND(100 * open / total, 2), \"%\") as \"open (rel)\", running, CONCAT(ROUND(100 * running / total, 2), \"%\") as \"running (rel)\", finished, CONCAT(ROUND(100 * finished / total, 2), \"%\") as \"finished (rel)\", successful, failed, CONCAT(ROUND(100 * successful / (successful + failed), 2), \"%\") as \"success rate\", CONCAT(ROUND(avgRuntimeFinished), \"s\") as \"Average Time of Finished\", CONCAT(ROUND(avgRuntimeFinished * open / "
						+ (numberOfParallelJobs > 0 ? numberOfParallelJobs : "running") + "), \"s\") as \"ETA\" FROM ");
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

	public static String getQueryToIdentifyCorruptRuns(final String tablename) {
		return "SELECT * FROM (SELECT " + AExperimenterSQLHandle.FIELD_EXECUTOR + ", COUNT(*) as n FROM `" + tablename + "` WHERE time_started is not null and time_end is null group by " + AExperimenterSQLHandle.FIELD_EXECUTOR + ") as t where n > 1";
	}

	public static String getQueryToListAllCorruptJobRuns(final String tablename) {
		return "SELECT t1.* FROM `" + tablename + "` as t1 join `" + tablename + "` as t2 USING(" + AExperimenterSQLHandle.FIELD_EXECUTOR + ") WHERE t1.time_started is not null and t1.time_end is null and t2.time_started > t1.time_started and t2.time_end is null";
	}

	public static String getQueryToListAllRunningExecutions(final String tablename) {
		return "SELECT * FROM `" + tablename + "` WHERE time_started is not null and time_end is null";
	}

	public static String getQueryToListAllFailedExecutions(final String tablename) {
		return "SELECT * FROM `" + tablename + "` WHERE exception is not null";
	}

	public static String getOccurredExceptions(final String tablename, final String... ignorePatterns) {
		StringBuilder sb = new StringBuilder();
		for (String p : ignorePatterns) {
			sb.append(" AND `exception` NOT LIKE '%" + p + "%'");
		}
		return "SELECT exception, COUNT(*) FROM `" + tablename + "` WHERE exception is not null" + sb.toString() + " GROUP BY exception";
	}
}
