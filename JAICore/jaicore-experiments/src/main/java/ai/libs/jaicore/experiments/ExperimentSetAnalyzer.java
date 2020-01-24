package ai.libs.jaicore.experiments;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.basic.sets.LDSRelationComputer;
import ai.libs.jaicore.basic.sets.RelationComputationProblem;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;
import ai.libs.jaicore.experiments.exceptions.IllegalKeyDescriptorException;
import ai.libs.jaicore.logging.LoggerUtil;

public class ExperimentSetAnalyzer {

	private static final String PROTOCOL_JAVA = "java:";
	private static final String LOGMESSAGE_CREATEINSTANCE = "Create a new instance of {} and ask it for the number of possible values.";

	private Logger logger = LoggerFactory.getLogger(ExperimentSetAnalyzer.class);

	private final IExperimentSetConfig config;

	private List<String> keyFields;
	private Map<String, List<String>> valuesForKeyFieldsInConfig;
	private List<Map<String, String>> possibleKeyCombinations;
	private Map<String, IExperimentKeyGenerator<?>> valueGeneratorsPerKey = new HashMap<>();
	private int numExperimentsTotal;

	public ExperimentSetAnalyzer(final IExperimentSetConfig config) {
		this.config = config;
		this.reloadConfiguration();
	}

	public void reloadConfiguration() {

		/* reload configuration */
		this.config.reload();

		/* erase laze fields */
		this.possibleKeyCombinations = null;
		this.valueGeneratorsPerKey.clear();

		/* update key fields */
		this.keyFields = Collections.unmodifiableList(new ArrayList<>(this.config.getKeyFields()));

		/* create map of possible values for each key field */
		this.numExperimentsTotal = 1;
		this.valuesForKeyFieldsInConfig = new HashMap<>();
		for (String key : this.keyFields) {
			String propertyVals = this.config.removeProperty(key);
			if (propertyVals == null) {
				throw new IllegalArgumentException("Invalid experiment set configuration! No property values defined for key field \"" + key + "\"");
			}
			List<String> vals = Arrays.asList(StringUtil.explode(propertyVals, ",")).stream().map(String::trim).collect(Collectors.toList());
			this.config.setProperty(key, propertyVals);
			this.valuesForKeyFieldsInConfig.put(key, vals);
			try {
				this.numExperimentsTotal *= this.getNumberOfValuesForKey(key);
			} catch (IllegalKeyDescriptorException e) {
				this.logger.error(LoggerUtil.getExceptionInfo(e));
			}
		}
	}

