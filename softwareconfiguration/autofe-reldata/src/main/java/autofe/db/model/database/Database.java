package autofe.db.model.database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;
import autofe.db.util.DBUtils;

public class Database {

	private List<Table> tables;

	private List<BackwardRelationship> backwards;

	private List<ForwardRelationship> forwards;

	private String jdbcDriver;

	private String jdbcUrl;

	private String jdbcUsername;

	private String jdbcPassword;

	private String jdbcDatabase;

	public List<Table> getTables() {
		return tables;
	}

	public void setTables(List<Table> tables) {
		this.tables = tables;
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

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getJdbcUsername() {
		return jdbcUsername;
	}

	public void setJdbcUsername(String jdbcUsername) {
		this.jdbcUsername = jdbcUsername;
	}

	public String getJdbcPassword() {
		return jdbcPassword;
	}

	public void setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword = jdbcPassword;
	}

	@Override
	public String toString() {
		return "Database [tables=" + tables + ", backwards=" + backwards + ", forwards=" + forwards + ", jdbcDriver="
				+ jdbcDriver + ", jdbcUrl=" + jdbcUrl + ", jdbcUsername=" + jdbcUsername + ", jdbcPassword="
				+ jdbcPassword + ", jdbcDatabase=" + jdbcDatabase + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((backwards == null) ? 0 : backwards.hashCode());
		result = prime * result + ((forwards == null) ? 0 : forwards.hashCode());
		result = prime * result + ((jdbcDatabase == null) ? 0 : jdbcDatabase.hashCode());
		result = prime * result + ((jdbcDriver == null) ? 0 : jdbcDriver.hashCode());
		result = prime * result + ((jdbcPassword == null) ? 0 : jdbcPassword.hashCode());
		result = prime * result + ((jdbcUrl == null) ? 0 : jdbcUrl.hashCode());
		result = prime * result + ((jdbcUsername == null) ? 0 : jdbcUsername.hashCode());
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
		} else if (!backwards.equals(other.backwards)) {
			return false;
		} if (forwards == null) {
			if (other.forwards != null)
				return false;
		} else if (!forwards.equals(other.forwards)) {
			return false;
		} if (jdbcDatabase == null) {
			if (other.jdbcDatabase != null)
				return false;
		} else if (!jdbcDatabase.equals(other.jdbcDatabase)) {
			return false;
		} if (jdbcDriver == null) {
			if (other.jdbcDriver != null) {
				return false;
			}
		} else if (!jdbcDriver.equals(other.jdbcDriver)) {
			return false;
		}
		if (jdbcPassword == null) {
			if (other.jdbcPassword != null) {
				return false;
			}
		} else if (!jdbcPassword.equals(other.jdbcPassword)) {
			return false;
		} if (jdbcUrl == null) {
			if (other.jdbcUrl != null) {
				return false;
			}
		} else if (!jdbcUrl.equals(other.jdbcUrl)) {
			return false;
		}
		if (jdbcUsername == null) {
			if (other.jdbcUsername != null)
				return false;
		} else if (!jdbcUsername.equals(other.jdbcUsername)) {
			return false;
		}
		if (tables == null) {
			if (other.tables != null) {
				return false;
			}
		} else if (!tables.equals(other.tables)) {
			return false;
		}
		return true;
	}

	public String getJdbcDatabase() {
		return jdbcDatabase;
	}

	public void setJdbcDatabase(String jdbcDatabase) {
		this.jdbcDatabase = jdbcDatabase;
	}

	public Set<Attribute> getForwardAttributes() {
		Set<Attribute> toReturn = new HashSet<>();

		Table targetTable = DBUtils.getTargetTable(this);
		for (Table t : DBUtils.getForwardReachableTables(targetTable, this)) {
			for (Attribute att : t.getColumns()) {
				if (att.isFeature()) {
					toReturn.add(att);
				}
			}
		}
		return toReturn;
	}

	public Set<Attribute> getBackwardAttributes() {
		Set<Attribute> toReturn = new HashSet<>();

		Table targetTable = DBUtils.getTargetTable(this);
		for (Table t : DBUtils.getBackwardReachableTables(targetTable, this)) {
			for (Attribute att : t.getColumns()) {
				if (att.isFeature()) {
					toReturn.add(att);
				}
			}
		}
		return toReturn;
	}

}
