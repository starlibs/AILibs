package jaicore.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.StringUtil;
import jaicore.basic.sets.SetUtil;
import jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import jaicore.experiments.exceptions.IllegalExperimentSetupException;
import jaicore.experiments.exceptions.IllegalKeyDescriptorException;

public class ExperimentRunner {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentRunner.class);

	private final IExperimentSetConfig config;
	private final IExperimentSetEvaluator conductor;
	private final IExperimentDatabaseHandle handle;
	private final Collection<ExperimentDBEntry> knownExperimentEntries = new HashSet<>();

	private Map<String, List<String>> valuesForKeyFields = new HashMap<>();
	private int memoryLimit;
	private int cpuLimit;
	private int totalNumberOfExperiments;

	/**
	 * This flag indicates whether the given memory limit deviates from the actually available memory.
	 */
	private boolean condMemoryLimitCheck = false;

	public ExperimentRunner(final IExperimentSetConfig config, final IExperimentSetEvaluator conductor, final IExperimentDatabaseHandle databaseHandle) {

		/* check data base configuration */
		this.config = config;
		if (config.getMemoryLimitInMB() == null) {
			throw new IllegalArgumentException("Memory field (" + IExperimentSetConfig.MEM_MAX + ") must be set in configuration");
		}
		if (config.getNumberOfCPUs() == null) {
			throw new IllegalArgumentException("Max CPU field (" + IExperimentSetConfig.CPU_MAX+ ") must be set in configuration");
		}
		if (config.getKeyFields() == null) {
			throw new IllegalArgumentException("Key fields (" + IExperimentSetConfig.KEYFIELDS + ") entry must be set in configuration!");
		}
		if (config.getResultFields() == null) {
			throw new IllegalArgumentException("Result fields (" + IExperimentSetConfig.RESULTFIELDS + ") entry must be set in configuration!");
		}
		this.conductor = conductor;
		this.handle = databaseHandle;
	}

	private void updateExperimentSetupAccordingToConfig() throws IllegalKeyDescriptorException {
		if (this.condMemoryLimitCheck) {
			this.memoryLimit = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
			if (this.memoryLimit != this.config.getMemoryLimitInMB()) {
				System.err.println("The true memory limit is " + this.memoryLimit + ", which differs from the " + this.config.getMemoryLimitInMB() + " specified in the config! We will write " + this.memoryLimit + " into the database.");
			}
		} else {
			this.memoryLimit = this.config.getMemoryLimitInMB();
		}

		this.cpuLimit = this.config.getNumberOfCPUs();
		int numExperiments = 1;
		try {

			/* create map of possible values for each key field */
			for (String key : this.config.getKeyFields()) {
				/* this is a hack needed because one cannot retrieve generic configs */
				String propertyVals = this.config.removeProperty(key);
				if (propertyVals == null) {
					throw new IllegalArgumentException("No property values defined for key field \"" + key + "\"");
				}
				List<String> vals = Arrays.asList(StringUtil.explode(propertyVals, ",")).stream().map(s -> s.trim()).collect(Collectors.toList());
				this.config.setProperty(key, propertyVals);
				this.valuesForKeyFields.put(key, vals);
				numExperiments *= this.getNumberOfValuesForKey(key);
			}

			this.handle.setup(this.config);
			for (ExperimentDBEntry experiment : this.handle.getConductedExperiments()) {
				if (this.isExperimentInLineWithSetup(experiment.getExperiment())) {
					this.knownExperimentEntries.add(experiment);
				}
				else {
					logger.warn("Experiment with id {} and keys {} seems outdated. The reason can be an illegal key name or an outdated value for one of the keys. Enable DEBUG mode for more details.", experiment.getId(), experiment.getExperiment().getValuesOfKeyFields());
				}
			}

		} catch (ExperimentDBInteractionFailedException e) {
			e.printStackTrace();
			numExperiments = -1;
		}
		this.totalNumberOfExperiments = numExperiments;
	}

	public boolean isExperimentInLineWithSetup(final Experiment experiment) {
		for (Entry<String, String> keyEntry : experiment.getValuesOfKeyFields().entrySet()) {
			try {
				if (!this.isValueForKeyValid(keyEntry.getKey(), keyEntry.getValue())) {
					logger.debug("Experiment {} seems outdated. The value {} for key {} is not admissible anymore. Consider removing it.", experiment, keyEntry.getKey(), keyEntry.getValue());
					return false;
				}
			} catch (IllegalKeyDescriptorException e) {
				logger.debug("Experiment {} seems outdated. The key {} is not defined in the current setup.", experiment, keyEntry.getKey());
				return false;
			}
		}
		return true;
	}

	public ExperimentDBEntry createAndGetExperimentIfNotConducted(final Map<String, String> values) throws ExperimentDBInteractionFailedException {
		return this.handle.createAndGetExperimentIfNotConducted(new Experiment(this.memoryLimit, this.cpuLimit, values));
	}

	public Experiment getExperimentForNumber(final int id) throws IllegalExperimentSetupException {
		logger.debug("Computing experiment for id {}", id);
		if (id < 0) {
			throw new IllegalArgumentException("Experiment ID must be positive!");
		}
		if (id >= this.totalNumberOfExperiments) {
			throw new IllegalArgumentException("Invalid experiment ID " + id + ". Only " + this.totalNumberOfExperiments + " are possible with the given config.");
		}

		/* determine the block sizes for the different iterations */
		Map<String, Integer> blockSizes = new HashMap<>();
		int size = 1;
		List<String> keyOrder = new ArrayList<>(this.config.getKeyFields());
		Collections.reverse(keyOrder);
		for (String key : keyOrder) {
			blockSizes.put(key, size);
			int numValuesForKey = this.getNumberOfValuesForKey(key);
			if (numValuesForKey < 1) {
				throw new IllegalExperimentSetupException("Key \"" + key + "\" has no valid values.");
			}
			logger.trace("Identified {} possible values for key {}", numValuesForKey, key);
			size *= numValuesForKey;
		}

		/* find the correct experiment */
		Map<String, String> keyFieldValues = new HashMap<>();
		int k = id;
		for (String key : this.config.getKeyFields()) {
			int s = blockSizes.get(key);
			int index = (int) Math.floor(k / (s * 1f));
			String value = this.getValueForKey(key, index);
			logger.trace("Determined value {} for key {}", value, key);
			keyFieldValues.put(key, value);
			k = k % s;
		}
		assert SetUtil.differenceEmpty(this.config.getKeyFields(), keyFieldValues.keySet());
		return new Experiment(this.memoryLimit, this.cpuLimit, keyFieldValues);
	}

	private int getNumberOfValuesForKey(final String key) throws IllegalKeyDescriptorException {
		List<String> possibleValues = this.valuesForKeyFields.get(key);
		if (possibleValues.isEmpty()) {
			return 0;
		}
		if (!possibleValues.get(0).startsWith("java:")) {
			return possibleValues.size();
		}
		if (possibleValues.size() > 1) {
			throw new UnsupportedOperationException("The value for key " + key + " seems to be a java class, but there are multiple values defined.");
		}
		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			if (!IExperimentKeyGenerator.class.isAssignableFrom(c)) {
				throw new IllegalKeyDescriptorException("The specified class " + c.getName() + " does not implement the " + IExperimentKeyGenerator.class.getName() + " interface.");
			}
			logger.trace("Create a new instance of {} and ask it for the number of possible values.", c.getName());
			return ((IExperimentKeyGenerator<?>)c.newInstance()).getNumberOfValues();
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	private String getValueForKey(final String key, final int indexOfValue) throws IllegalKeyDescriptorException {
		List<String> possibleValues = this.valuesForKeyFields.get(key);
		assert !possibleValues.isEmpty() : "No values specified for key " + key;
		if (!possibleValues.get(0).startsWith("java:")) {
			return possibleValues.get(indexOfValue);
		}
		if (possibleValues.size() > 1) {
			throw new UnsupportedOperationException("The value for key " + key + " seems to be a java class, but there are multiple values defined.");
		}
		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			if (!IExperimentKeyGenerator.class.isAssignableFrom(c)) {
				throw new IllegalKeyDescriptorException("The specified class " + c.getName() + " does not implement the " + IExperimentKeyGenerator.class.getName() + " interface.");
			}
			logger.trace("Create a new instance of {} and ask it for the number of possible values.", c.getName());
			Object value = ((IExperimentKeyGenerator<?>)c.newInstance()).getValue(indexOfValue);
			if (value == null) {
				throw new NoSuchElementException("No value could be found for index " + indexOfValue + " in keyfield " + key);
			}
			return value.toString();
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	private boolean isValueForKeyValid(final String key, final String value) throws IllegalKeyDescriptorException {
		List<String> possibleValues = this.valuesForKeyFields.get(key);
		assert !possibleValues.isEmpty() : "No values specified for key " + key;
		if (!possibleValues.get(0).startsWith("java:")) {
			return possibleValues.contains(value);
		}
		if (possibleValues.size() > 1) {
			throw new UnsupportedOperationException("The value for key " + key + " seems to be a java class, but there are multiple values defined.");
		}
		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			if (!IExperimentKeyGenerator.class.isAssignableFrom(c)) {
				throw new IllegalKeyDescriptorException("The specified class " + c.getName() + " does not implement the " + IExperimentKeyGenerator.class.getName() + " interface.");
			}
			logger.trace("Create a new instance of {} and ask it for the number of possible values.", c.getName());
			return ((IExperimentKeyGenerator<?>)c.newInstance()).isValueValid(value);
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	public void randomlyConductExperiments(final int maxNumberOfExperiments, final boolean reload) throws IllegalKeyDescriptorException {
		this.updateExperimentSetupAccordingToConfig();

		if (this.totalNumberOfExperiments <= 0) {
			System.out.println("Number of total experiments is 0");
			return;
		}

		logger.info("Now conducting new experiment. {}/{} experiments have already been started or even been completed", this.knownExperimentEntries.size(), this.totalNumberOfExperiments);

		int numberOfConductedExperiments = 0;
		while (!Thread.interrupted() && this.knownExperimentEntries.size() < this.totalNumberOfExperiments && ((maxNumberOfExperiments > 0) ? numberOfConductedExperiments < maxNumberOfExperiments : true)) {
			if (reload) {
				this.config.reload();
			}
			this.updateExperimentSetupAccordingToConfig();
			int k = (int) Math.floor(Math.random() * this.totalNumberOfExperiments);
			logger.info("Now conducting {}/{}", k, this.totalNumberOfExperiments);
			try {
				Experiment exp = this.getExperimentForNumber(k);
				this.checkExperimentValidity(exp);
				logger.info("Conduct experiment with key values: {}", exp.getValuesOfKeyFields());
				if (this.conductExperimentIfNotAlreadyConducted(exp)) {
					numberOfConductedExperiments++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void randomlyConductExperiments(final boolean reload) throws IllegalKeyDescriptorException {
		this.randomlyConductExperiments(-1, reload);
	}

	/**
	 * Conducts a single experiment
	 *
	 * @param exp
	 * @throws Exception.
	 *             These are not the exceptions thrown by the experiment itself, because these are logged into the database. Exceptions thrown here are technical exceptions that occur when arranging the experiment
	 */
	public boolean conductExperimentIfNotAlreadyConducted(final Experiment exp) throws Exception {

		ExperimentDBEntry expEntry = this.handle.createAndGetExperimentIfNotConducted(exp);
		if (expEntry != null) {
			Exception error = null;
			this.knownExperimentEntries.add(expEntry);
			try {
				this.conductor.evaluate(expEntry, m -> {
					try {
						this.handle.updateExperiment(expEntry, m);
					} catch (ExperimentDBInteractionFailedException  e) {
						e.printStackTrace();
					}
				});

			} catch (Exception e) {
				error = e;
				System.err.println("Experiment failed due to exception, which has been logged");
			}
			this.handle.finishExperiment(expEntry, error);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method can be used to toggle the check of the memory limit. If the check is active (default or doCheck == true), the memory actually available in this runtime will be taken as an entry for the memory limit.
	 *
	 * @param doCheck
	 *            A flag whether the check shall be performed or not. If doCheck is true, the check will be performed and the actually available memory in this runtime environment will be written to the database. Otherwise, the memory limit
	 *            specified in the configuration file will be written to the database.
	 */
	public void setConditionMemoryLimitCheck(final boolean doCheck) {
		this.condMemoryLimitCheck = doCheck;
	}

	private void checkExperimentValidity(final Experiment experiment) {
		if (SetUtil.differenceNotEmpty(this.config.getKeyFields(), experiment.getValuesOfKeyFields().keySet())) {
			throw new IllegalArgumentException("The experiment " + experiment + " is invalid, because key fields have not been defined: " + SetUtil.difference(this.config.getKeyFields(), experiment.getValuesOfKeyFields().keySet()));
		}
	}
}
