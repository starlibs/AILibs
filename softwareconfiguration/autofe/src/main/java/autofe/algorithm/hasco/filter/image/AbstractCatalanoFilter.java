package autofe.algorithm.hasco.filter.image;

import java.io.Serializable;

import Catalano.Imaging.FastBitmap;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import autofe.util.ImageUtils;

/**
 * Abstract wrapper filter for catalano filters.
 *
 * @author Julian Lienen
 *
 * @param <T>
 *            Type (interface) of the wrapped catalano filters.
 */
public abstract class AbstractCatalanoFilter<T> implements IFilter, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 7278242028991236545L;

	private String name;

	protected AbstractCatalanoFilter(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public abstract AbstractCatalanoFilter<T> clone() throws CloneNotSupportedException;

	DataSet prepareData(final DataSet inputData, final boolean copy) {
		if (copy) {
			return inputData.copy();
		} else {
			return inputData;
		}
	}

	static void checkInterrupt() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Thread got interrupted, thus, kill filter application.");
		}
	}

	static FastBitmap.ColorSpace sampleColorSpace(final DataSet inputData) {
		return ImageUtils.determineColorSpace(inputData.getIntermediateInstances().get(0));
	}
}
