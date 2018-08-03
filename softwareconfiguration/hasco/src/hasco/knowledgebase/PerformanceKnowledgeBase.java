package hasco.knowledgebase;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hasco.core.Util;
import hasco.model.CategoricalParameterDomain;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;
import jaicore.basic.SQLAdapter;
import jaicore.basic.sets.PartialOrderedSet;
import jaicore.ml.core.FeatureSpace;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.ProtectedProperties;

/**
 * Knowledge base that manages observed performance behavior
 * 
 * @author jmhansel
 *
 */

public class PerformanceKnowledgeBase {

	private SQLAdapter sqlAdapter;
	private Map<String, HashMap<ComponentInstance, Double>> performanceSamples;
	/** This is map contains a String */
	private Map<String, HashMap<String, List<Pair<ParameterConfiguration, Double>>>> performanceSamplesByIdentifier;
	private Map<String, HashMap<String, Instances>> performanceInstancesByIdentifier;

	/**
	 * Inner helper class for managing parameter configurations easily.
	 * 
	 * @author jmhansel
	 *
	 */
	private class ParameterConfiguration {
		private final List<Pair<Parameter, String>> values;

		public ParameterConfiguration(ComponentInstance composition) {
			ArrayList<Pair<Parameter, String>> temp = new ArrayList<Pair<Parameter, String>>();
			List<ComponentInstance> componentInstances = Util.getComponentInstancesOfComposition(composition);
			for (ComponentInstance compInst : componentInstances) {
				PartialOrderedSet<Parameter> parameters = compInst.getComponent().getParameters();
				for (Parameter parameter : parameters) {
					temp.add(Pair.of(parameter, compInst.getParameterValues().get(parameter.getName())));
				}
			}
			// Make the list immutable to avoid problems with hashCode
			values = Collections.unmodifiableList(temp);
		}

		@Override
		public int hashCode() {
			return values.hashCode();
		}

		public List<Pair<Parameter, String>> getValues() {
			return this.values;
		}
	}

	public PerformanceKnowledgeBase(final SQLAdapter sqlAdapter) {
		super();
		this.sqlAdapter = sqlAdapter;
		// this.performanceSamples = new HashMap<String, HashMap<ComponentInstance,
		// Double>>();
		// this.performanceSamplesByIdentifier = new HashMap<String, HashMap<String,
		// List<Pair<ParameterConfiguration, Double>>>>();
		this.performanceInstancesByIdentifier = new HashMap<String, HashMap<String, Instances>>();
	}

	public PerformanceKnowledgeBase() {
		super();
		// this.performanceSamples = new HashMap<String, HashMap<ComponentInstance,
		// Double>>();
		// this.performanceSamplesByIdentifier = new HashMap<String, HashMap<String,
		// List<Pair<ParameterConfiguration, Double>>>>();
		this.performanceInstancesByIdentifier = new HashMap<String, HashMap<String, Instances>>();
	}

