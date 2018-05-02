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
			
			if (sqlAdapter.getDriver().startsWith("hsqldb"))
				sqlAdapter.update("SET DATABASE SQL SYNTAX MYS TRUE", new ArrayList<>());
			
			String sqlRuns = "CREATE TABLE IF NOT EXISTS runs(\r\n" + 
					"	run_id integer NOT NULL AUTO_INCREMENT,\r\n" + 
					"	seed INTEGER NOT NULL,\r\n" + 
					"	timeout INTEGER,\r\n" + 
					"	CPUs INTEGER,\r\n" + 
					"	benchmark varchar(200) DEFAULT NULL,\r\n" + 
					"	run_started timestamp DEFAULT CURRENT_TIMESTAMP,\r\n" + 
					"	run_terminated timestamp NULL,\r\n" + 
					"	solution varchar(1000) DEFAULT NULL,\r\n" + 
					"	score double DEFAULT NULL,\r\n" + 
					"	PRIMARY KEY (run_id)\r\n" + 
					")";
			sqlAdapter.update(sqlRuns, new ArrayList<>());
			
			String sqlEvaluations = "CREATE TABLE IF NOT EXISTS evaluations(\r\n" + 
					"	evaluation_id integer NOT NULL AUTO_INCREMENT,\r\n" + 
					"	run_id integer NOT NULL,\r\n" + 
					"	composition VARCHAR(1000) NOT NULL,\r\n" + 
					"	score double NOT NULL,\r\n" + 
					"	PRIMARY KEY (evaluation_id)\r\n" + 
					")";
			sqlAdapter.update(sqlEvaluations, new ArrayList<>());
			
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
