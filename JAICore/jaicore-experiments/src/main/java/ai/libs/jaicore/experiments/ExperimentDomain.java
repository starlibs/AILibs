package ai.libs.jaicore.experiments;

import java.lang.reflect.InvocationTargetException;

import org.api4.java.algorithm.IAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logging.LoggerUtil;

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
 */
public abstract class ExperimentDomain<B extends IExperimentBuilder, I, A extends IAlgorithm<? extends I, ?>> {

	protected Logger logger = LoggerFactory.getLogger("experimentsdomain");
	private final IExperimentSetConfig config;
	private final IExperimentDecoder<I, A> decoder;

	public ExperimentDomain(final IExperimentSetConfig config, final IExperimentDecoder<I, A> decoder) {
		super();
		this.config = config;
		this.decoder = decoder;
	}

	public IExperimentSetConfig getConfig() {
		return this.config;
	}

	public IExperimentDecoder<I, A> getDecoder() {
		return this.decoder;
	}

	public abstract Class<B> getBuilderClass();

	public B newBuilder() {
		try {
			return this.getBuilderClass().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			this.logger.error(LoggerUtil.getExceptionInfo(e));
			return null;
		}
	}
}
