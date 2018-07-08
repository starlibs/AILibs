package hasco.knowledgebase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import jaicore.basic.SQLAdapter;
import jaicore.ml.intervaltree.ExtendedRandomTree;

/**
 * Knowledge base that manages observed performance behavior
 * 
 * @author jmhansel
 *
 */

public class PerformanceKnowledgeBase implements IKnowledgeBase {

	private SQLAdapter sqlAdapter;
	private Map<String, HashMap<ComponentInstance, Double>> performanceSamples;

	public PerformanceKnowledgeBase(final SQLAdapter sqlAdapter) {
		super();
		this.sqlAdapter = sqlAdapter;
		this.performanceSamples = new HashMap<String, HashMap<ComponentInstance, Double>>();
	}

	public Set<Component> getPipeline(ComponentInstance composition) {
		HashSet<Component> pipeline = new HashSet<Component>();
		pipeline.add(composition.getComponent());
		Collection<ComponentInstance> componentInstances = composition.getSatisfactionOfRequiredInterfaces().values();
		for(ComponentInstance componentInstance : componentInstances) {
			pipeline.add(componentInstance.getComponent());
		}
		// build a representation of the components in the pipeline
		return pipeline;
	}

	public PerformanceKnowledgeBase() {
		super();
		this.performanceSamples = new HashMap<String, HashMap<ComponentInstance, Double>>();
	}

	public void addPerformanceSample(String benchmarkName, ComponentInstance componentInstance, double score) {
		if (performanceSamples.get(benchmarkName) == null) {
			HashMap<ComponentInstance, Double> newMap = new HashMap<ComponentInstance, Double>();
			newMap.put(componentInstance, score);
			performanceSamples.put(benchmarkName, newMap);
		} else {
			performanceSamples.get(benchmarkName).put(componentInstance, score);
		}
	}

	public void initializeDBTables() {
		/* initialize tables if not existent */
		try {
			ResultSet rs = sqlAdapter.getResultsOfQuery("SHOW TABLES");
			boolean havePerformanceTable = false;
			while (rs.next()) {
				String tableName = rs.getString(1);
				if (tableName.equals("performance")) {
					havePerformanceTable = true;
				}
			}

			if (!havePerformanceTable) {
				System.out.println("Creating table for performance samples");
				sqlAdapter.update(
						"CREATE TABLE `performance` (\r\n" + " `sample_id` int(10) NOT NULL AUTO_INCREMENT,\r\n"
								+ " `benchmark` varchar(200) COLLATE utf8_bin DEFAULT NULL,\r\n"
								+ " `composition` json NOT NULL,\r\n" + " `score` double NOT NULL,\r\n"
								+ " PRIMARY KEY (`sample_id`)\r\n"
								+ ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin",
						new ArrayList<>());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addPerformanceSampleToDB(String benchmarkName, ComponentInstance componentInstance, double score) {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("benchmark", benchmarkName);
			ObjectMapper mapper = new ObjectMapper();
			String composition = mapper.writeValueAsString(componentInstance);
			map.put("composition", composition);
			map.put("score", "" + score);
			this.sqlAdapter.insert("evaluations", map);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void loadPerformanceSamplesFromDB() {
		// TODO
	}

	public double getImportanceOfParam(Parameter param) {
		// TODO Auto-generated method stub
		return 0;
	}

}
