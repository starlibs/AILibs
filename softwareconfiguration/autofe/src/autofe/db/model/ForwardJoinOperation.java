package autofe.db.model;

public class ForwardJoinOperation implements DatabaseOperation {

	private ForwardRelationship forwardRelationship;	
	
	public ForwardJoinOperation(ForwardRelationship forwardRelationship) {
		super();
		this.forwardRelationship = forwardRelationship;
	}

	@Override
	public Database applyTo(Database db) {
//		//Check for references
//		//TODO: Necessary?
//		if(!db.getTables().contains(forwardRelationship.getFrom()) ||
//				!db.getTables().contains(forwardRelationship.getTo())){
//			throw new RuntimeException("References are incorrect!");
//		}
//		
//		Table from = forwardRelationship.getFrom();
//		Table to = forwardRelationship.getTo();
//		
//		//1. Add columns to origin table
//		from.getColumns().addAll(to.getColumns());
//		
//		// Remove forward relationship from origin table
//		from.getFowards().remove(forwardRelationship);
//		
//		//2. Update forward relationships of origin table
//		for(ForwardRelationship forwardRelationship : to.getFowards()) {
//			ForwardRelationship newFR = new ForwardRelationship();
//			newFR.setFrom(from);
//			newFR.setTo(forwardRelationship.getTo());
//			newFR.setCommonAttribute(forwardRelationship.getCommonAttribute());
//			from.getFowards().add(newFR);
//		}
//		
//		//3. Update backward relationships of origin table
//		for(BackwardRelationship backwardRelationship : to.getBackwards()) {
//			BackwardRelationship newBR = new BackwardRelationship();
//			newBR.setFrom(from);
//			newBR.setTo(backwardRelationship.getTo());
//			newBR.setCommonAttribute(backwardRelationship.getCommonAttribute());
//			from.getBackwards().add(newBR);
//		}
//		
//		db.getOperationHistory().add(this);
//		
//		return db;
		return null;
	}

}
