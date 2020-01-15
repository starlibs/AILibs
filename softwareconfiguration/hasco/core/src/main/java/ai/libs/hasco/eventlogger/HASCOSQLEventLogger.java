package ai.libs.hasco.eventlogger;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.events.HASCORunStartedEvent;
import ai.libs.hasco.events.HASCORunTerminatedEvent;
import ai.libs.hasco.events.HASCOSolutionEvaluationEvent;
import ai.libs.jaicore.db.sql.SQLAdapter;

public class HASCOSQLEventLogger<T, V extends Comparable<V>> {

	private static final String MSG_EXCEPTION = "Observed exception: {}";
	private Logger logger = LoggerFactory.getLogger(HASCOSQLEventLogger.class);
	private int runId;
	private final SQLAdapter sqlAdapter;

	public HASCOSQLEventLogger(final SQLAdapter sqlAdapter) {
		super();
		this.sqlAdapter = sqlAdapter;

		/* initialize tables if not existent */
		try {
			List<IKVStore> rs = sqlAdapter.getResultsOfQuery("SHOW TABLES");
			boolean haveRunTable = false;
			boolean haveEvaluationTable = false;
			for (IKVStore store : rs) {
				Optional<String> tableNameKeyOpt = store.keySet().stream().filter(x -> x.startsWith("Table_in")).findFirst();
				if (tableNameKeyOpt.isPresent()) {
					String tableName = store.getAsString(tableNameKeyOpt.get());
					if (tableName.equals("runs")) {
						haveRunTable = true;
					} else if (tableName.equals("evaluations")) {
						haveEvaluationTable = true;
					}
				}
			}

			if (!haveRunTable) {
				this.logger.info("Creating table for runs");
				sqlAdapter.update(
						"CREATE TABLE `runs` ( `run_id` int(8) NOT NULL AUTO_INCREMENT, `seed` int(20) NOT NULL, `timeout` int(10) NOT NULL, `CPUs` int(2) NOT NULL, `benchmark` varchar(200) COLLATE utf8_bin DEFAULT NULL, `run_started` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, `run_terminated` timestamp NULL DEFAULT NULL, `solution` json DEFAULT NULL, `score` double DEFAULT NULL, PRIMARY KEY (`run_id`)) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin",
						new ArrayList<>());
			}
			if (!haveEvaluationTable) {
				this.logger.info("Creating table for evaluations");
				sqlAdapter.update("CREATE TABLE `evaluations` (\r\n" + " `evaluation_id` int(10) NOT NULL AUTO_INCREMENT,\r\n" + " `run_id` int(8) NOT NULL,\r\n" + " `composition` json NOT NULL,\r\n" + " `score` double NOT NULL,\r\n"
						+ " PRIMARY KEY (`evaluation_id`)\r\n" + ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin", new ArrayList<>());
			}

		} catch (SQLException e) {
			this.logger.error(MSG_EXCEPTION, e);
		}
	}

	@Subscribe
	public void receiveRunStartedEvent(final HASCORunStartedEvent<T, V> event) {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("seed", "" + event.getSeed());
			map.put("timeout", "" + event.getTimeout());
			map.put("CPUs", "" + event.getNumberOfCPUS());
			map.put("benchmark", event.getBenchmark().toString());
			this.runId = this.sqlAdapter.insert("runs", map)[0];
		} catch (Exception e) {
			this.logger.error(MSG_EXCEPTION, e);
		}
	}

	@Subscribe
	public void receiveSolutionEvaluationEvent(final HASCOSolutionEvaluationEvent<T, V> solution) {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("run_id", "" + this.runId);
			ObjectMapper mapper = new ObjectMapper();
			String composition = mapper.writeValueAsString(solution.getComposition());
			map.put("composition", composition);
			map.put("score", solution.getScore().toString());
			this.sqlAdapter.insert("evaluations", map);
		} catch (Exception e) {
			this.logger.error(MSG_EXCEPTION, e);
		}
	}

	@Subscribe
	public void receiveRunTerminatedEvent(final HASCORunTerminatedEvent<T, V> event) {
		try {
			Map<String, String> valueMap = new HashMap<>();
			valueMap.put("solution", "" + new ObjectMapper().writeValueAsString(event.getCompositionOfSolution()));
			valueMap.put("run_terminated", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(Instant.now())));
			valueMap.put("score", event.getScore().toString());

			Map<String, String> conditionMap = new HashMap<>();
			conditionMap.put("run_id", "" + this.runId);
			this.sqlAdapter.update("runs", valueMap, conditionMap);
		} catch (Exception e) {
			this.logger.error(MSG_EXCEPTION, e);
		}
	}
}
