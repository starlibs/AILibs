package autofe.db.model.relation;

public class BackwardRelationship extends AbstractRelationship {
	
	public BackwardRelationship() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "BackwardRelationship [fromTableName=" + fromTableName + ", toTableName=" + toTableName
				+ ", commonAttributeName=" + commonAttributeName + "]";
	}

}
