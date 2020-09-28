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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.basic.sets.LDSRelationComputer;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.RelationComputationProblem;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;
import ai.libs.jaicore.experiments.exceptions.IllegalKeyDescriptorException;
import ai.libs.jaicore.logging.LoggerUtil;

public class ExperimentSetAnalyzer {

	private static final String PROTOCOL_JAVA = "java:";
	private static final String LOGMESSAGE_CREATEINSTANCE = "Create a new instance of {} and ask it for the number of possible values.";


	/**
	 * The ThreadLocal variable holds a unique instance of ScriptEngine for each thread that requests it.
	 * We use a ThreadLocal variable instead of a local variable to speed up the creation of the ScriptEngine.
	 * We use a ThreadLocal variable instead of a (static) instance variable because ScriptEngine generally isn't threadsafe
	 * and can cause problems if multiple threads operate on it.
	 */
	private static final ThreadLocal<ScriptEngine> scriptEngine = ThreadLocal.withInitial(() -> {
		ScriptEngineManager mgr = new ScriptEngineManager();
		return mgr.getEngineByName("JavaScript");
	});

	private final Logger logger = LoggerFactory.getLogger(ExperimentSetAnalyzer.class);

	private final IExperimentSetConfig config;

	private List<String> keyFields;
	private Map<String, List<String>> valuesForKeyFieldsInConfig;
	private List<Map<String, String>> possibleKeyCombinations;
	private Map<String, IExperimentKeyGenerator<?>> valueGeneratorsPerKey = new HashMap<>();
	private int numExperimentsTotal;

	public ExperimentSetAnalyzer(final IExperimentSetConfig config) {
		this.config = config;
		this.reloadConfiguration();
		scriptEngine.remove();
	}

