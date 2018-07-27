package autofe.db.model.relation;

import autofe.db.model.database.Attribute;
import autofe.db.model.database.Database;
import autofe.db.model.database.Table;
import autofe.db.util.DBUtils;

public abstract class AbstractRelationship {

	protected String fromTableName;

	protected String toTableName;

	protected String commonAttributeName;

	protected transient Database context;

	public AbstractRelationship() {
		// TODO Auto-generated constructor stub
	}

	public AbstractRelationship(String fromTableName, String toTableName) {
		super();
		this.fromTableName = fromTableName;
		this.toTableName = toTableName;
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

	public void setContext(Database context) {
		this.context = context;
	}

	public Table getFrom() {
		if (context == null) {
			throw new IllegalStateException("Context not set!");
		}
		if (DBUtils.getTableByName(fromTableName, context) == null) {
			System.out.println(fromTableName + " is null!");
		}
		return DBUtils.getTableByName(fromTableName, context);
	}

	public Table getTo() {
		if (context == null) {
			throw new IllegalStateException("Context not set!");
		}
		return DBUtils.getTableByName(toTableName, context);
	}

	public Attribute getCommonAttribute() {
		if (context == null) {
			throw new IllegalStateException("Context not set!");
		}
		return DBUtils.getAttributeByName(commonAttributeName, DBUtils.getTableByName(fromTableName, context));
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
		AbstractRelationship other = (AbstractRelationship) obj;
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
	public String toString() {
		return "AbstractRelationship [fromTableName=" + fromTableName + ", toTableName=" + toTableName
				+ ", commonAttributeName=" + commonAttributeName + "]";
	}

}
