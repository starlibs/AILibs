package autofe.db.model.relation;

public class ForwardRelationship extends AbstractRelationship {

	public ForwardRelationship() {
		super();
		// nothing to do here
	}

	public ForwardRelationship(String fromTableName, String toTableName, String commonAttributeName) {
		super(fromTableName, toTableName, commonAttributeName);
	}

	@Override
	public String toString() {
		return "ForwardRelationship [fromTableName=" + fromTableName + ", toTableName=" + toTableName + ", commonAttributeName=" + commonAttributeName + "]";
	}

}
