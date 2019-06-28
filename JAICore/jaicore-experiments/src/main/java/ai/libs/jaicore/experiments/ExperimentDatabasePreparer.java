package ai.libs.jaicore.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.ILoggingCustomizable;
import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import ai.libs.jaicore.basic.sets.LDSRelationComputer;
import ai.libs.jaicore.basic.sets.RelationComputationProblem;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;
import ai.libs.jaicore.experiments.exceptions.IllegalKeyDescriptorException;

public class ExperimentDatabasePreparer implements ILoggingCustomizable {
	private Logger logger = LoggerFactory.getLogger(ExperimentDatabasePreparer.class);

	private static final String PROTOCOL_JAVA = "java:";
	private static final int MAX_MEM_DEVIATION = 50;

	private static final String LOGMESSAGE_CREATEINSTANCE = "Create a new instance of {} and ask it for the number of possible values.";

	private final IExperimentSetConfig config;
	private final IExperimentDatabaseHandle handle;
	private final Collection<Map<String, String>> keysForWhichResultsAreKnown = new HashSet<>();

	private List<Map<String, String>> possibleKeyCombinations;

	private final String[] keyFields;

	private final Map<String, IExperimentKeyGenerator<?>> valueGeneratorsPerKey = new HashMap<>();
	private final Map<String, List<String>> valuesForKeyFieldsInConfig = new HashMap<>();
	private int memoryLimit;
	private int cpuLimit;
	private int totalNumberOfExperiments;

	/**
	 * This flag indicates whether the given memory limit deviates from the actually
	 * available memory.
	 */
	private boolean condMemoryLimitCheck = false;

	public ExperimentDatabasePreparer(final IExperimentSetConfig config, final IExperimentDatabaseHandle databaseHandle) throws ExperimentDBInteractionFailedException {

		/* check data base configuration */
		this.config = config;
		this.keyFields = config.getKeyFields().toArray(new String[config.getKeyFields().size()]);
		if (config.getMemoryLimitInMB() == null) {
			throw new IllegalArgumentException("Memory field (" + IExperimentSetConfig.MEM_MAX + ") must be set in configuration");
		}
		if (config.getNumberOfCPUs() == null) {
			throw new IllegalArgumentException("Max CPU field (" + IExperimentSetConfig.CPU_MAX + ") must be set in configuration");
		}
		if (config.getKeyFields() == null) {
			throw new IllegalArgumentException("Key fields (" + IExperimentSetConfig.KEYFIELDS + ") entry must be set in configuration!");
		}
		if (config.getResultFields() == null) {
			throw new IllegalArgumentException("Result fields (" + IExperimentSetConfig.RESULTFIELDS + ") entry must be set in configuration!");
		}
		this.handle = databaseHandle;
		this.logger.debug("Created ExperimentRunner. Now updating its configuration from the database.");
		this.updateExperimentSetupAccordingToConfigFromDatabase();
		this.logger.info("Successfully created and initialized ExperimentRunner.");
	}

	/**
	 * Creates all experiments in the database that should exist with respect to the configuration but have not been created yet.
	 *
	 * @return
	 * @throws ExperimentDBInteractionFailedException
	 * @throws IllegalExperimentSetupException
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 * @throws AlgorithmTimeoutedException
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 */
	public List<ExperimentDBEntry> synchronizeExperiments() throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {

		/* compute all experiments */
		List<Map<String, String>> tmpPossibleKeyCombinations = this.getAllPossibleKeyCombinations();
		this.logger.debug("Determined {} possible combinations. Will now remove keys that are already contained.", tmpPossibleKeyCombinations.size());
		int removed = 0;
		for (ExperimentDBEntry experiment : this.handle.getAllExperiments()) {
			tmpPossibleKeyCombinations.remove(experiment.getExperiment().getValuesOfKeyFields());
			removed ++;
		}
		this.logger.debug("{} experiments already exist. Number of experiments that will be created now is {}.", removed, tmpPossibleKeyCombinations.size());
		if (tmpPossibleKeyCombinations.isEmpty()) {
			return new ArrayList<>(0);
		}

		/* create experiments */
		List<ExperimentDBEntry> entries = this.handle.createAndGetExperiments(tmpPossibleKeyCombinations.stream().map(t -> new Experiment(this.memoryLimit, this.cpuLimit, t)).collect(Collectors.toList()));
		this.logger.info("Ids of {} inserted entries: {}", entries.size(), entries.stream().map(ExperimentDBEntry::getId).collect(Collectors.toList()));
		return entries;
	}

