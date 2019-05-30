package autofe.db.model.relation;

public class BackwardRelationship extends AbstractRelationship {

	public BackwardRelationship() {
		super();
	}

	public BackwardRelationship(final String fromTableName, final String toTableName, final String commonAttributeName) {
		super(fromTableName, toTableName, commonAttributeName);
	}

	@Override
	public String toString() {
		return "BackwardRelationship [fromTableName=" + this.fromTableName + ", toTableName=" + this.toTableName + ", commonAttributeName=" + this.commonAttributeName + "]";
	}

}
