package hasco.knowledgebase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import hasco.core.Util;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;
import jaicore.basic.SQLAdapter;
import jaicore.basic.sets.PartialOrderedSet;
import jaicore.ml.core.CategoricalFeatureDomain;
import jaicore.ml.core.FeatureSpace;
import jaicore.ml.intervaltree.ExtendedRandomForest;
import jaicore.ml.intervaltree.ExtendedRandomTree;
import weka.core.Attribute;
import weka.core.AttributeMetaInfo;
import weka.core.Instances;
import weka.core.ProtectedProperties;

/**
 * Knowledge base that manages observed performance behavior
 * 
 * @author jmhansel
 *
 */

public class PerformanceKnowledgeBase implements IKnowledgeBase {

	private SQLAdapter sqlAdapter;
	private Map<String, HashMap<ComponentInstance, Double>> performanceSamples;
	private Map<String, ExtendedRandomForest> forests;

	public PerformanceKnowledgeBase(final SQLAdapter sqlAdapter) {
		super();
		this.sqlAdapter = sqlAdapter;
		this.performanceSamples = new HashMap<String, HashMap<ComponentInstance, Double>>();
		forests = new HashMap<String, ExtendedRandomForest>();
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

	private void initiliazeForests() {
		for (HashMap<ComponentInstance, Double> samples : performanceSamples.values()) {
			for (ComponentInstance componentInstance : samples.keySet()) {
				if (forests.get(componentInstance) == null) {
					ExtendedRandomForest curForest = new ExtendedRandomForest();
					forests.put(Util.getComponentNamesOfComposition(componentInstance), curForest);
				}
			}
		}
	}

	public String getStringOfMaps() {
		return performanceSamples.toString();
	}

	public FeatureSpace createFeatureSpaceFromComponentInstance(ComponentInstance compInst) {
		FeatureSpace space = new FeatureSpace();
		for (Parameter param : compInst.getComponent().getParameters()) {
			ParameterDomain domain = param.getDefaultDomain();
		}
		return space;
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

	public Instances createInstancesForPerformanceSamples(String benchmarkName, ComponentInstance pipeline) {
		Instances instances = null;
		// Add parameter domains as attributes
		List<Component> components = Util.getComponentsOfComposition(pipeline);
		for (Component component : components) {
			PartialOrderedSet<Parameter> parameters = component.getParameters();
			ArrayList<Attribute> attributes = new ArrayList<Attribute>(parameters.size());
			for (Parameter parameter : parameters) {
				ParameterDomain domain = parameter.getDefaultDomain();
				Attribute attr = null;
				if (domain instanceof CategoricalParameterDomain) {
					CategoricalParameterDomain catDomain = (CategoricalParameterDomain) domain;
					attr = new Attribute(parameter.getName(), Arrays.asList(catDomain.getValues()));
				} else if (domain instanceof NumericParameterDomain) {
					NumericParameterDomain numDomain = (NumericParameterDomain) domain;
					// TODO is there a better way to set the range of this attribute?
					String range = "[" + numDomain.getMin() + "," + numDomain.getMax() + "]";
					Properties prop = new Properties();
					prop.setProperty("range", range);
					ProtectedProperties metaInfo = new ProtectedProperties(prop);
					attr = new Attribute(parameter.getName(), metaInfo);
				}
				attributes.add(attr);
			}
		}
		// Add performance score as class attribute
		return instances;
	}

}
