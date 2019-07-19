package ai.libs.jaicore.ml.dataset;

public abstract class AAttribute implements IAttribute {

	private final String name;

	protected AAttribute(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
