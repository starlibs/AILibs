package autofe.algorithm.hasco.filter.image;

import java.io.Serializable;

import autofe.algorithm.hasco.filter.meta.IFilter;

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
}
