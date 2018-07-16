package autofe.db.model.operation;

import autofe.db.model.database.AggregatedAttribute;
import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.AbstractAttribute;
import autofe.db.model.database.AttributeType;
import autofe.db.model.database.BackwardRelationship;
import autofe.db.model.database.Database;
import autofe.db.model.database.DatabaseOperation;
import autofe.db.model.database.Table;
import autofe.db.util.DBUtils;

public class BackwardAggregateOperation implements DatabaseOperation {

	private String fromTableName;

	private String toTableName;

	private String commonAttributeName;

	private String toBeAggregatedName;

	private AggregationFunction aggregationFunction;

	public BackwardAggregateOperation(BackwardRelationship backwardRelationship,
			AggregationFunction aggregationFunction, String toBeAggregatedName) {
		super();
		this.fromTableName = backwardRelationship.getFrom().getName();
		this.toTableName = backwardRelationship.getTo().getName();
		this.commonAttributeName = backwardRelationship.getCommonAttribute().getName();
		this.aggregationFunction = aggregationFunction;
		this.toBeAggregatedName = toBeAggregatedName;
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

	public String getToBeAggregatedName() {
		return toBeAggregatedName;
	}

	public void setToBeAggregatedName(String toBeAggregatedName) {
		this.toBeAggregatedName = toBeAggregatedName;
	}

	public AggregationFunction getAggregationFunction() {
		return aggregationFunction;
	}

	public void setAggregationFunction(AggregationFunction aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aggregationFunction == null) ? 0 : aggregationFunction.hashCode());
		result = prime * result + ((commonAttributeName == null) ? 0 : commonAttributeName.hashCode());
		result = prime * result + ((fromTableName == null) ? 0 : fromTableName.hashCode());
		result = prime * result + ((toBeAggregatedName == null) ? 0 : toBeAggregatedName.hashCode());
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
		BackwardAggregateOperation other = (BackwardAggregateOperation) obj;
		if (aggregationFunction != other.aggregationFunction)
			return false;
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
		if (toBeAggregatedName == null) {
			if (other.toBeAggregatedName != null)
				return false;
		} else if (!toBeAggregatedName.equals(other.toBeAggregatedName))
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
		AbstractAttribute toBeAggregated = DBUtils.getAttributeByName(toBeAggregatedName, to);

		// New feature in from column
		String aggregatedAttributeName = DBUtils.getAggregatedAttributeName(aggregationFunction, toTableName,
				toBeAggregated.getName());
		AggregatedAttribute aggregatedAttribute = new AggregatedAttribute(aggregatedAttributeName,
				AttributeType.NUMERIC, toBeAggregated, aggregationFunction);
		from.getColumns().add(aggregatedAttribute);

		db.getOperationHistory().add(this);
	}

	@Override
	public String toString() {
		return "BackwardAggregateOperation [fromTableName=" + fromTableName + ", toTableName=" + toTableName
				+ ", commonAttributeName=" + commonAttributeName + ", toBeAggregatedName=" + toBeAggregatedName
				+ ", aggregationFunction=" + aggregationFunction + "]";
	}

}
