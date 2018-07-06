package autofe.algorithm.hasco.filter.image;

import autofe.algorithm.hasco.filter.meta.IFilter;

/**
 * Abstract wrapper filter for catalano filters.
 * 
 * @author Julian Lienen
 *
 * @param <T>
 *            Type (interface) of the wrapped catalano filters.
 */
public abstract class AbstractCatalanoFilter<T> implements IFilter {
	private T catalanoFilter;
	private boolean requiresGrayscale;

	public T getCatalanoFilter() {
		return catalanoFilter;
	}

	public void setCatalanoFilter(T catalanoFilter) {
		this.catalanoFilter = catalanoFilter;
	}

	public boolean isRequiresGrayscale() {
		return requiresGrayscale;
	}

	public void setRequiresGrayscale(boolean requiresGrayscale) {
		this.requiresGrayscale = requiresGrayscale;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " ["
				+ (this.getCatalanoFilter() != null ? this.getCatalanoFilter().getClass().getSimpleName() : "") + "]";
	}
}
