package hasco.eventlogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;

import hasco.events.HASCORunStartedEvent;
import hasco.events.HASCORunTerminatedEvent;
import hasco.events.HASCOSolutionEvaluationEvent;
import jaicore.basic.MySQLAdapter;

public class HASCOSQLEventLogger<T, V extends Comparable<V>> {

	private int runId;
	private final MySQLAdapter sqlAdapter;

	public HASCOSQLEventLogger(MySQLAdapter sqlAdapter) {
		super();
		this.sqlAdapter = sqlAdapter;
		
		/* initialize tables if not existent */
		try {
			ResultSet rs = sqlAdapter.getResultsOfQuery("SHOW TABLES");
			boolean haveRunTable = false;
			boolean haveEvaluationTable = false;
			while (rs.next()) {
				String tableName = rs.getString(1);
				if (tableName.equals("runs"))
					haveRunTable = true;
				else if (tableName.equals("evaluations"))
					haveEvaluationTable = true;
			}
			
			if (!haveRunTable) {
				System.out.println("Creating table for runs");
				sqlAdapter.update("CREATE TABLE `runs` ( `run_id` int(8) NOT NULL AUTO_INCREMENT, `seed` int(20) NOT NULL, `timeout` int(10) NOT NULL, `CPUs` int(2) NOT NULL, `benchmark` varchar(200) COLLATE utf8_bin DEFAULT NULL, `run_started` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, `run_terminated` timestamp NULL DEFAULT NULL, `solution` json DEFAULT NULL, `score` double DEFAULT NULL, PRIMARY KEY (`run_id`)) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_bin", new ArrayList<>());
			}
			if (!haveEvaluationTable) {
				System.out.println("Creating table for evaluations");
				sqlAdapter.update("CREATE TABLE `evaluations` (\r\n" + 
						" `evaluation_id` int(10) NOT NULL AUTO_INCREMENT,\r\n" + 
						" `run_id` int(8) NOT NULL,\r\n" + 
						" `composition` json NOT NULL,\r\n" + 
						" `score` double NOT NULL,\r\n" + 
						" PRIMARY KEY (`evaluation_id`)\r\n" + 
						") ENGINE=InnoDB AUTO_INCREMENT=296 DEFAULT CHARSET=utf8 COLLATE=utf8_bin", new ArrayList<>());
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Subscribe
	public void receiveRunStartedEvent(HASCORunStartedEvent<T, V> event) {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("seed", "" + event.getSeed());
			map.put("timeout", "" + event.getTimeout());
			map.put("CPUs", "" + event.getNumberOfCPUS());
			map.put("benchmark", event.getBenchmark().toString());
			runId = sqlAdapter.insert("runs", map);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Subscribe
	public void receiveSolutionEvaluationEvent(HASCOSolutionEvaluationEvent<T, V> solution) {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("run_id", "" + runId);
			ObjectMapper mapper = new ObjectMapper();
			String composition = mapper.writeValueAsString(solution.getComposition());
			map.put("composition", composition);
			map.put("score", solution.getScore().toString());
			sqlAdapter.insert("evaluations", map);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Subscribe
	public void receiveRunTerminatedEvent(HASCORunTerminatedEvent<T, V> event) {
		try {
			Map<String, String> valueMap = new HashMap<>();
			valueMap.put("solution", "" + new ObjectMapper().writeValueAsString(event.getCompositionOfSolution()));
			valueMap.put("run_terminated", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(Instant.now())));
			valueMap.put("score", event.getScore().toString());
			
			Map<String,String> conditionMap = new HashMap<>();
			conditionMap.put("run_id", "" + runId);
			sqlAdapter.update("runs", valueMap, conditionMap);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
