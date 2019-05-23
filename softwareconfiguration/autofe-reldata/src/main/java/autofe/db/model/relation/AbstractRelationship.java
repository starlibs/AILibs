package autofe.db.model.relation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.Attribute;
import autofe.db.model.database.Database;
import autofe.db.model.database.Table;
import autofe.db.util.DBUtils;

public abstract class AbstractRelationship {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRelationship.class);

	private static final String LOG_CONTEXT_NOT_SET = "Context not set!";

	protected String fromTableName;

	protected String toTableName;

	protected String commonAttributeName;

	protected Database context;

	public AbstractRelationship() {
		// nothing to do here
	}

	public AbstractRelationship(String fromTableName, String toTableName, String commonAttributeName) {
		super();
		this.fromTableName = fromTableName;
		this.toTableName = toTableName;
		this.commonAttributeName = commonAttributeName;
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
			throw new IllegalStateException(LOG_CONTEXT_NOT_SET);
		}
		if (DBUtils.getTableByName(fromTableName, context) == null) {
			LOGGER.warn("{} is null!", fromTableName);
		}
		return DBUtils.getTableByName(fromTableName, context);
	}

	public Table getTo() {
		if (context == null) {
			throw new IllegalStateException(LOG_CONTEXT_NOT_SET);
		}
		return DBUtils.getTableByName(toTableName, context);
	}

	public Attribute getCommonAttribute() {
		if (context == null) {
			throw new IllegalStateException(LOG_CONTEXT_NOT_SET);
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractRelationship other = (AbstractRelationship) obj;
		if (commonAttributeName == null) {
			if (other.commonAttributeName != null) {
				return false;
			}
		} else if (!commonAttributeName.equals(other.commonAttributeName)) {
			return false;
		}
		if (fromTableName == null) {
			if (other.fromTableName != null) {
				return false;
			}
		} else if (!fromTableName.equals(other.fromTableName)) {
			return false;
		}
		if (toTableName == null) {
			if (other.toTableName != null) {
				return false;
			}
		} else if (!toTableName.equals(other.toTableName)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AbstractRelationship [fromTableName=" + fromTableName + ", toTableName=" + toTableName + ", commonAttributeName=" + commonAttributeName + "]";
	}

}