	private void updateExperimentSetupAccordingToConfigFromDatabase() throws ExperimentDBInteractionFailedException {
		if (this.condMemoryLimitCheck) {
			if (Math.abs((int) (Runtime.getRuntime().maxMemory() / 1024 / 1024) - this.config.getMemoryLimitInMB()) > MAX_MEM_DEVIATION) {
				this.logger.error("The true memory limit is {}, which differs from the {} specified in the config by more than the allowed {}MB!", this.memoryLimit, this.config.getMemoryLimitInMB(), MAX_MEM_DEVIATION);
			}
		} else {
			this.memoryLimit = this.config.getMemoryLimitInMB();
		}
		this.cpuLimit = this.config.getNumberOfCPUs();

		/* create map of possible values for each key field */
		this.handle.setup(this.config);
		this.logger.debug("Reading all experiments from database.");
		this.totalNumberOfExperiments = this.handle.getNumberOfAllExperiments();

		this.logger.debug("Identified {} experiments. Now deriving the possible values for each key from the experiments.", this.totalNumberOfExperiments);
		for (String key : this.keyFields) {
			String propertyVals = this.config.removeProperty(key);
			if (propertyVals == null) {
				throw new IllegalArgumentException("No property values defined for key field \"" + key + "\"");
			}
			List<String> vals = Arrays.asList(StringUtil.explode(propertyVals, ",")).stream().map(String::trim).collect(Collectors.toList());
			this.config.setProperty(key, propertyVals);
			this.valuesForKeyFieldsInConfig.put(key, vals);
		}

		for (ExperimentDBEntry experiment : this.handle.getConductedExperiments()) {
			if (this.isExperimentInLineWithSetup(experiment.getExperiment())) {
				this.keysForWhichResultsAreKnown.add(experiment.getExperiment().getValuesOfKeyFields());
			} else {
				this.logger.warn("Experiment with id {} and keys {} seems outdated. The reason can be an illegal key name or an outdated value for one of the keys. Enable DEBUG mode for more details.", experiment.getId(),
						experiment.getExperiment().getValuesOfKeyFields());
			}
		}
	}

	private void updateExperimentSetupAccordingToConfigFromScratch() throws IllegalKeyDescriptorException, ExperimentDBInteractionFailedException {
		if (this.condMemoryLimitCheck) {
			if (Math.abs((int) (Runtime.getRuntime().maxMemory() / 1024 / 1024) - this.config.getMemoryLimitInMB()) > MAX_MEM_DEVIATION) {
				this.logger.error("The true memory limit is {}, which differs from the {} specified in the config by more than the allowed {}MB!", this.memoryLimit, this.config.getMemoryLimitInMB(), MAX_MEM_DEVIATION);
			}
		} else {
			this.memoryLimit = this.config.getMemoryLimitInMB();
		}
		this.cpuLimit = this.config.getNumberOfCPUs();
		int numExperiments = 1;

		/* create map of possible values for each key field */
		for (String key : this.keyFields) {

			/* this is a hack needed because one cannot retrieve generic configs */
			String propertyVals = this.config.removeProperty(key);
			if (propertyVals == null) {
				throw new IllegalArgumentException("No property values defined for key field \"" + key + "\"");
			}
			List<String> vals = Arrays.asList(StringUtil.explode(propertyVals, ",")).stream().map(String::trim).collect(Collectors.toList());
			this.config.setProperty(key, propertyVals);
			this.valuesForKeyFieldsInConfig.put(key, vals);
			numExperiments *= this.getNumberOfValuesForKey(key);
		}

		this.handle.setup(this.config);
		for (ExperimentDBEntry experiment : this.handle.getConductedExperiments()) {
			if (this.isExperimentInLineWithSetup(experiment.getExperiment())) {
				this.keysForWhichResultsAreKnown.add(experiment.getExperiment().getValuesOfKeyFields());
			} else {
				this.logger.warn("Experiment with id {} and keys {} seems outdated. The reason can be an illegal key name or an outdated value for one of the keys. Enable DEBUG mode for more details.", experiment.getId(),
						experiment.getExperiment().getValuesOfKeyFields());
			}
		}
		this.totalNumberOfExperiments = numExperiments;
	}

