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
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.StringUtil;
import jaicore.basic.sets.SetUtil;
import jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import jaicore.experiments.exceptions.ExperimentUpdateFailedException;
import jaicore.experiments.exceptions.IllegalExperimentSetupException;
import jaicore.experiments.exceptions.IllegalKeyDescriptorException;

/**
 * This class is used to run experiments.
 * 
 * @author fmohr
 *
 */
public class ExperimentRunner {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentRunner.class);

	private static final String PROTOCOL_JAVA = "java:";
	private static final int MAX_MEM_DEVIATION = 50;
	
	private static final String LOGMESSAGE_CREATEINSTANCE = "Create a new instance of {} and ask it for the number of possible values.";

	private final IExperimentSetConfig config;
	private final IExperimentSetEvaluator conductor;
	private final IExperimentDatabaseHandle handle;
	private final Collection<ExperimentDBEntry> knownExperimentEntries = new HashSet<>();

	private Map<String, List<String>> valuesForKeyFields = new HashMap<>();
	private int memoryLimit;
	private int cpuLimit;
	private int totalNumberOfExperiments;
	private final Random random = new Random(System.currentTimeMillis());

	/**
	 * This flag indicates whether the given memory limit deviates from the actually
	 * available memory.
	 */
	private boolean condMemoryLimitCheck = false;

	public ExperimentRunner(final IExperimentSetConfig config, final IExperimentSetEvaluator conductor, final IExperimentDatabaseHandle databaseHandle) {

		/* check data base configuration */
		this.config = config;
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
		this.conductor = conductor;
		this.handle = databaseHandle;
	}

	private void updateExperimentSetupAccordingToConfig() throws IllegalKeyDescriptorException, ExperimentDBInteractionFailedException {
		if (this.condMemoryLimitCheck) {
			if (Math.abs((int) (Runtime.getRuntime().maxMemory() / 1024 / 1024) - this.config.getMemoryLimitInMB()) > MAX_MEM_DEVIATION) {
				logger.error("The true memory limit is {}, which differs from the {} specified in the config by more than the allowed {}MB!", this.memoryLimit, this.config.getMemoryLimitInMB(), MAX_MEM_DEVIATION);
			}
		} else {
			this.memoryLimit = this.config.getMemoryLimitInMB();
		}
		this.cpuLimit = this.config.getNumberOfCPUs();
		int numExperiments = 1;

		/* create map of possible values for each key field */
		for (String key : this.config.getKeyFields()) {
			/* this is a hack needed because one cannot retrieve generic configs */
			String propertyVals = this.config.removeProperty(key);
			if (propertyVals == null) {
				throw new IllegalArgumentException("No property values defined for key field \"" + key + "\"");
			}
			List<String> vals = Arrays.asList(StringUtil.explode(propertyVals, ",")).stream().map(String::trim).collect(Collectors.toList());
			this.config.setProperty(key, propertyVals);
			this.valuesForKeyFields.put(key, vals);
			numExperiments *= this.getNumberOfValuesForKey(key);
		}

		this.handle.setup(this.config);
		for (ExperimentDBEntry experiment : this.handle.getConductedExperiments()) {
			if (this.isExperimentInLineWithSetup(experiment.getExperiment())) {
				this.knownExperimentEntries.add(experiment);
			} else {
				logger.warn("Experiment with id {} and keys {} seems outdated. The reason can be an illegal key name or an outdated value for one of the keys. Enable DEBUG mode for more details.", experiment.getId(),
						experiment.getExperiment().getValuesOfKeyFields());
			}
		}
		this.totalNumberOfExperiments = numExperiments;
	}

	private boolean isExperimentInLineWithSetup(final Experiment experiment) {
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

	private Experiment getExperimentForNumber(final int id) throws IllegalExperimentSetupException {
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
	
	private void checkUniquenessOfKey(String key) {
		if (this.valuesForKeyFields.get(key).size() > 1) {
			throw new UnsupportedOperationException("The value for key " + key + " seems to be a java class, but there are multiple values defined.");
		}
	}
	
	private void checkKeyGenerator(Class<?> c) throws IllegalKeyDescriptorException {
		if (!IExperimentKeyGenerator.class.isAssignableFrom(c)) {
			throw new IllegalKeyDescriptorException("The specified class " + c.getName() + " does not implement the " + IExperimentKeyGenerator.class.getName() + " interface.");
		}
	}

	private int getNumberOfValuesForKey(final String key) throws IllegalKeyDescriptorException {
		List<String> possibleValues = this.valuesForKeyFields.get(key);
		if (possibleValues.isEmpty()) {
			return 0;
		}
		if (!possibleValues.get(0).startsWith(PROTOCOL_JAVA)) {
			return possibleValues.size();
		}
		checkUniquenessOfKey(key);
		
		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			checkKeyGenerator(c);
			logger.trace(LOGMESSAGE_CREATEINSTANCE, c.getName());
			return ((IExperimentKeyGenerator<?>) c.newInstance()).getNumberOfValues();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	private String getValueForKey(final String key, final int indexOfValue) throws IllegalKeyDescriptorException {
		List<String> possibleValues = this.valuesForKeyFields.get(key);
		assert !possibleValues.isEmpty() : "No values specified for key " + key;
		if (!possibleValues.get(0).startsWith(PROTOCOL_JAVA)) {
			return possibleValues.get(indexOfValue);
		}
		checkUniquenessOfKey(key);
		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			checkKeyGenerator(c);
			logger.trace(LOGMESSAGE_CREATEINSTANCE, c.getName());
			Object value = ((IExperimentKeyGenerator<?>) c.newInstance()).getValue(indexOfValue);
			if (value == null) {
				throw new NoSuchElementException("No value could be found for index " + indexOfValue + " in keyfield " + key);
			}
			return value.toString();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	private boolean isValueForKeyValid(final String key, final String value) throws IllegalKeyDescriptorException {
		List<String> possibleValues = this.valuesForKeyFields.get(key);
		assert !possibleValues.isEmpty() : "No values specified for key " + key;
		if (!possibleValues.get(0).startsWith(PROTOCOL_JAVA)) {
			return possibleValues.contains(value);
		}
		checkUniquenessOfKey(key);
		try {
			Class<?> c = Class.forName(possibleValues.get(0).substring(5).trim());
			checkKeyGenerator(c);
			logger.trace(LOGMESSAGE_CREATEINSTANCE, c.getName());
			return ((IExperimentKeyGenerator<?>) c.newInstance()).isValueValid(value);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalKeyDescriptorException(e);
		}
	}

	/**
	 * Conducts a limited number of not yet conducted experiments randomly chosen
	 * from the grid.
	 * 
	 * @param maxNumberOfExperiments
	 *            Limit for the number of experiments
	 * @param reload
	 *            Whether or not the experiment setup should be reloaded between two
	 *            experiment runs.
	 * @throws ExperimentDBInteractionFailedException
	 * @throws IllegalExperimentSetupException
	 */
	public void randomlyConductExperiments(final int maxNumberOfExperiments, final boolean reload) throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException {
		this.updateExperimentSetupAccordingToConfig();

		if (this.totalNumberOfExperiments <= 0) {
			logger.info("Number of total experiments is 0");
			return;
		}
		logger.info("Now conducting new experiment. {}/{} experiments have already been started or even been completed", this.knownExperimentEntries.size(), this.totalNumberOfExperiments);

		int numberOfConductedExperiments = 0;
		while (!Thread.interrupted() && this.knownExperimentEntries.size() < this.totalNumberOfExperiments && numberOfConductedExperiments < maxNumberOfExperiments) {
			if (reload) {
				this.config.reload();
			}
			this.updateExperimentSetupAccordingToConfig();
			int k = random.nextInt(this.totalNumberOfExperiments);
			logger.info("Now conducting {}/{}", k, this.totalNumberOfExperiments);
			Experiment exp = this.getExperimentForNumber(k);
			this.checkExperimentValidity(exp);
			logger.info("Conduct experiment with key values: {}", exp.getValuesOfKeyFields());
			if (this.conductExperimentIfNotAlreadyConducted(exp)) {
				numberOfConductedExperiments++;
			}
		}

	}

	/**
	 * Conducts an unbound number of randomly chosen experiments from the grid.
	 * 
	 * @param reload
	 *            Whether or not the experiment setup should be reloaded between two
	 *            experiment runs.
	 * @throws IllegalExperimentSetupException
	 * @throws ExperimentDBInteractionFailedException
	 */
	public void randomlyConductExperiments(final boolean reload) throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException {
		this.randomlyConductExperiments(-1, reload);
	}

	/**
	 * Conducts a single experiment
	 *
	 * @param exp
	 * @throws ExperimentAlreadyExistsInDatabaseException
	 * @throws ExperimentDBInteractionFailedException
	 * @throws Exception.
	 *             These are not the exceptions thrown by the experiment itself,
	 *             because these are logged into the database. Exceptions thrown
	 *             here are technical exceptions that occur when arranging the
	 *             experiment
	 */
	public boolean conductExperimentIfNotAlreadyConducted(final Experiment exp) throws ExperimentDBInteractionFailedException {

		/* create experiment entry */
		ExperimentDBEntry expEntry;
		try {
			expEntry = this.handle.createAndGetExperiment(exp);
		} catch (ExperimentAlreadyExistsInDatabaseException e) {
			return false;
		}

		/* run experiment */
		assert expEntry != null;
		Throwable error = null;
		this.knownExperimentEntries.add(expEntry);
		try {
			this.conductor.evaluate(expEntry, m -> {
				try {
					this.handle.updateExperiment(expEntry, m);
				} catch (ExperimentUpdateFailedException e) {
					logger.error("Error in updating experiment data. Message of {}: {}", e.getClass().getName(), e.getMessage());
				}
			});

		} catch (ExperimentEvaluationFailedException e) {
			error = e.getCause();
			logger.error("Experiment failed due to {}. Message: {}. Stack trace: {}", error.getClass().getName(), error.getMessage(), Arrays.asList(error.getStackTrace()).stream().map(s -> "\n\t" + s).collect(Collectors.toList()));
		}
		this.handle.finishExperiment(expEntry, error);
		return true;
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

	private void checkExperimentValidity(final Experiment experiment) {
		if (SetUtil.differenceNotEmpty(this.config.getKeyFields(), experiment.getValuesOfKeyFields().keySet())) {
			throw new IllegalArgumentException("The experiment " + experiment + " is invalid, because key fields have not been defined: " + SetUtil.difference(this.config.getKeyFields(), experiment.getValuesOfKeyFields().keySet()));
		}
	}
}