	public void reloadConfiguration() {

		/* reload configuration */
		//FIXME this reload yields empty config object.
		//		this.config.reload();

		/* erase laze fields */
		synchronized (this.config) {
			this.possibleKeyCombinations = null;
			this.valueGeneratorsPerKey.clear();

			/* update key fields */
			this.keyFields = Collections.unmodifiableList(this.config.getKeyFields().stream().map(k -> this.getNameTypeSplitForAttribute(k).getX()).collect(Collectors.toList()));

			/* create map of possible values for each key field */
			this.numExperimentsTotal = 1;
			this.valuesForKeyFieldsInConfig = new HashMap<>();
			for (String key: this.keyFields) {
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
			Class<?> c = Class.forName(possibleValues.get(0).substring(PROTOCOL_JAVA.length()).trim());
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

	public List<List<String>> getAllPossibleKeyCombinationsAsList() throws IllegalExperimentSetupException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		this.logger.debug("Computing all possible experiments.");

		/* build cartesian product over all possible key-values */
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
		List<List<String>> valuesSortedByConstraintRelevance;
		List<Predicate<List<String>>> constraints = new ArrayList<>();
		List<Integer> permutation;
		if (this.config.getConstraints() != null) {

			List<String> constraintsToParse = new ArrayList<>();
			Map<String, Integer> constraintScorePerKeyword = new HashMap<>();
			this.keyFields.forEach(k -> constraintScorePerKeyword.put(k, 0));

			for (String p : this.config.getConstraints()) {
				if (p.startsWith(PROTOCOL_JAVA)) {
					try {
						constraints.add((Predicate<List<String>>) Class.forName(p.substring(PROTOCOL_JAVA.length()).trim()).getConstructor().newInstance());
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						this.logger.error("Error in loading constraint {}: {}", p, LoggerUtil.getExceptionInfo(e));
					}
				}
				else {
					this.logger.info("Parsing constraint {}", p);
					constraintsToParse.add(p);
					for (String keyword : this.keyFields) {
						if (p.contains(keyword)) {
							constraintScorePerKeyword.put(keyword, constraintScorePerKeyword.get(keyword) + 1);
						}
					}
				}
			}
			final List<String> keyFieldsSortedByConstraintRelevance = this.keyFields.stream().sorted((f1, f2) -> Integer.compare(constraintScorePerKeyword.get(f2), constraintScorePerKeyword.get(f1))).collect(Collectors.toList());
			permutation = SetUtil.getPermutation(this.keyFields, keyFieldsSortedByConstraintRelevance);
			valuesSortedByConstraintRelevance = SetUtil.applyPermutation(values, permutation);

			/* now create predicates from the constraints that require parsing */
			for (String p : constraintsToParse) {
				Predicate<List<String>> predicate = new Predicate<List<String>>() {

					private final int highestRequiredIndex = keyFieldsSortedByConstraintRelevance.stream().filter(p::contains).map(keyFieldsSortedByConstraintRelevance::indexOf).max(Integer::compare).get();

					@Override
					public boolean test(final List<String> t) {
						String evaluatedConstraint = p;
						int n = t.size(); // n is the number of key fields considered in t (grows over time)
						if (n <= this.highestRequiredIndex || n > this.highestRequiredIndex + 1) { // only apply the predicate for tuples of EXACTLY the length of the highest required variable (we assume that any larger tuple has already been tested implictly by a previously requested sub-tuple)
							return true;
						}
						for (int i = 0; i < n; i++) {
							evaluatedConstraint = evaluatedConstraint.replace(keyFieldsSortedByConstraintRelevance.get(i), t.get(i));
						}
						try {
							ScriptEngine engine = scriptEngine.get();
							Object evaluation = engine.eval(evaluatedConstraint);
							if(evaluation instanceof Boolean) {
								return (boolean) evaluation;
							} else {
								ExperimentSetAnalyzer.this.logger.error("The evaluation of constraint={} did not return a boolean but instead: {}. Predicate falls back to `false`."
										+ " \nThe original constraint is: {}",
										evaluatedConstraint, evaluation, p);
								return false;
							}
						} catch (ScriptException e) {
							ExperimentSetAnalyzer.this.logger.error(LoggerUtil.getExceptionInfo(e));
							return false;
						}
					}
				};
				constraints.add(predicate);
			}

		}
		else {
			valuesSortedByConstraintRelevance = values;
			permutation = null;
		}
		Predicate<List<String>> jointConstraints = t -> {
			for (Predicate<List<String>> c : constraints) {
				if (!c.test(t)) {
					return false;
				}
			}
			return true;
		};

		/* create one experiment object from every tuple */
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Building relation from {} cartesian product with {} constraints.", values.stream().map(l -> "" + l.size()).collect(Collectors.joining(" x ")), constraints.size());
		}
		RelationComputationProblem<String> problem = constraints.isEmpty() ? new RelationComputationProblem<>(values) : new RelationComputationProblem<>(valuesSortedByConstraintRelevance, jointConstraints);
		LDSRelationComputer<String> lc = new LDSRelationComputer<>(problem);
		lc.setLoggerName(this.logger.getName() + ".relationcomputer");
		List<List<String>> entries = lc.call();
		return permutation != null ? entries.stream().map(e -> SetUtil.applyInvertedPermutation(e, permutation)).collect(Collectors.toList()) : entries;
	}

	public List<Map<String, String>> getAllPossibleKeyCombinations() throws IllegalExperimentSetupException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		if (this.possibleKeyCombinations == null) {
			List<List<String>> combinationsAsList = this.getAllPossibleKeyCombinationsAsList();
			this.logger.info("Obtained {} key combinations. Now building maps from these.", combinationsAsList.size());
			this.possibleKeyCombinations = this.mapListTuplesToKeyValueMap(combinationsAsList);
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

	public List<Map<String, String>> mapListTuplesToKeyValueMap(final Collection<List<String>> tuples) {
		return Collections.unmodifiableList(tuples.stream().map(c -> Collections.unmodifiableMap(this.mapValuesToKeyValueMap(c))).collect(Collectors.toList()));
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
			Class<?> c = Class.forName(possibleValues.get(0).substring(PROTOCOL_JAVA.length()).trim());
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
				Class<?> c = Class.forName(possibleValues.get(0).substring(PROTOCOL_JAVA.length()).trim());
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

	public Pair<String, String> getNameTypeSplitForAttribute(final String name) {
		String[] parts = name.split(":");
		String type = parts.length == 2 ? parts[1] : null;
		return new Pair<>(parts[0], type);
	}

}
