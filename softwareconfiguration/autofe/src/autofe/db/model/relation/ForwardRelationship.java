package autofe.db.model.relation;

public class ForwardRelationship extends AbstractRelationship {

	public ForwardRelationship() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		return "ForwardRelationship [fromTableName=" + fromTableName + ", toTableName=" + toTableName
				+ ", commonAttributeName=" + commonAttributeName + "]";
	}
	
	

}
