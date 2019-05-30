package autofe.db.model.database;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Table {

	private String name;

	private List<Attribute> columns;

	private boolean isTarget;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Attribute> getColumns() {
		return columns;
	}

	public void setColumns(List<Attribute> columns) {
		this.columns = columns;
	}

	public boolean isTarget() {
		return isTarget;
	}

	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(columns).append(isTarget).append(name).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Table)) {
			return false;
		}
		Table other = (Table) obj;
		return new EqualsBuilder().append(columns, other.columns).append(isTarget, other.isTarget).append(name, other.name).isEquals();
	}

	@Override
	public String toString() {
		return "Table [name=" + name + ", columns=" + columns + ", isTarget=" + isTarget + "]";
	}

}
