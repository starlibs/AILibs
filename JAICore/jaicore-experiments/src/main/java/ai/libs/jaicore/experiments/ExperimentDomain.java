package ai.libs.jaicore.experiments;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.api4.java.common.attributedobjects.IGetter;

/**
 *
 * @author felix
 *
 * @param <B> class of the builder for problems in this domain
 * @param <I> input class of concrete problem instances for the algorithm
 * @param <A> class of the algorithms applied here
 * @param <O> class of interpreted outputs
 */
public abstract class ExperimentDomain<B extends IExperimentBuilder, I, A, O> {

	private final File coreFile;
	private final IExperimentDecoder<I, A> decoder;
	private final Class<B> builderClass;

	public File getCoreFile() {
		return this.coreFile;
	}

	public ExperimentDomain(final String coreFile, final IExperimentDecoder<I, A> decoder, final Class<B> builderClass) {
		super();
		this.coreFile = new File("conf" + File.separator + coreFile);
		this.decoder = decoder;
		this.builderClass = builderClass;
	}

	public IExperimentDecoder<I, A> getDecoder() {
		return this.decoder;
	}

	public B newBuilder() {
		try {
			return this.builderClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public abstract IGetter<Map<String,Object>, O> getResultInterpreter();
}
