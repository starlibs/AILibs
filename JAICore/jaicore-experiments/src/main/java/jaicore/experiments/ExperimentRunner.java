package jaicore.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.StringUtil;
import jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;

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
	private boolean cond_memoryLimitCheck = false;

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
		handle = databaseHandle;
	}

	private void updateExperimentSetupAccordingToConfig() {
		if (cond_memoryLimitCheck) {
			memoryLimit = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
			if (memoryLimit != config.getMemoryLimitInMB()) {
				System.err.println("The true memory limit is " + memoryLimit + ", which differs from the " + config.getMemoryLimitInMB() + " specified in the config! We will write " + memoryLimit + " into the database.");
			}
		} else {
			memoryLimit = config.getMemoryLimitInMB();
		}

		cpuLimit = config.getNumberOfCPUs();
		int numExperiments = 1;
		try {

			/* create map of possible values for each key field */
			for (String key : config.getKeyFields()) {
				/* this is a hack needed because one cannot retrieve generic configs */
				String propertyVals = config.removeProperty(key);
				if (propertyVals == null) {
					throw new IllegalArgumentException("No property values defined for key field \"" + key + "\"");
				}
				List<String> vals = Arrays.asList(StringUtil.explode(propertyVals, ",")).stream().map(s -> s.trim()).collect(Collectors.toList());
				config.setProperty(key, propertyVals);
				valuesForKeyFields.put(key, vals);
				numExperiments *= vals.size();
			}

			handle.setup(config);
			knownExperimentEntries.addAll(handle.getConductedExperiments());

		} catch (ExperimentDBInteractionFailedException e) {
			e.printStackTrace();
			numExperiments = -1;
		}
		totalNumberOfExperiments = numExperiments;
	}

	private String getDatabaseFieldnameForConfigEntry(final String configKey) {
		return configKey.substring(0, configKey.length() - 1).replace(".", "_");
	}

	public ExperimentDBEntry createAndGetExperimentIfNotConducted(final Map<String, String> values) throws ExperimentDBInteractionFailedException {
		return handle.createAndGetExperimentIfNotConducted(new Experiment(memoryLimit, cpuLimit, values));
	}

	public Experiment getExperimentForNumber(final int id) {
		if (id < 0) {
			throw new IllegalArgumentException("Experiment ID must be positive!");
		}
		if (id >= totalNumberOfExperiments) {
			throw new IllegalArgumentException("Invalid experiment ID " + id + ". Only " + totalNumberOfExperiments + " are possible with the given config.");
		}

		/* determine the block sizes for the different iterations */
		Map<String, Integer> blockSizes = new HashMap<>();
		int size = 1;
		List<String> keyOrder = new ArrayList<>(config.getKeyFields());
		Collections.reverse(keyOrder);
		for (String key : keyOrder) {
			blockSizes.put(key, size);
			size *= valuesForKeyFields.get(key).size();
		}

		/* find the correct experiment */
		Map<String, String> keyFieldValues = new HashMap<>();
		int k = id;
		for (String key : config.getKeyFields()) {
			int s = blockSizes.get(key);
			int index = (int) Math.floor(k / s * 1f);
			keyFieldValues.put(getDatabaseFieldnameForConfigEntry(key), valuesForKeyFields.get(key).get(index));
			k = k % s;
		}
		return new Experiment(memoryLimit, cpuLimit, keyFieldValues);
	}

	public void randomlyConductExperiments(final int maxNumberOfExperiments, final boolean reload) {
		updateExperimentSetupAccordingToConfig();

		if (totalNumberOfExperiments <= 0) {
			System.out.println("Number of total experiments is 0");
			return;
		}

		logger.info("Now conducting new experiment. {}/{} experiments have already been started or even been completed", knownExperimentEntries.size(), totalNumberOfExperiments);

		int numberOfConductedExperiments = 0;
		while (!Thread.interrupted() && knownExperimentEntries.size() < totalNumberOfExperiments && ((maxNumberOfExperiments > 0) ? numberOfConductedExperiments < maxNumberOfExperiments : true)) {
			if (reload) {
				config.reload();
			}
			updateExperimentSetupAccordingToConfig();
			int k = (int) Math.floor(Math.random() * totalNumberOfExperiments);
			logger.info("Now conducting {}/{}", k, totalNumberOfExperiments);
			try {
				Experiment exp = getExperimentForNumber(k);
				logger.info("Conduct experiment with key values: {}", exp.getValuesOfKeyFields());
				if (conductExperimentIfNotAlreadyConducted(exp)) {
					numberOfConductedExperiments++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void randomlyConductExperiments(final boolean reload) {
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

		ExperimentDBEntry expEntry = handle.createAndGetExperimentIfNotConducted(exp);
		if (expEntry != null) {
			Exception error = null;
			knownExperimentEntries.add(expEntry);
			try {
				conductor.evaluate(expEntry, m -> {
					try {
						handle.updateExperiment(expEntry, m);
					} catch (ExperimentDBInteractionFailedException  e) {
						e.printStackTrace();
					}
				});

			} catch (Exception e) {
				error = e;
				System.err.println("Experiment failed due to exception, which has been logged");
			}
			handle.finishExperiment(expEntry, error);
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
		cond_memoryLimitCheck = doCheck;
	}
}
