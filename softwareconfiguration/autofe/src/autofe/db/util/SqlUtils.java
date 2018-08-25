package autofe.db.util;

import java.util.ArrayList;
import java.util.List;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.Attribute;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardFeature;
import autofe.db.model.database.Path;
import autofe.db.model.database.Table;
import autofe.db.model.relation.AbstractRelationship;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;

public class SqlUtils {

	private static final String GENERAL_PREFIX = "FE_";
	private static final String FORWARD_PREFIX = "FWD";
	private static final String BACKWARD_PREFIX = "BWD";

	private static final String TEMP_FEATURE = "TEMPFEATURE";
	protected static final String TEMP_TABLE = "TEMPTABLE";

	public static String replacePlaceholder(String in, int index, String replacement) {
		String placeholder = "$" + index;
		return in.replace(placeholder, replacement);
	}

	public static String getTableNameForFeature(AbstractFeature feature) {
		StringBuilder sb = new StringBuilder();
		sb.append(GENERAL_PREFIX);
		if (feature instanceof ForwardFeature) {
			String parentFullName = feature.getParent().getFullName();
			parentFullName = parentFullName.replace(".", "_");
			parentFullName = parentFullName.toUpperCase();
			sb.append(FORWARD_PREFIX);
			sb.append("_");
			sb.append(parentFullName);
		} else if (feature instanceof BackwardFeature) {
			String parentFullName = feature.getParent().getFullName();
			parentFullName = parentFullName.replace(".", "_");
			parentFullName = parentFullName.toUpperCase();
			sb.append(BACKWARD_PREFIX);
			sb.append("_");
			sb.append(parentFullName);
			sb.append("_");
			Path path = ((BackwardFeature) feature).getPath();
			for (int i = 0; i < path.length(); i++) {
				Tuple<AbstractRelationship, AggregationFunction> pathElement = path.getPathElements().get(i);
				AbstractRelationship ar = pathElement.getT();
				if (pathElement.getU() != null) {
					sb.append(pathElement.getU());
				}
				sb.append(ar.getFromTableName());
				if (i != path.length() - 1) {
					sb.append("_");
				}
			}
		}
		return sb.toString().toUpperCase();
	}

