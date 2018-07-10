package autofe.db.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Database {

	private List<Table> tables;

	private List<DatabaseOperation> operationHistory;

	private List<BackwardRelationship> backwards;

	private List<ForwardRelationship> forwards;

	public List<Table> getTables() {
		return tables;
	}

	public void setTables(List<Table> tables) {
		this.tables = tables;
	}

	public List<DatabaseOperation> getOperationHistory() {
		if (this.operationHistory == null) {
			operationHistory = new ArrayList<>();
		}
		return operationHistory;
	}

	public void setOperationHistory(List<DatabaseOperation> operationHistory) {
		this.operationHistory = operationHistory;
	}

	public List<BackwardRelationship> getBackwards() {
		return backwards;
	}

	public void setBackwards(List<BackwardRelationship> backwards) {
		this.backwards = backwards;
	}

	public List<ForwardRelationship> getForwards() {
		return forwards;
	}

	public void setForwards(List<ForwardRelationship> forwards) {
		this.forwards = forwards;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((backwards == null) ? 0 : backwards.hashCode());
		result = prime * result + ((forwards == null) ? 0 : forwards.hashCode());
		result = prime * result + ((operationHistory == null) ? 0 : operationHistory.hashCode());
		result = prime * result + ((tables == null) ? 0 : tables.hashCode());
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
		Database other = (Database) obj;
		if (backwards == null) {
			if (other.backwards != null)
				return false;
		} else if (!backwards.equals(other.backwards))
			return false;
		if (forwards == null) {
			if (other.forwards != null)
				return false;
		} else if (!forwards.equals(other.forwards))
			return false;
		if (operationHistory == null) {
			if (other.operationHistory != null)
				return false;
		} else if (!operationHistory.equals(other.operationHistory))
			return false;
		if (tables == null) {
			if (other.tables != null)
				return false;
		} else if (!tables.equals(other.tables))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Database [tables=" + tables + ", operationHistory=" + operationHistory + ", backwards=" + backwards
				+ ", forwards=" + forwards + "]";
	}

}
