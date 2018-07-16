package autofe.db.model.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.Attribute;
import autofe.db.model.database.BackwardRelationship;
import autofe.db.model.database.Database;
import autofe.db.model.database.DatabaseOperation;
import autofe.db.model.database.ForwardRelationship;
import autofe.db.model.database.Table;
import autofe.db.search.DatabaseSuccessorGenerator;
import autofe.db.util.DBUtils;

public class ForwardJoinOperation implements DatabaseOperation {
	private static Logger LOG = LoggerFactory.getLogger(ForwardJoinOperation.class);

	private String fromTableName;

	private String toTableName;

	private String commonAttributeName;

	public ForwardJoinOperation(ForwardRelationship forwardRelationship) {
		super();
		this.fromTableName = forwardRelationship.getFrom().getName();
		this.toTableName = forwardRelationship.getTo().getName();
		this.commonAttributeName = forwardRelationship.getCommonAttribute().getName();
	}

	public static Logger getLOG() {
		return LOG;
	}

	public static void setLOG(Logger lOG) {
		LOG = lOG;
	}

	public String getFromTableName() {
		return fromTableName;
	}

	public void setFromTableName(String fromTableName) {
		this.fromTableName = fromTableName;
	}

	public String getToTableName() {
		return toTableName;
	}

	public void setToTableName(String toTableName) {
		this.toTableName = toTableName;
	}

	public String getCommonAttributeName() {
		return commonAttributeName;
	}

	public void setCommonAttributeName(String commonAttributeName) {
		this.commonAttributeName = commonAttributeName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commonAttributeName == null) ? 0 : commonAttributeName.hashCode());
		result = prime * result + ((fromTableName == null) ? 0 : fromTableName.hashCode());
		result = prime * result + ((toTableName == null) ? 0 : toTableName.hashCode());
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
		if (commonAttributeName == null) {
			if (other.commonAttributeName != null)
				return false;
		} else if (!commonAttributeName.equals(other.commonAttributeName))
			return false;
		if (fromTableName == null) {
			if (other.fromTableName != null)
				return false;
		} else if (!fromTableName.equals(other.fromTableName))
			return false;
		if (toTableName == null) {
			if (other.toTableName != null)
				return false;
		} else if (!toTableName.equals(other.toTableName))
			return false;
		return true;
	}

	@Override
	public void applyTo(Database db) {

		// Create context
		Table from = DBUtils.getTableByName(fromTableName, db);
		Table to = DBUtils.getTableByName(toTableName, db);
		Attribute commonAttribute = DBUtils.getAttributeByName(commonAttributeName, from);
		ForwardRelationship fr = new ForwardRelationship();
		fr.setFrom(from);
		fr.setTo(to);
		fr.setCommonAttribute(commonAttribute);

		// 1. Add columns to origin table
		for (Attribute attribute : to.getColumns()) {
			if (!from.getColumns().contains(attribute)) {
				LOG.debug("Add attribute {}", attribute.getName());
				from.getColumns().add(attribute);
			}
		}

		// Remove forward relationship from origin table
		db.getForwards().remove(fr);

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
	}

}
