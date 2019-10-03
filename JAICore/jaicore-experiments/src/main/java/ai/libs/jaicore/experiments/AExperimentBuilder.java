package ai.libs.jaicore.experiments;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ai.libs.jaicore.basic.sets.SetUtil;

public abstract class AExperimentBuilder<B extends AExperimentBuilder<B>> implements IExperimentBuilder {

	private int memory;
	private int numCPUs;
	private final Map<String, String> keyMap = new HashMap<>();
	private Set<String> requiredFields = new HashSet<>();

	protected void set(final String key, final Object value) {
		this.keyMap.put(key, "" + value);
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
			B builder = (B) this.getMe().getClass().getConstructor().newInstance();
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
		if (!this.keyMap.keySet().containsAll(this.requiredFields)) {
			throw new IllegalStateException("Cannot build experiment. Required fields have not been defined: " + SetUtil.difference(this.requiredFields, this.keyMap.keySet()));
		}
		this.preBuildHook();
		return new Experiment(this.memory, this.numCPUs, this.keyMap);
	}

	/**
	 * This can be used to check whether everything is ok with the experiment
	 */
	protected void preBuildHook() {}

	protected void requireField(final String field) {
		this.requiredFields.add(field);
	}
}
