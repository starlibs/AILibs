package autofe.db.util;

import java.util.List;

public class SelectPart {
	private List<String> selectedColumns;
	private String fromTable;
	private String joinTable;
	private String commonAttribute;
	private int counter;
	private String groupBy;

	protected SelectPart(final String commonAttribute, final int counter, final String fromTable, final String joinTable) {
		this.fromTable = fromTable;
		this.joinTable = joinTable;
		this.counter = counter;
		this.commonAttribute = commonAttribute;
	}

	protected String part1() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (int i = 0; i < this.selectedColumns.size(); i++) {
			sb.append(this.selectedColumns.get(i));
			if (i != this.selectedColumns.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append(" FROM ");
		sb.append(this.fromTable);
		sb.append(" LEFT OUTER JOIN");
		return sb.toString();
	}

	protected String part2() {
		StringBuilder sb = new StringBuilder();
		sb.append(SqlUtils.TEMP_TABLE + this.counter);
		sb.append(String.format(" ON (%1$s.%2$s = %3$s.%2$s)", this.fromTable, this.commonAttribute, SqlUtils.escape(SqlUtils.TEMP_TABLE + this.counter)));
		if (this.groupBy != null && !this.groupBy.isEmpty()) {
			sb.append(String.format(" GROUP BY %s", this.groupBy));
		}
		return sb.toString();
	}

	protected String getInitalSelect() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (int i = 0; i < this.selectedColumns.size(); i++) {
			sb.append(this.selectedColumns.get(i));
			if (i != this.selectedColumns.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append(" FROM ");
		sb.append(this.fromTable);
		sb.append(" LEFT OUTER JOIN ");
		sb.append(this.joinTable);
		sb.append(String.format(" ON (%1$s.%2$s = %3$s.%2$s)", this.fromTable, this.commonAttribute, this.joinTable));
		if (this.groupBy != null && !this.groupBy.isEmpty()) {
			sb.append(String.format(" GROUP BY %s", this.groupBy));
		}
		return sb.toString();
	}

	public List<String> getSelectedColumns() {
		return this.selectedColumns;
	}

	public void setSelectedColumns(final List<String> selectedColumns) {
		this.selectedColumns = selectedColumns;
	}

	public String getFromTable() {
		return this.fromTable;
	}

	public void setFromTable(final String fromTable) {
		this.fromTable = fromTable;
	}

	public String getJoinTable() {
		return this.joinTable;
	}

	public void setJoinTable(final String joinTable) {
		this.joinTable = joinTable;
	}

	public String getCommonAttribute() {
		return this.commonAttribute;
	}

	public void setCommonAttribute(final String commonAttribute) {
		this.commonAttribute = commonAttribute;
	}

	public int getCounter() {
		return this.counter;
	}

	public void setCounter(final int counter) {
		this.counter = counter;
	}

	public String getGroupBy() {
		return this.groupBy;
	}

	public void setGroupBy(final String groupBy) {
		this.groupBy = groupBy;
	}

}
