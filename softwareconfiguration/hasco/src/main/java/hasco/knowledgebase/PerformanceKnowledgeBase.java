package hasco.knowledgebase;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import hasco.core.Util;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Dependency;
import hasco.model.IParameterDomain;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.serialization.ParameterDeserializer;
import hasco.serialization.ParameterDomainDeserializer;
import jaicore.basic.SQLAdapter;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ProtectedProperties;

/**
 * Knowledge base that manages observed performance behavior
 *
 * @author jmhansel
 *
 */

public class PerformanceKnowledgeBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceKnowledgeBase.class);

	private static final String LABEL_PERFORMANCE_SAMPLES = "performance_samples";

	private SQLAdapter sqlAdapter;
	private Map<String, HashMap<ComponentInstance, Double>> performanceSamples;
	/** This is map contains a String */
	private Map<String, HashMap<String, List<Pair<ParameterConfiguration, Double>>>> performanceSamplesByIdentifier;
	private Map<String, HashMap<String, Instances>> performanceInstancesByIdentifier;
	private Map<String, HashMap<String, Instances>> performanceInstancesIndividualComponents;

	/**
	 * Inner helper class for managing parameter configurations easily.
	 *
	 * @author jmhansel
	 *
	 */
	private class ParameterConfiguration {
		private final List<Pair<Parameter, String>> values;

		public ParameterConfiguration(final ComponentInstance composition) {
			ArrayList<Pair<Parameter, String>> temp = new ArrayList<>();
			List<ComponentInstance> componentInstances = Util.getComponentInstancesOfComposition(composition);
			for (ComponentInstance compInst : componentInstances) {
				List<Parameter> parameters = compInst.getComponent().getParameters().getTotalOrder();
				for (Parameter parameter : parameters) {
					String value;
					if (compInst.getParametersThatHaveBeenSetExplicitly().contains(parameter)) {
						value = compInst.getParameterValues().get(parameter.getName());
					} else {
						value = parameter.getDefaultValue().toString();
					}
					temp.add(Pair.of(parameter, value));
				}
			}
			// Make the list immutable to avoid problems with hashCode
			this.values = Collections.unmodifiableList(temp);
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(this.values).toHashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof ParameterConfiguration)) {
				return false;
			}
			ParameterConfiguration other = (ParameterConfiguration) obj;
			return new EqualsBuilder().append(this.values, other.values).isEquals();
		}

		public List<Pair<Parameter, String>> getValues() {
			return this.values;
		}
	}

	public PerformanceKnowledgeBase(final SQLAdapter sqlAdapter) {
		super();
		this.sqlAdapter = sqlAdapter;
		this.performanceInstancesByIdentifier = new HashMap<>();
		this.performanceInstancesIndividualComponents = new HashMap<>();
	}

	public PerformanceKnowledgeBase() {
		super();
		this.performanceInstancesByIdentifier = new HashMap<>();
		this.performanceInstancesIndividualComponents = new HashMap<>();
	}

	private Attribute getAttribute(final ComponentInstance ci, final Parameter parameter) {
		IParameterDomain domain = parameter.getDefaultDomain();
		if (domain instanceof CategoricalParameterDomain) {
			CategoricalParameterDomain catDomain = (CategoricalParameterDomain) domain;
			return new Attribute(ci.getComponent().getName() + "::" + parameter.getName(), Arrays.asList(catDomain.getValues()));
		} else if (domain instanceof NumericParameterDomain) {
			NumericParameterDomain numDomain = (NumericParameterDomain) domain;
			String range = "[" + numDomain.getMin() + "," + numDomain.getMax() + "]";
			Properties prop = new Properties();
			prop.setProperty("range", range);
			ProtectedProperties metaInfo = new ProtectedProperties(prop);
			return new Attribute(ci.getComponent().getName() + "::" + parameter.getName(), metaInfo);
		} else {
			return null;
		}
	}

	public Instances getInstancesForCI(final ComponentInstance ci) {
		// Create Instances pipeline for this pipeline type
		Instances instances = null;
		// Add parameter domains as attributes
		List<Parameter> parameters = ci.getComponent().getParameters().getTotalOrder();
		ArrayList<Attribute> attributes = new ArrayList<>(parameters.size());
		for (Parameter parameter : parameters) {
			attributes.add(this.getAttribute(ci, parameter));
		}
		Attribute scoreAttr = new Attribute("performance_score");
		attributes.add(scoreAttr);
		instances = new Instances(LABEL_PERFORMANCE_SAMPLES, attributes, 16);
		instances.setClass(scoreAttr);
		return instances;
	}

	public Instances getInstancesForCIList(final List<ComponentInstance> componentInstances) {
		// Create Instances pipeline for this pipeline type
		Instances instances = null;
		ArrayList<Attribute> allAttributes = new ArrayList<>();
		for (ComponentInstance ci : componentInstances) {
			List<Parameter> parameters = ci.getComponent().getParameters().getTotalOrder();
			ArrayList<Attribute> attributes = new ArrayList<>(parameters.size());
			for (Parameter parameter : parameters) {
				attributes.add(this.getAttribute(ci, parameter));
			}
			allAttributes.addAll(attributes);
		}
		Attribute scoreAttr = new Attribute("performance_score");
		allAttributes.add(scoreAttr);
		instances = new Instances(LABEL_PERFORMANCE_SAMPLES, allAttributes, 16);
		instances.setClass(scoreAttr);
		return instances;
	}

	public void addPerformanceSample(final String benchmarkName, final ComponentInstance componentInstance, final double score, final boolean addToDB) {
		String identifier = Util.getComponentNamesOfComposition(componentInstance);

		if (this.performanceInstancesByIdentifier.get(benchmarkName) == null) {
			HashMap<String, Instances> newMap = new HashMap<>();
			HashMap<String, Instances> newMap2 = new HashMap<>();
			this.performanceInstancesByIdentifier.put(benchmarkName, newMap);
			this.performanceInstancesIndividualComponents.put(benchmarkName, newMap2);

		}

		if (!this.performanceInstancesByIdentifier.get(benchmarkName).containsKey(identifier)) {
			// Add parameter domains as attributes
			List<ComponentInstance> componentInstances = Util.getComponentInstancesOfComposition(componentInstance);
			this.performanceInstancesByIdentifier.get(benchmarkName).put(identifier, this.getInstancesForCIList(componentInstances));
		}
		List<ComponentInstance> componentInstances = Util.getComponentInstancesOfComposition(componentInstance);
		for (ComponentInstance ci : componentInstances) {
			if (!this.performanceInstancesIndividualComponents.get(benchmarkName).containsKey(ci.getComponent().getName())) {
				this.performanceInstancesIndividualComponents.get(benchmarkName).put(ci.getComponent().getName(), this.getInstancesForCI(ci));
			}
		}

		// Add Instance for performance samples to corresponding Instances
		Instances instances = this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier);
		DenseInstance instance = new DenseInstance(instances.numAttributes());
		ParameterConfiguration config = new ParameterConfiguration(componentInstance);
		List<Pair<Parameter, String>> values = config.getValues();
		for (int i = 0; i < instances.numAttributes() - 1; i++) {
			Attribute attr = instances.attribute(i);
			Parameter param = values.get(i).getLeft();
			if (values.get(i).getRight() != null) {
				if (param.isCategorical()) {
					String value = values.get(i).getRight();
					boolean attrContainsValue = false;
					Enumeration<Object> possibleValues = attr.enumerateValues();
					while (possibleValues.hasMoreElements() && !attrContainsValue) {
						Object o = possibleValues.nextElement();
						if (o.equals(value)) {
							attrContainsValue = true;
						}
					}
					if (attrContainsValue) {
						instance.setValue(attr, value);
					} else {
						LOGGER.error("The value you're trying to insert is not in the attributes range!");
					}
				} else if (param.isNumeric()) {
					double finalValue = Double.parseDouble(values.get(i).getRight());
					instance.setValue(attr, finalValue);
				}
			}
		}
		Attribute scoreAttr = instances.classAttribute();
		instance.setValue(scoreAttr, score);
		this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier).add(instance);

		// Add Instance for individual component
		for (ComponentInstance ci : componentInstances) {
			this.performanceInstancesIndividualComponents.get(benchmarkName).get(ci.getComponent().getName()).add(this.getInstanceForIndividualCI(benchmarkName, ci, score));
		}

		if (addToDB) {
			this.addPerformanceSampleToDB(benchmarkName, componentInstance, score);
		}
	}

	public Instance getInstanceForIndividualCI(final String benchmarkName, final ComponentInstance ci, final double score) {
		Instances instancesInd = this.performanceInstancesIndividualComponents.get(benchmarkName).get(ci.getComponent().getName());
		DenseInstance instanceInd = new DenseInstance(instancesInd.numAttributes());
		for (int i = 0; i < instancesInd.numAttributes() - 1; i++) {
			Attribute attr = instancesInd.attribute(i);
			Parameter param = ci.getComponent().getParameterWithName(attr.name());
			String value;
			if (ci.getParametersThatHaveBeenSetExplicitly().contains(param)) {
				value = ci.getParameterValues().get(param.getName());
			} else {
				value = param.getDefaultValue().toString();
			}
			if (value != null) {
				if (param.isCategorical()) {
					boolean attrContainsValue = false;
					Enumeration<Object> possibleValues = attr.enumerateValues();
					while (possibleValues.hasMoreElements() && !attrContainsValue) {
						Object o = possibleValues.nextElement();
						if (o.equals(value)) {
							attrContainsValue = true;
						}
					}
					if (attrContainsValue) {
						instanceInd.setValue(attr, value);
					}
				} else if (param.isNumeric()) {
					double finalValue = Double.parseDouble(value);
					instanceInd.setValue(attr, finalValue);
				}
			}
		}
		Attribute scoreAttrInd = instancesInd.classAttribute();
		instanceInd.setValue(scoreAttrInd, score);
		return instanceInd;
	}

	public Map<String, HashMap<String, Instances>> getPerformanceInstancesIndividualComponents() {
		return this.performanceInstancesIndividualComponents;
	}

	public Map<String, HashMap<String, List<Pair<ParameterConfiguration, Double>>>> getPerformanceSamplesByIdentifier() {
		return this.performanceSamplesByIdentifier;
	}

	public String getStringOfMaps() {
		return this.performanceSamples.toString();
	}

	public void initializeDBTables() {
		/* initialize tables if not existent */
		try {
			ResultSet rs = this.sqlAdapter.getResultsOfQuery("SHOW TABLES");
			boolean havePerformanceTable = false;
			while (rs.next()) {
				String tableName = rs.getString(1);
				if (tableName.equals("performance_samples_J48")) {
					havePerformanceTable = true;
				}
			}

			if (!havePerformanceTable) {
				LOGGER.debug("Creating table for performance samples");
				this.sqlAdapter.update("CREATE TABLE `performance_samples_J48` (\r\n" + " `sample_id` int(10) NOT NULL AUTO_INCREMENT,\r\n" + " `dataset` varchar(200) COLLATE utf8_bin DEFAULT NULL,\r\n" + " `composition` json NOT NULL,\r\n"
						+ " `error_rate` double NOT NULL,\r\n" + " `test_evaluation_technique` varchar(20) ,\r\n" + " `test_split_technique` varchar(20) ,\r\n" + " `val_evaluation_technique` varchar(20) ,\r\n"
						+ " `val_split_technique` varchar(20) ,\r\n" + " `test_seed` int(11) ,\r\n" + " `val_seed` int(11) ,\r\n" + " PRIMARY KEY (`sample_id`)\r\n" + ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin",
						new ArrayList<>());
			}

		} catch (SQLException e) {
			LOGGER.error("Could not create table for performance samples", e);
		}
	}

	public void addPerformanceSampleToDB(final String benchmarkName, final ComponentInstance componentInstance, final double score) {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("benchmark", benchmarkName);
			ObjectMapper mapper = new ObjectMapper();
			String composition = mapper.writeValueAsString(componentInstance);
			map.put("composition", composition);
			map.put("score", "" + score);
			this.sqlAdapter.insert(LABEL_PERFORMANCE_SAMPLES, map);
		} catch (Exception e) {
			LOGGER.error("An error occurred while storing the performance sample in the database.", e);
		}
	}

	/**
	 * Returns the number of samples for the given benchmark name and pipeline
	 * identifier.
	 *
	 * @param benchmarkName
	 * @param identifier
	 * @return
	 */
	public int getNumSamples(final String benchmarkName, final String identifier) {
		if (!this.performanceInstancesByIdentifier.containsKey(benchmarkName)) {
			return 0;
		}
		if (!this.performanceInstancesByIdentifier.get(benchmarkName).containsKey(identifier)) {
			return 0;
		}

		return this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier).numInstances();
	}

	/**
	 * Returns the number of significant samples for the given benchmark name and
	 * pipeline identifier. Significant means, that
	 *
	 * @param benchmarkName
	 * @param identifier
	 * @return
	 */
	public int getNumSignificantSamples(final String benchmarkName, final String identifier) {
		if (!this.performanceInstancesByIdentifier.containsKey(benchmarkName)) {
			return 0;
		}
		if (!this.performanceInstancesByIdentifier.get(benchmarkName).containsKey(identifier)) {
			return 0;
		}
		Instances instances = this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier);
		int numDistinctValues = 1;
		for (int i = 0; i < instances.numInstances(); i++) {
			for (int j = 0; j < i; j++) {
				boolean allValuesDistinct = true;
				for (int k = 0; k < instances.numAttributes(); k++) {
					if (instances.get(i).value(k) == instances.get(j).value(k)) {
						allValuesDistinct = false;
					}
				}
				if (allValuesDistinct) {
					numDistinctValues++;
				}
			}
		}
		return numDistinctValues;
	}

	public void loadPerformanceSamplesFromDB() {
		if (this.sqlAdapter == null) {
			LOGGER.error("SQLAdapter is not set.");
			throw new IllegalArgumentException("No SQLAdapter set.");
		}
		try {
			ResultSet rs = this.sqlAdapter.getResultsOfQuery("SELECT dataset, composition, error_rate FROM performance_samples_J48");
			ObjectMapper mapper = new ObjectMapper();
			while (rs.next()) {
				String benchmarkName = rs.getString(1);
				String ciString = rs.getString(2);
				if (!benchmarkName.equals("test")) {
					SimpleModule parameterModule = new SimpleModule();
					ParameterDeserializer des = new ParameterDeserializer();
					parameterModule.addDeserializer(Parameter.class, des);

					SimpleModule parameterDomainModule = new SimpleModule();
					ParameterDomainDeserializer parameterDomainDes = new ParameterDomainDeserializer();
					parameterDomainModule.addDeserializer(Dependency.class, parameterDomainDes);

					ComponentInstance composition = mapper.readValue(ciString, ComponentInstance.class);
					double score = rs.getDouble(3);
					this.addPerformanceSample(benchmarkName, composition, score, false);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("An error occurred while trying to access the database", e);
		} catch (IOException e) {
			LOGGER.error("Could not parse json representation.", e);
		}
	}

	/**
	 * Checks whether k samples are available, which are
	 *
	 * @param k
	 * @return
	 */
	public boolean kDistinctAttributeValuesAvailable(final String benchmarkName, final ComponentInstance composition, final int minNum) {
		if (!this.performanceInstancesByIdentifier.containsKey(benchmarkName)) {
			return false;
		}
		String identifier = Util.getComponentNamesOfComposition(composition);
		if (!this.performanceInstancesByIdentifier.get(benchmarkName).containsKey(identifier)) {
			return false;
		}
		Instances instances = this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier);
		if (instances.numInstances() < minNum) {
			return false;
		}
		for (int i = 0; i < instances.numAttributes() - 1; i++) {
			// if the attribute is nominal or string but the number of values is smaller
			// than k, skip it
			if (instances.attribute(i).numValues() > 0 && instances.attribute(i).numValues() < minNum) {
				if (instances.numDistinctValues(i) < instances.attribute(i).numValues()) {
					return false;
				}
			} else if (instances.attribute(i).getUpperNumericBound() <= instances.attribute(i).getLowerNumericBound()) {
				// nothing to do here
			} else if (instances.numDistinctValues(i) < minNum) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether at least k sample are available, that are pairwise distinct in
	 * each of their attribute values.
	 *
	 * @param benchmarkName
	 * @param composition
	 * @param minNum
	 *            strictly positive minimum number of samples
	 * @return
	 */
	public boolean kCompletelyDistinctSamplesAvailable(final String benchmarkName, final ComponentInstance composition, final int minNum) {
		if (!this.performanceInstancesByIdentifier.containsKey(benchmarkName)) {
			return false;
		}
		String identifier = Util.getComponentNamesOfComposition(composition);
		if (!this.performanceInstancesByIdentifier.get(benchmarkName).containsKey(identifier)) {
			return false;
		}
		Instances instances = this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier);
		if (instances.numInstances() == 0) {
			return false;
		}
		if (minNum == 1 && instances.numInstances() > 0) {
			return true;
		}
		int count = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			boolean distinctFromAll = true;
			for (int j = 0; j < i; j++) {
				Instance instance1 = instances.get(i);
				Instance instance2 = instances.get(j);
				for (int k = 0; k < instances.numAttributes() - 1; k++) {
					if ((instances.attribute(k).isNominal() || instances.attribute(k).isString()) && (instances.attribute(k).numValues() < minNum)
							|| instances.attribute(k).getUpperNumericBound() <= instances.attribute(k).getLowerNumericBound()) {
						continue;
					}
					if (instance1.value(k) == instance2.value(k)) {
						distinctFromAll = false;
					}
				}
			}
			if (distinctFromAll) {
				count++;
			}
			if (count >= minNum) {
				return true;
			}
		}
		return false;
	}

	public Instances getPerformanceSamples(final String benchmarkName, final ComponentInstance composition) {
		String identifier = Util.getComponentNamesOfComposition(composition);
		return this.performanceInstancesByIdentifier.get(benchmarkName).get(identifier);
	}

	public Instances createInstancesForPerformanceSamples(final String benchmarkName, final ComponentInstance composition) {
		return this.getPerformanceSamples(benchmarkName, composition);
	}

	public Instances getPerformanceSamplesForIndividualComponent(final String benchmarkName, final Component component) {
		if (this.performanceInstancesIndividualComponents.get(benchmarkName) != null && this.performanceInstancesIndividualComponents.get(benchmarkName).get(component.getName()) != null) {
			return this.performanceInstancesIndividualComponents.get(benchmarkName).get(component.getName());
		}
		return null;
	}

	public int getNumSamplesForComponent(final String benchmarkName, final Component component) {
		if (this.performanceInstancesIndividualComponents.get(benchmarkName) != null && this.performanceInstancesIndividualComponents.get(benchmarkName).get(component.getName()) != null) {
			return this.performanceInstancesIndividualComponents.get(benchmarkName).get(component.getName()).size();
		}
		return 0;
	}

	public void setPerformanceSamples(final Instances instances, final ComponentInstance composition, final String benchmarkName) {
		String identifier = Util.getComponentNamesOfComposition(composition);

		if (!this.performanceInstancesByIdentifier.containsKey(benchmarkName)) {
			HashMap<String, Instances> newMap = new HashMap<>();
			HashMap<String, Instances> newMap2 = new HashMap<>();
			this.performanceInstancesByIdentifier.put(benchmarkName, newMap);
			this.performanceInstancesIndividualComponents.put(benchmarkName, newMap2);

		}

		this.performanceInstancesByIdentifier.get(benchmarkName).put(identifier, instances);
	}
}