	public static String generateForwardSql(List<ForwardRelationship> joins, ForwardFeature feature, Database db) {
		String startTableName, toTableName;
		Table startTable;

		if (joins == null || joins.isEmpty()) {
			// Feature is part of the target table
			startTable = DBUtils.getAttributeTable(feature.getParent(), db);
			startTableName = startTable.getName();
			toTableName = startTableName;
		} else {
			// Feature is in another table
			ForwardRelationship firstJoin = joins.get(0);
			firstJoin.setContext(db);
			startTable = firstJoin.getFrom();
			startTableName = joins.get(0).getFromTableName();
			toTableName = joins.get(joins.size() - 1).getToTableName();
		}

		Attribute primaryKey = DBUtils.getPrimaryKey(startTable, db);
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("SELECT %1$s.%2$s, %3$s.%4$s FROM %1$s ", escape(startTableName),
				escape(primaryKey.getName()), escape(toTableName), escape(feature.getParent().getName())));
		if (joins == null) {
			return sb.toString();
		}
		for (ForwardRelationship join : joins) {
			sb.append(String.format("JOIN %1s ON (%1$s.%2$s = %3$s.%2$s)", escape(join.getToTableName()),
					escape(join.getCommonAttributeName()), escape(join.getFromTableName())));
		}
		return sb.toString();
	}

	public static String generateBackwardSql(List<ForwardRelationship> joins, BackwardFeature feature, Database db) {
		Path path = new Path(feature.getPath());
		
		// Add joins to path
		for (ForwardRelationship fr : joins) {
			path.addPathElement(fr, null);
		}

		// Create select parts
		List<SelectPart> selectParts = new ArrayList<>();
		for (int i = 0; i < path.length(); i++) {
			Tuple<AbstractRelationship, AggregationFunction> pathElement = path.getPathElements().get(i);
			AbstractRelationship ar = pathElement.getT();
			ar.setContext(db);
			SelectPart sp = new SelectPart();
			sp.commonAttribute = escape(ar.getCommonAttributeName());
			sp.counter = i;
			sp.fromTable = escape(ar.getFromTableName());
			sp.joinTable = escape(ar.getToTableName());

			List<String> selectedColumns = new ArrayList<>();

			// Join attribute
			if (i != path.length() - 1) {
				selectedColumns.add(
						String.format("%s.%s", escape(ar.getFromTableName()), escape(ar.getCommonAttributeName())));
			}

			// Join attribute for next join
			if (i != path.length() - 1) {
				Tuple<AbstractRelationship, AggregationFunction> nextPathElement = path.getPathElements().get(i + 1);
				String joinAttribute = String.format("%s.%s", escape(ar.getFromTableName()),
						escape(nextPathElement.getT().getCommonAttributeName()));
				if (!selectedColumns.contains(joinAttribute)) {
					selectedColumns.add(joinAttribute);
				}
			}

			if (ar instanceof BackwardRelationship) {

				// Aggregated attribute
				if (i != 0 && i != path.length() - 1) {
					selectedColumns.add(String.format("%s(%s) AS %s", pathElement.getU(),
							escape(TEMP_FEATURE + (i - 1)), TEMP_FEATURE + i));
				} else if (i == path.length() - 1) {
					selectedColumns.add(String.format("%s(%s) AS '%s'", pathElement.getU(),
							escape(TEMP_FEATURE + (i - 1)), feature.getName()));
				} else {
					selectedColumns.add(String.format("%s(%s.%s) AS %s", pathElement.getU(),
							escape(ar.getToTableName()), escape(feature.getParent().getName()), TEMP_FEATURE + i));
				}

				sp.groupBy = String.format("%s.%s", escape(ar.getFromTableName()), escape(ar.getCommonAttributeName()));

			} else if (ar instanceof ForwardRelationship) {
				if (i != path.length() - 1) {
					selectedColumns.add(String.format("%s AS %s", escape(TEMP_FEATURE + (i - 1)), TEMP_FEATURE + i));
				} else {
					selectedColumns.add(String.format("%s AS '%s'", escape(TEMP_FEATURE + (i - 1)), feature.getName()));
				}
			}

			// Primary key (if not already present)
			if (i == path.length() - 1) {
				Table firstTable = ar.getFrom();
				Attribute primaryKey = DBUtils.getPrimaryKey(firstTable, db);
				String primaryKeySelect = String.format("%s.%s", escape(firstTable.getName()),
						escape(primaryKey.getName()));
				if (!selectedColumns.contains(primaryKeySelect)) {
					selectedColumns.add(primaryKeySelect);
				}
			}

			sp.selectedColumns = selectedColumns;
			selectParts.add(sp);
		}

		// Create final SQL
		String sql = "";
		for (int i = 0; i < selectParts.size(); i++) {
			SelectPart sp = selectParts.get(i);
			if (i != 0) {
				sql = String.format("%s (%s) %s", sp.part1(), sql, sp.part2());
			} else {
				sql = sp.getInitalSelect();
			}
		}

		return sql;
	}

	static String escape(String toEscape) {
		return "`" + toEscape + "`";
	}

}

class SelectPart {
	public List<String> selectedColumns;
	public String fromTable;
	public String joinTable;
	public String commonAttribute;
	public int counter;
	public String groupBy;

	String part1() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (int i = 0; i < selectedColumns.size(); i++) {
			sb.append(selectedColumns.get(i));
			if (i != selectedColumns.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append(" FROM ");
		sb.append(fromTable);
		sb.append(" LEFT OUTER JOIN");
		return sb.toString();
	}

	String part2() {
		StringBuilder sb = new StringBuilder();
		sb.append(SqlUtils.TEMP_TABLE + counter);
		sb.append(String.format(" ON (%1$s.%2$s = %3$s.%2$s)", fromTable, commonAttribute,
				SqlUtils.escape(SqlUtils.TEMP_TABLE + counter)));
		if (groupBy != null && !groupBy.isEmpty()) {
			sb.append(String.format(" GROUP BY %s", groupBy));
		}
		return sb.toString();
	}

	String getInitalSelect() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (int i = 0; i < selectedColumns.size(); i++) {
			sb.append(selectedColumns.get(i));
			if (i != selectedColumns.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append(" FROM ");
		sb.append(fromTable);
		sb.append(" LEFT OUTER JOIN ");
		sb.append(joinTable);
		sb.append(String.format(" ON (%1$s.%2$s = %3$s.%2$s)", fromTable, commonAttribute, joinTable));
		if (groupBy != null && !groupBy.isEmpty()) {
			sb.append(String.format(" GROUP BY %s", groupBy));
		}
		return sb.toString();
	}
}
