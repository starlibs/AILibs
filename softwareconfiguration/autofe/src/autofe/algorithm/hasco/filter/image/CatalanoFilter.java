package autofe.algorithm.hasco.filter.image;

import autofe.algorithm.hasco.filter.meta.IFilter;

public abstract class CatalanoFilter<T> implements IFilter {
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
}
