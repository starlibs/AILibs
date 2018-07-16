package autofe.db.model.database;

public class SimpleAttribute extends AbstractAttribute {

	public SimpleAttribute(String name, AttributeType type) {
		super(name, type);
	}

	public SimpleAttribute(String name, AttributeType type, boolean isTarget) {
		super(name, type, isTarget);
	}

}
