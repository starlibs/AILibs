package autofe.db.model.relation;

public class BackwardRelationship extends AbstractRelationship {

	public BackwardRelationship(String fromTableName, String toTableName) {
		super(fromTableName, toTableName);
	}

	@Override
	public String toString() {
		return "BackwardRelationship [fromTableName=" + fromTableName + ", toTableName=" + toTableName
				+ ", commonAttributeName=" + commonAttributeName + "]";
	}

}
