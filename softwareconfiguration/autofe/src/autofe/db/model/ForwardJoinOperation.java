package autofe.db.model;

public class ForwardJoinOperation implements DatabaseOperation {

	private ForwardRelationship forwardRelationship;

	public ForwardJoinOperation(ForwardRelationship forwardRelationship) {
		super();
		this.forwardRelationship = forwardRelationship;
	}

	@Override
	public void applyTo(Database db) {
		// //Check for references
		// //TODO: Necessary?
		// if(!db.getTables().contains(forwardRelationship.getFrom()) ||
		// !db.getTables().contains(forwardRelationship.getTo())){
		// throw new RuntimeException("References are incorrect!");
		// }
		//
		// Table from = forwardRelationship.getFrom();
		// Table to = forwardRelationship.getTo();
		//
		// //1. Add columns to origin table
		// from.getColumns().addAll(to.getColumns());
		//
		// // Remove forward relationship from origin table
		// from.getFowards().remove(forwardRelationship);
		//
		// //2. Update forward relationships of origin table
		// for(ForwardRelationship forwardRelationship : to.getFowards()) {
		// ForwardRelationship newFR = new ForwardRelationship();
		// newFR.setFrom(from);
		// newFR.setTo(forwardRelationship.getTo());
		// newFR.setCommonAttribute(forwardRelationship.getCommonAttribute());
		// from.getFowards().add(newFR);
		// }
		//
		// //3. Update backward relationships of origin table
		// for(BackwardRelationship backwardRelationship : to.getBackwards()) {
		// BackwardRelationship newBR = new BackwardRelationship();
		// newBR.setFrom(from);
		// newBR.setTo(backwardRelationship.getTo());
		// newBR.setCommonAttribute(backwardRelationship.getCommonAttribute());
		// from.getBackwards().add(newBR);
		// }
		//
		// db.getOperationHistory().add(this);
		//
		// return db;
		return;
	}

	public ForwardRelationship getForwardRelationship() {
		return forwardRelationship;
	}

	public void setForwardRelationship(ForwardRelationship forwardRelationship) {
		this.forwardRelationship = forwardRelationship;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((forwardRelationship == null) ? 0 : forwardRelationship.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForwardJoinOperation other = (ForwardJoinOperation) obj;
		if (forwardRelationship == null) {
			if (other.forwardRelationship != null)
				return false;
		} else if (!forwardRelationship.equals(other.forwardRelationship))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ForwardJoinOperation [forwardRelationship=" + forwardRelationship + "]";
	}

}
