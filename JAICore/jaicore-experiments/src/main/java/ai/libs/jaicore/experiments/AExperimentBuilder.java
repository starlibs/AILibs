package ai.libs.jaicore.experiments;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.experiments.exceptions.IllegalKeyDescriptorException;

public abstract class AExperimentBuilder<B extends AExperimentBuilder<B>> implements IExperimentBuilder {

	private int memory;
	private int numCPUs;
	private final Map<String, String> keyMap = new HashMap<>();
	private final IExperimentSetConfig config;
	private final Class<?> configClass;
	private final ExperimentSetAnalyzer analyzer;

	public AExperimentBuilder(final IExperimentSetConfig config) {
		super();
		this.config = config;

		/* determine config class (relevant for forks) */
		Class<?> tmpConfigClass = null;
		for (Class<?> c : this.config.getClass().getInterfaces()) {
			if (IExperimentSetConfig.class.isAssignableFrom(c)) {
				if (tmpConfigClass != null) {
					throw new IllegalStateException("Config interface is not unique!");
				}
				tmpConfigClass = c;
			}
		}
		if (tmpConfigClass == null) {
			throw new IllegalArgumentException("Could not identify config interface of the given configuration");
		}
		this.configClass = tmpConfigClass;
		this.analyzer = new ExperimentSetAnalyzer(config);
	}

	protected void set(final String key, final Object value) {
		String valAsString = value.toString();
		try {
			if (!this.analyzer.isValueForKeyValid(key, valAsString)) {
				throw new IllegalArgumentException("\"" + valAsString + "\" is not a valid value for key \"" + key + "\"");
			}
		} catch (IllegalKeyDescriptorException e) {
			throw new IllegalArgumentException("\"" + key + "\" is not a valid key in this experiment setup.");
		}
		this.keyMap.put(key, valAsString);
	}

	protected abstract B getMe();

	public B withMem(final int memoryInMB) {
		this.memory = memoryInMB;
		return this.getMe();
	}

	public B withCPUs(final int numCPUs) {
		this.numCPUs = numCPUs;
		return this.getMe();
	}

	public B fork() {
		try {
			B builder = (B) this.getMe().getClass().getConstructor(this.configClass).newInstance(this.config);
			AExperimentBuilder<B> cBuilder = (builder);
			cBuilder.keyMap.putAll(this.keyMap);
			cBuilder.memory = this.memory;
			cBuilder.numCPUs = this.numCPUs;
			return builder;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public B withExperiment(final Experiment experiment) {
		this.keyMap.putAll(experiment.getValuesOfKeyFields());
		this.memory = experiment.getMemoryInMB();
		this.numCPUs = experiment.getNumCPUs();
		return this.getMe();
	}

	@Override
	public Experiment build() {
		if (!this.keyMap.keySet().containsAll(this.config.getKeyFields())) {
			throw new IllegalStateException("Cannot build experiment. Required fields have not been defined: " + SetUtil.difference(this.config.getKeyFields(), this.keyMap.keySet()));
		}
		this.preBuildHook();
		return new Experiment(this.memory, this.numCPUs, this.keyMap);
	}

	/**
	 * This can be used to check whether everything is ok with the experiment
	 */
	protected void preBuildHook() {
	}
}