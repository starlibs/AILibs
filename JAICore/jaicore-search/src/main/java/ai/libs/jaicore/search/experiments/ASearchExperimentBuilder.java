package ai.libs.jaicore.search.experiments;

import ai.libs.jaicore.experiments.AExperimentBuilder;

public abstract class ASearchExperimentBuilder<B extends ASearchExperimentBuilder<B>> extends AExperimentBuilder<B> {

	public ASearchExperimentBuilder() {
		super();
		this.requireField("search");
	}

	public B withSearch(final String search) {
		this.set("search", search);
		return this.getMe();
	}
}