	public void addPerformanceSample(String benchmarkName, ComponentInstance componentInstance, double score,
			boolean addToDB) {
		String identifier = Util.getComponentNamesOfComposition(componentInstance);
		// if (performanceSamples.get(benchmarkName) == null) {
		// HashMap<ComponentInstance, Double> newMap = new HashMap<ComponentInstance,
		// Double>();
		// newMap.put(componentInstance, score);
		// performanceSamples.put(benchmarkName, newMap);
		// } else {
		// performanceSamples.get(benchmarkName).put(componentInstance, score);
		// }
		// if (performanceSamplesByIdentifier.get(benchmarkName) == null) {
		// HashMap<String, List<Pair<ParameterConfiguration, Double>>> newMap = new
		// HashMap<String, List<Pair<ParameterConfiguration, Double>>>();
		// LinkedList<Pair<ParameterConfiguration, Double>> sampleList = new
		// LinkedList<Pair<ParameterConfiguration, Double>>();
		// sampleList.add(Pair.of(new ParameterConfiguration(componentInstance),
		// score));
		// newMap.put(identifier, new LinkedList<Pair<ParameterConfiguration,
		// Double>>());
		// performanceSamplesByIdentifier.put(benchmarkName, newMap);
		// } else {
		// if
		// (performanceSamplesByIdentifier.get(benchmarkName).containsKey(identifier)) {
		// performanceSamplesByIdentifier.get(benchmarkName).get(identifier)
		// .add(Pair.of(new ParameterConfiguration(componentInstance), score));
		// } else {
		// performanceSamplesByIdentifier.get(benchmarkName).put(identifier,
		// new LinkedList<Pair<ParameterConfiguration, Double>>());
		// performanceSamplesByIdentifier.get(benchmarkName).get(identifier)
		// .add(Pair.of(new ParameterConfiguration(componentInstance), score));
		// }
		// }

		if (performanceInstancesByIdentifier.get(benchmarkName) == null) {
			System.out.println("Creating new HashMap");
			HashMap<String, Instances> newMap = new HashMap<String, Instances>();
			performanceInstancesByIdentifier.put(benchmarkName, newMap);
		}

		if (!performanceInstancesByIdentifier.get(benchmarkName).containsKey(identifier)) {
			System.out.println("Creating new Instances Object");
			// Create Instances pipeline for this pipeline type
			ParameterConfiguration parameterConfig = new ParameterConfiguration(componentInstance);
			Instances instances = null;
			// Add parameter domains as attributes
			List<ComponentInstance> componentInstances = Util.getComponentInstancesOfComposition(componentInstance);
			ArrayList<Attribute> allAttributes = new ArrayList<Attribute>();
			for (ComponentInstance ci : componentInstances) {
				PartialOrderedSet<Parameter> parameters = ci.getComponent().getParameters();
				ArrayList<Attribute> attributes = new ArrayList<Attribute>(parameters.size());
				for (Parameter parameter : parameters) {
					ParameterDomain domain = parameter.getDefaultDomain();
					Attribute attr = null;
					if (domain instanceof CategoricalParameterDomain) {
						CategoricalParameterDomain catDomain = (CategoricalParameterDomain) domain;
						// TODO further namespacing of attributes!!!
						attr = new Attribute(ci.getComponent().getName() + "::" + parameter.getName(),
								Arrays.asList(catDomain.getValues()));
					} else if (domain instanceof NumericParameterDomain) {
						NumericParameterDomain numDomain = (NumericParameterDomain) domain;
						// TODO is there a better way to set the range of this attribute?
						// if(numDomain.getMin() == numDomain.getMax()) {
						// System.out.println("Domain has range of 0, skipping it!");
						// continue;
						// }
						String range = "[" + numDomain.getMin() + "," + numDomain.getMax() + "]";
						Properties prop = new Properties();
						prop.setProperty("range", range);
						ProtectedProperties metaInfo = new ProtectedProperties(prop);
						attr = new Attribute(ci.getComponent().getName() + "::" + parameter.getName(), metaInfo);
					}
					// System.out.println("Trying to add parameter: " + attr.name() + " for
					// component: "
					// + componentInstance.getComponent().getName());

					attributes.add(attr);
				}
				allAttributes.addAll(attributes);
			}
			// Add performance score as class attribute TODO make score numeric?
			Attribute scoreAttr = new Attribute("performance_score");
			allAttributes.add(scoreAttr);
			instances = new Instances("performance_samples", allAttributes, 16);
			instances.setClass(scoreAttr);
			performanceInstancesByIdentifier.get(benchmarkName).put(identifier, instances);
		}

		
			// Add Instance for performance samples to corresponding Instances
			System.out.println("Adding instance");
			Instances instances = performanceInstancesByIdentifier.get(benchmarkName).get(identifier);
			DenseInstance instance = new DenseInstance(instances.numAttributes());
			ParameterConfiguration config = new ParameterConfiguration(componentInstance);
			List<Pair<Parameter, String>> values = config.getValues();
			for (int i = 0; i < instances.numAttributes() - 1; i++) {
				Attribute attr = instances.attribute(i);
				Parameter param = values.get(i).getLeft();
				System.out.println("Adding vlaue " + values.get(i).getRight() + " for Parameter " + param);
				if (param.isCategorical()) {
					Enumeration<Object> e = attr.enumerateValues();
					System.out.println("Values: ");
					if (e == null) {
						System.out.println("Enumeration is null");
					} else {
						while (e.hasMoreElements()) {
							System.out.println(e.nextElement().toString());
						}
					}
					String value = values.get(i).getRight();
//					if (value.equals("default")) {
//						System.out.println("Value is default!");
//						value = (String) param.getDefaultValue();
//						System.out.println("Default value is: " + value);
//					}
					instance.setValue(attr, value);
				} else if (param.isNumeric()) {
//					String value = values.get(i).getRight();
//					if (value.equals("default"))
//						value = (String) param.getDefaultValue();
					double finalValue = Double.parseDouble(values.get(i).getRight());
					instance.setValue(attr, finalValue);
					// System.out.println("bounds: [" + attr.getLowerNumericBound() + "," +
					// attr.getUpperNumericBound() + "]");
				}
			}
			Attribute scoreAttr = instances.classAttribute();
			instance.setValue(scoreAttr, score);
			System.out.println("Adding instance: " + instance.toString());
			performanceInstancesByIdentifier.get(benchmarkName).get(identifier).add(instance);

		if (addToDB)
			this.addPerformanceSampleToDB(benchmarkName, componentInstance, score);
	}

