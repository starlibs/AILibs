package autofe.processor;

import autofe.configuration.AbstractConfiguration;

public abstract class AbstractProcessor {
	public abstract AbstractConfiguration createConfiguration();

	public abstract void extractData();

	public abstract void searchFilters();

	public abstract void applyFilters();
}
