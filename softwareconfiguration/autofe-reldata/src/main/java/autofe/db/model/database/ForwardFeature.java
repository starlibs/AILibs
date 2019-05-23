package autofe.db.model.database;

public class ForwardFeature extends AbstractFeature {

	public ForwardFeature(Attribute parent) {
		super(parent);
	}

	public ForwardFeature(ForwardFeature toClone) {
		super(toClone.parent);
	}

	@Override
	public String getName() {
		return parent.getFullName();
	}

	@Override
	public AttributeType getType() {
		return parent.getType();
	}

	@Override
	public String toString() {
		return "[F:" + getName() + "]";
	}

}