	public Map<String, HashMap<ComponentInstance, Double>> getPerformanceSamples() {
		return this.performanceSamples;
	}

	public Map<String, HashMap<String, List<Pair<ParameterConfiguration, Double>>>> getPerformanceSamplesByIdentifier() {
		return performanceSamplesByIdentifier;
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
				if (tableName.equals("performance_samples")) {
					havePerformanceTable = true;
				}
			}

			if (!havePerformanceTable) {
				System.out.println("Creating table for performance samples");
				sqlAdapter.update(
						"CREATE TABLE `performance_samples` (\r\n" + " `sample_id` int(10) NOT NULL AUTO_INCREMENT,\r\n"
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
			this.sqlAdapter.insert("performance_samples", map);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// public int getNumSamples(String benchmarkName, String identifier) {
	// if (performanceSamplesByIdentifier.containsKey(benchmarkName)) {
	// if
	// (performanceSamplesByIdentifier.get(benchmarkName).containsKey(identifier))
	// return
	// performanceSamplesByIdentifier.get(benchmarkName).get(identifier).size();
	// else
	// return 0;
	// } else
	// return 0;
	// }

	public int getNumSamples(String benchmarkName, String identifier) {
		if(!this.performanceInstancesByIdentifier.containsKey(benchmarkName))
			return 0;
		if(!this.performanceInstancesByIdentifier.get(benchmarkName).containsKey(identifier))
			return 0;
		
		return this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier).numInstances();
	}

	public void loadPerformanceSamplesFromDB() {
		try {
			ResultSet rs = sqlAdapter
					.getResultsOfQuery("SELECT benchmark, composition, score FROM performance_samples");
			ObjectMapper mapper = new ObjectMapper();
			while (rs.next()) {
				String benchmarkName = rs.getString(1);
				String ciString = rs.getString(2);
				System.out.println(ciString);
				ComponentInstance composition = mapper.readValue(ciString, ComponentInstance.class);
				double score = rs.getDouble(3);
				this.addPerformanceSample(benchmarkName, composition, score, false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks whether k samples are available, which are
	 * 
	 * @param k
	 * @return
	 */
	public boolean kDistinctSamplesAvailable(String benchmarkName, ComponentInstance composition, int k) {
		boolean result = false;

		return result;
	}

	/**
	 * Creates an Instances object containing all performance samples observed for
	 * pipelines of the type of the given ComponentInstance for the given benchmark.
	 * 
	 * @param benchmarkName
	 * @param pipeline
	 * @return Instances
	 */
	// public Instances createInstancesForPerformanceSamples(String benchmarkName,
	// ComponentInstance composition) {
	// Instances instances = null;
	// String identifier = Util.getComponentNamesOfComposition(composition);
	// // Add parameter domains as attributes
	// List<ComponentInstance> componentInstances =
	// Util.getComponentInstancesOfComposition(composition);
	// ArrayList<Attribute> allAttributes = new ArrayList<Attribute>();
	// for (ComponentInstance componentInstance : componentInstances) {
	// PartialOrderedSet<Parameter> parameters =
	// componentInstance.getComponent().getParameters();
	// ArrayList<Attribute> attributes = new
	// ArrayList<Attribute>(parameters.size());
	// for (Parameter parameter : parameters) {
	// ParameterDomain domain = parameter.getDefaultDomain();
	// Attribute attr = null;
	// if (domain instanceof CategoricalParameterDomain) {
	// CategoricalParameterDomain catDomain = (CategoricalParameterDomain) domain;
	// // TODO further namespacing of attributes!!!
	// attr = new Attribute(componentInstance.getComponent().getName() + "::" +
	// parameter.getName(),
	// Arrays.asList(catDomain.getValues()));
	// } else if (domain instanceof NumericParameterDomain) {
	// NumericParameterDomain numDomain = (NumericParameterDomain) domain;
	// // TODO is there a better way to set the range of this attribute?
	// // if(numDomain.getMin() == numDomain.getMax()) {
	// // System.out.println("Domain has range of 0, skipping it!");
	// // continue;
	// // }
	// String range = "[" + numDomain.getMin() + "," + numDomain.getMax() + "]";
	// Properties prop = new Properties();
	// prop.setProperty("range", range);
	// ProtectedProperties metaInfo = new ProtectedProperties(prop);
	// attr = new Attribute(componentInstance.getComponent().getName() + "::" +
	// parameter.getName(),
	// metaInfo);
	// }
	// // System.out.println("Trying to add parameter: " + attr.name() + " for
	// // component: "
	// // + componentInstance.getComponent().getName());
	//
	// attributes.add(attr);
	// }
	// allAttributes.addAll(attributes);
	// }
	// // Add performance score as class attribute TODO make score numeric?
	// Attribute scoreAttr = new Attribute("performance_score");
	// allAttributes.add(scoreAttr);
	// instances = new Instances("performance_samples", allAttributes,
	// this.performanceSamples.get(benchmarkName).size());
	// instances.setClass(scoreAttr);
	// List<Pair<ParameterConfiguration, Double>> samples =
	// performanceSamplesByIdentifier.get(benchmarkName)
	// .get(identifier);
	// for (Pair<ParameterConfiguration, Double> sample : samples) {
	// DenseInstance instance = new DenseInstance(instances.numAttributes());
	// ParameterConfiguration config = sample.getLeft();
	// double score = sample.getRight();
	// List<Pair<Parameter, String>> values = config.getValues();
	// for (int i = 0; i < instances.numAttributes() - 1; i++) {
	// Attribute attr = instances.attribute(i);
	// Parameter param = values.get(i).getLeft();
	// if (param.isCategorical()) {
	// Enumeration<Object> e = attr.enumerateValues();
	// System.out.println("Values: ");
	// if (e == null) {
	// System.out.println("Enumeration is null");
	// } else {
	// while (e.hasMoreElements()) {
	// System.out.println(e.nextElement().toString());
	// }
	// }
	// String value = values.get(i).getRight();
	// if (value.equals("default")) {
	// System.out.println("Value is default!");
	// value = (String) param.getDefaultValue();
	// System.out.println("Default value is: " + value);
	// }
	// System.out.println("Trying to add value: " + value);
	// instance.setValue(attr, value);
	// } else if (param.isNumeric()) {
	// String value = values.get(i).getRight();
	// if (value.equals("default"))
	// value = (String) param.getDefaultValue();
	// double finalValue = Double.parseDouble(values.get(i).getRight());
	// instance.setValue(attr, finalValue);
	// // System.out.println("bounds: [" + attr.getLowerNumericBound() + "," +
	// // attr.getUpperNumericBound() + "]");
	// }
	// }
	// instance.setValue(scoreAttr, score);
	// instances.add(instance);
	// }
	// return instances;
	// }

	public Instances getPerformanceSamples(String benchmarkName, ComponentInstance composition) {
		String identifier = Util.getComponentNamesOfComposition(composition);
		return this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier);
	}

	public Instances createInstancesForPerformanceSamples(String benchmarkName, ComponentInstance composition) {
		String identifier = Util.getComponentNamesOfComposition(composition);
		return this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier);
	}

}
