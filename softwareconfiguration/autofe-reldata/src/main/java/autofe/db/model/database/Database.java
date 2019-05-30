package autofe.db.model.database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
		return this.tables;
	}

	public void setTables(final List<Table> tables) {
		this.tables = tables;
	}

	public List<BackwardRelationship> getBackwards() {
		return this.backwards;
	}

	public void setBackwards(final List<BackwardRelationship> backwards) {
		this.backwards = backwards;
	}

	public List<ForwardRelationship> getForwards() {
		return this.forwards;
	}

	public void setForwards(final List<ForwardRelationship> forwards) {
		this.forwards = forwards;
	}

	public String getJdbcDriver() {
		return this.jdbcDriver;
	}

	public void setJdbcDriver(final String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getJdbcUrl() {
		return this.jdbcUrl;
	}

	public void setJdbcUrl(final String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getJdbcUsername() {
		return this.jdbcUsername;
	}

	public void setJdbcUsername(final String jdbcUsername) {
		this.jdbcUsername = jdbcUsername;
	}

	public String getJdbcPassword() {
		return this.jdbcPassword;
	}

	public void setJdbcPassword(final String jdbcPassword) {
		this.jdbcPassword = jdbcPassword;
	}

	@Override
	public String toString() {
		return "Database [tables=" + this.tables + ", backwards=" + this.backwards + ", forwards=" + this.forwards + ", jdbcDriver=" + this.jdbcDriver + ", jdbcUrl=" + this.jdbcUrl + ", jdbcUsername=" + this.jdbcUsername + ", jdbcPassword="
				+ this.jdbcPassword + ", jdbcDatabase=" + this.jdbcDatabase + "]";
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.backwards).append(this.forwards).append(this.jdbcDatabase).append(this.jdbcDriver).append(this.jdbcPassword).append(this.jdbcUrl).append(this.jdbcUsername).append(this.tables).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Database)) {
			return false;
		}
		Database other = (Database) obj;
		return new EqualsBuilder().append(this.backwards, other.backwards).append(this.forwards, other.forwards).append(this.jdbcDatabase, other.jdbcDatabase).append(this.jdbcDriver, other.jdbcDriver)
				.append(this.jdbcPassword, other.jdbcPassword).append(this.jdbcUrl, other.jdbcUrl).append(this.jdbcUsername, other.jdbcUsername).append(this.tables, other.tables).isEquals();
	}

	public String getJdbcDatabase() {
		return this.jdbcDatabase;
	}

	public void setJdbcDatabase(final String jdbcDatabase) {
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