	public boolean isValueForKeyValid(final String key, final String value) throws IllegalKeyDescriptorException {
		if (!this.keyFields.contains(key)) {
			throw new IllegalStateException("Key \"" + key + "\" is not defined in experiment setup.");
		}
		List<String> possibleValues = this.valuesForKeyFieldsInConfig.get(key);
		if (possibleValues.isEmpty()) {
			throw new IllegalStateException("No values specified for key " + key);
		}
		if (!possibleValues.get(0).startsWith(PROTOCOL_JAVA)) {
			return possibleValues.contains(value);
		}
		this.checkThatKeyOnlyAllowsOneValue(key);
		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			this.checkKeyGenerator(c);
			this.logger.trace(LOGMESSAGE_CREATEINSTANCE, c.getName());
			return ((IExperimentKeyGenerator<?>) c.getConstructor().newInstance()).isValueValid(value);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	public boolean isExperimentInLineWithSetup(final Experiment experiment) {

		/* first check on the keys themselves (not the values) */
		Collection<String> additionalKeys = SetUtil.difference(experiment.getValuesOfKeyFields().keySet(), this.keyFields);
		Collection<String> missingKeys = SetUtil.difference(this.keyFields, experiment.getValuesOfKeyFields().keySet());
		if (!additionalKeys.isEmpty() || !missingKeys.isEmpty()) {
			return false;
		}

		/* now check the concrete values */
		for (Entry<String, String> keyEntry : experiment.getValuesOfKeyFields().entrySet()) {
			try {
				if (!this.isValueForKeyValid(keyEntry.getKey(), keyEntry.getValue())) {
					this.logger.debug("Experiment {} seems outdated. The value {} for key {} is not admissible anymore. Consider removing it.", experiment, keyEntry.getKey(), keyEntry.getValue());
					return false;
				}
			} catch (IllegalKeyDescriptorException e) {
				this.logger.debug("Experiment {} seems outdated. The key {} is not defined in the current setup.", experiment, keyEntry.getKey());
				return false;
			}
		}
		return true;
	}

	public List<Map<String, String>> getAllPossibleKeyCombinations() throws IllegalExperimentSetupException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		if (this.possibleKeyCombinations == null) {
			this.logger.debug("Computing all possible experiments.");

			/* build cartesian product over all possible key-value */
			List<List<String>> values = new ArrayList<>();
			for (String key : this.keyFields) {
				if (!this.valuesForKeyFieldsInConfig.containsKey(key)) {
					throw new IllegalStateException("No values for key " + key + " have been defined!");
				}
				List<String> valuesForKey = this.getAllValuesForKey(key);
				this.logger.debug("Retrieving {} values for key {}. Enable TRACE to see all values.", valuesForKey.size(), key);
				this.logger.trace("Values for key {}: {}", key, valuesForKey);
				values.add(valuesForKey);
			}

			/* get constraints */
			List<Predicate<List<String>>> constraints = new ArrayList<>();
			if (this.config.getConstraints() != null) {
				for (String p : this.config.getConstraints()) {
					if (!p.startsWith(PROTOCOL_JAVA)) {
						this.logger.warn("Ignoring constraint {} since currently only java constraints are allowed.", p);
						continue;
					}
					try {
						constraints.add((Predicate<List<String>>) Class.forName(p.substring(PROTOCOL_JAVA.length()).trim()).getConstructor().newInstance());
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						this.logger.error("Error in loading constraint {}: {}", p, e);
					}
				}
			}

			/* create one experiment object from every tuple */
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Building relation from {} cartesian product with {} constraints.", values.stream().map(l -> "" + l.size()).collect(Collectors.joining(" x ")), constraints.size());
			}
			RelationComputationProblem<String> problem = constraints.isEmpty() ? new RelationComputationProblem<>(values) : new RelationComputationProblem<>(values, constraints.get(0));
			LDSRelationComputer<String> lc = new LDSRelationComputer<>(problem);
			lc.setLoggerName(this.logger.getName() + ".relationcomputer");
			List<List<String>> combinationsAsList = lc.call();
			this.logger.info("Obtained {} key combinations. Now building maps from these.", combinationsAsList.size());
			this.possibleKeyCombinations = Collections.unmodifiableList(combinationsAsList.stream().map(c -> Collections.unmodifiableMap(this.mapValuesToKeyValueMap(c))).collect(Collectors.toList()));
		}
		return this.possibleKeyCombinations;
	}

	private Map<String, String> mapValuesToKeyValueMap(final List<String> values) {
		Map<String, String> map = new HashMap<>();
		int i = 0;
		for (String key : this.keyFields) {
			map.put(key, values.get(i++));
		}
		return map;
	}

	public int getNumberOfValuesForKey(final String key) throws IllegalKeyDescriptorException {
		List<String> possibleValues = this.valuesForKeyFieldsInConfig.get(key);
		if (possibleValues.isEmpty()) {
			return 0;
		}
		if (!possibleValues.get(0).startsWith(PROTOCOL_JAVA)) {
			return possibleValues.size();
		}
		this.checkThatKeyOnlyAllowsOneValue(key);

		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			this.checkKeyGenerator(c);
			this.logger.trace(LOGMESSAGE_CREATEINSTANCE, c.getName());
			return ((IExperimentKeyGenerator<?>) c.getConstructor().newInstance()).getNumberOfValues();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	public String getValueForKey(final String key, final int indexOfValue) {
		List<String> possibleValues = this.valuesForKeyFieldsInConfig.get(key);
		if (possibleValues.isEmpty()) {
			throw new IllegalArgumentException("No values specified for key " + key);
		}
		if (!possibleValues.get(0).startsWith(PROTOCOL_JAVA)) {
			return possibleValues.get(indexOfValue);
		}
		this.checkThatKeyOnlyAllowsOneValue(key);

		/* determine the generator for this key if this has not happened before */
		IExperimentKeyGenerator<?> keyGenerator = this.valueGeneratorsPerKey.computeIfAbsent(key, k -> {
			try {
				Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
				this.checkKeyGenerator(c);
				this.logger.trace(LOGMESSAGE_CREATEINSTANCE, c.getName());
				return (IExperimentKeyGenerator<?>) c.getConstructor().newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalKeyDescriptorException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException(e);
			}
		});

		Object value = keyGenerator.getValue(indexOfValue);
		if (value == null) {
			throw new NoSuchElementException("No value could be found for index " + indexOfValue + " in keyfield " + key);
		}
		return value.toString();
	}

	public List<String> getAllValuesForKey(final String key) throws IllegalKeyDescriptorException {
		int n = this.getNumberOfValuesForKey(key);
		List<String> vals = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			vals.add(this.getValueForKey(key, i));
		}
		return Collections.unmodifiableList(vals);
	}

	public int getNumExperimentsTotal() {
		return this.numExperimentsTotal;
	}

	private void checkThatKeyOnlyAllowsOneValue(final String key) {
		if (this.valuesForKeyFieldsInConfig.get(key).size() > 1) {
			throw new UnsupportedOperationException("The value for key " + key + " seems to be a java class, but there are multiple values defined.");
		}
	}

	private void checkKeyGenerator(final Class<?> c) throws IllegalKeyDescriptorException {
		if (!IExperimentKeyGenerator.class.isAssignableFrom(c)) {
			throw new IllegalKeyDescriptorException("The specified class " + c.getName() + " does not implement the " + IExperimentKeyGenerator.class.getName() + " interface.");
		}
	}
}
