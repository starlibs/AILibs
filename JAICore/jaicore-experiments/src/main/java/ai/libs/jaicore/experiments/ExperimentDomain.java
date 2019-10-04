package ai.libs.jaicore.experiments;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.common.attributedobjects.IGetter;

/**
 *
 * @author felix
 *
 * @param <B>
 *            class of the builder for problems in this domain
 * @param <I>
 *            input class of concrete problem instances for the algorithm
 * @param <A>
 *            class of the algorithms applied here
 * @param <Z>
 *            class of interpreted outputs
 */
public abstract class ExperimentDomain<B extends IExperimentBuilder, I, A extends IAlgorithm<? extends I, ?>, Z> {

	private final IExperimentSetConfig config;
	private final IExperimentDecoder<I, A> decoder;
	private final Class<B> builderClass;
	private final Function<Experiment, List<IEventBasedResultUpdater>> resultUpdaterComputer;
	private final Function<Experiment, List<IExperimentTerminationCriterion>> terminationCriterionComputer;

	public ExperimentDomain(final IExperimentSetConfig config, final IExperimentDecoder<I, A> decoder, final Class<B> builderClass, final Function<Experiment, List<IEventBasedResultUpdater>> resultUpdaterComputer,
			final Function<Experiment, List<IExperimentTerminationCriterion>> terminationCriterionComputer) {
		super();
		this.config = config;
		this.decoder = decoder;
		this.builderClass = builderClass;
		this.resultUpdaterComputer = resultUpdaterComputer;
		this.terminationCriterionComputer = terminationCriterionComputer;
	}

	public IExperimentDecoder<I, A> getDecoder() {
		return this.decoder;
	}

	public IExperimentSetConfig getConfig() {
		return this.config;
	}

	public Class<B> getBuilderClass() {
		return this.builderClass;
	}

	public Function<Experiment, List<IExperimentTerminationCriterion>> getTerminationCriterionComputer() {
		return this.terminationCriterionComputer;
	}

	public Function<Experiment, List<IEventBasedResultUpdater>> getResultUpdaterComputer() {
		return this.resultUpdaterComputer;
	}

	public B newBuilder() {
		try {
			return this.builderClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public abstract IGetter<Map<String, Object>, Z> getResultInterpreter();
}
