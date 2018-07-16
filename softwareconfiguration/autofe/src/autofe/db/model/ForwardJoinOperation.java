package autofe.db.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.search.DatabaseSuccessorGenerator;
import autofe.db.util.DBUtils;

public class ForwardJoinOperation implements DatabaseOperation {
	private static Logger LOG = LoggerFactory.getLogger(ForwardJoinOperation.class);

	private ForwardRelationship forwardRelationship;

	public ForwardJoinOperation(ForwardRelationship forwardRelationship) {
		super();
		this.forwardRelationship = forwardRelationship;
	}

	@Override
	public void applyTo(Database db) {

		//TODO: applyTo() mit clone => Returned geclonte DB
		LOG.debug("Database before application: {}", DBUtils.serializeToString(db));

		Table from = forwardRelationship.getFrom();
		Table to = forwardRelationship.getTo();

		// 1. Add columns to origin table
		for (Attribute attribute : to.getColumns()) {
			if (!from.getColumns().contains(attribute)) {
				LOG.debug("Add attribute {}", attribute.getName());
				from.getColumns().add(attribute);
			}
		}

		// Remove forward relationship from origin table
		db.getForwards().remove(forwardRelationship);

		// 2. Update forward relationships of origin table
		for (ForwardRelationship forwardRelationship : DBUtils.getForwardsFor(to, db)) {
			ForwardRelationship newFR = new ForwardRelationship();
			newFR.setFrom(from);
			newFR.setTo(forwardRelationship.getTo());
			newFR.setCommonAttribute(forwardRelationship.getCommonAttribute());
			db.getForwards().add(newFR);
		}

		// 3. Update backward relationships of origin table
		for (BackwardRelationship backwardRelationship : DBUtils.getBackwardsFor(to, db)) {
			BackwardRelationship newBR = new BackwardRelationship();
			newBR.setFrom(from);
			newBR.setTo(backwardRelationship.getTo());
			newBR.setCommonAttribute(backwardRelationship.getCommonAttribute());
			db.getBackwards().add(newBR);
		}

		db.getOperationHistory().add(this);

		LOG.debug("Database after application: {}", DBUtils.serializeToString(db));
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