	private boolean isExperimentInLineWithSetup(final Experiment experiment) {
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

	public List<Map<String, String>> getAllPossibleKeyCombinations() throws IllegalExperimentSetupException, ExperimentDBInteractionFailedException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		if (this.possibleKeyCombinations == null) {
			this.logger.debug("Computing all possible experiments.");
			this.updateExperimentSetupAccordingToConfigFromScratch();

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
						constraints.add((Predicate<List<String>>)Class.forName(p.substring(PROTOCOL_JAVA.length()).trim()).newInstance());
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
						this.logger.error("Error in loading constraint {}: {}", p, e);
					}
				}
			}

			/* create one experiment object from every tuple */
			this.logger.debug("Building relation from {} cartesian product with {} constraints.", values.stream().map(l -> "" + l.size()).collect(Collectors.joining(" x ")), constraints.size());
			RelationComputationProblem<String> problem = constraints.isEmpty() ? new RelationComputationProblem<>(values) : new RelationComputationProblem<>(values, constraints.get(0));
			LDSRelationComputer<String> lc = new LDSRelationComputer<>(problem);
			lc.setLoggerName(this.getLoggerName() + ".relationcomputer");
			List<List<String>> combinationsAsList = lc.call();
			this.logger.info("Obtained {} key combinations. Now building maps from these.", combinationsAsList.size());
			this.possibleKeyCombinations = combinationsAsList.stream().map(this::mapValuesToKeyValueMap).collect(Collectors.toList());
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

	private void checkUniquenessOfKey(final String key) {
		if (this.valuesForKeyFieldsInConfig.get(key).size() > 1) {
			throw new UnsupportedOperationException("The value for key " + key + " seems to be a java class, but there are multiple values defined.");
		}
	}

	private void checkKeyGenerator(final Class<?> c) throws IllegalKeyDescriptorException {
		if (!IExperimentKeyGenerator.class.isAssignableFrom(c)) {
			throw new IllegalKeyDescriptorException("The specified class " + c.getName() + " does not implement the " + IExperimentKeyGenerator.class.getName() + " interface.");
		}
	}

	private int getNumberOfValuesForKey(final String key) throws IllegalKeyDescriptorException {
		List<String> possibleValues = this.valuesForKeyFieldsInConfig.get(key);
		if (possibleValues.isEmpty()) {
			return 0;
		}
		if (!possibleValues.get(0).startsWith(PROTOCOL_JAVA)) {
			return possibleValues.size();
		}
		this.checkUniquenessOfKey(key);

		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			this.checkKeyGenerator(c);
			this.logger.trace(LOGMESSAGE_CREATEINSTANCE, c.getName());
			return ((IExperimentKeyGenerator<?>) c.newInstance()).getNumberOfValues();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	private String getValueForKey(final String key, final int indexOfValue) {
		List<String> possibleValues = this.valuesForKeyFieldsInConfig.get(key);
		assert !possibleValues.isEmpty() : "No values specified for key " + key;
		if (!possibleValues.get(0).startsWith(PROTOCOL_JAVA)) {
			return possibleValues.get(indexOfValue);
		}
		this.checkUniquenessOfKey(key);

		/* determine the generator for this key if this has not happened before */
		IExperimentKeyGenerator<?> keyGenerator = this.valueGeneratorsPerKey.computeIfAbsent(key, k -> {
			try {
				Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
				this.checkKeyGenerator(c);
				this.logger.trace(LOGMESSAGE_CREATEINSTANCE, c.getName());
				return (IExperimentKeyGenerator<?>) c.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalKeyDescriptorException e) {
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
		return vals;
	}

	private boolean isValueForKeyValid(final String key, final String value) throws IllegalKeyDescriptorException {
		List<String> possibleValues = this.valuesForKeyFieldsInConfig.get(key);
		assert !possibleValues.isEmpty() : "No values specified for key " + key;
		if (!possibleValues.get(0).startsWith(PROTOCOL_JAVA)) {
			return possibleValues.contains(value);
		}
		this.checkUniquenessOfKey(key);
		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			this.checkKeyGenerator(c);
			this.logger.trace(LOGMESSAGE_CREATEINSTANCE, c.getName());
			return ((IExperimentKeyGenerator<?>) c.newInstance()).isValueValid(value);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	/**
	 * This method can be used to toggle the check of the memory limit. If the check
	 * is active (default or doCheck == true), the memory actually available in this
	 * runtime will be taken as an entry for the memory limit.
	 *
	 * @param doCheck
	 *            A flag whether the check shall be performed or not. If doCheck is
	 *            true, the check will be performed and the actually available
	 *            memory in this runtime environment will be written to the
	 *            database. Otherwise, the memory limit specified in the
	 *            configuration file will be written to the database.
	 */
	public void setConditionMemoryLimitCheck(final boolean doCheck) {
		this.condMemoryLimitCheck = doCheck;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.handle instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.handle).setLoggerName(name + ".handle");
		}
	}

}
