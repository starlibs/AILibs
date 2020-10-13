package ai.libs.jaicore.components.model;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IComponentInstanceConstraint;

public class ComponentInstanceConstraint implements IComponentInstanceConstraint {

	private final boolean positive;
	private final IComponentInstance premise;
	private final IComponentInstance conclusion;

	public ComponentInstanceConstraint(final boolean positive, final IComponentInstance premise, final IComponentInstance conclusion) {
		super();
		this.positive = positive;
		this.premise = premise;
		this.conclusion = conclusion;
	}

	@Override
	public boolean isPositive() {
		return this.positive;
	}

	@Override
	public IComponentInstance getPremisePattern() {
		return this.premise;
	}

	@Override
	public IComponentInstance getConclusionPattern() {
		return this.conclusion;
	}

}
