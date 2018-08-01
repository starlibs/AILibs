package autofe.db.util;

import java.util.List;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.Attribute;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardFeature;
import autofe.db.model.database.Path;
import autofe.db.model.relation.AbstractRelationship;
import autofe.db.model.relation.ForwardRelationship;

public class SqlUtils {

	private static final String GENERAL_PREFIX = "FE_";
	private static final String FORWARD_PREFIX = "FWD";
	private static final String BACKWARD_PREFIX = "BWD";

	public static String replacePlaceholder(String in, int index, String replacement) {
		String placeholder = "$" + index;
		return in.replace(placeholder, replacement);
	}

	public static String getTableNameForFeature(AbstractFeature feature) {
		StringBuilder sb = new StringBuilder();
		sb.append(GENERAL_PREFIX);
		if (feature instanceof ForwardFeature) {
			sb.append(FORWARD_PREFIX);
			sb.append("_");
			sb.append(feature.getParent().getName());
		} else if (feature instanceof BackwardFeature) {
			sb.append(BACKWARD_PREFIX);
			sb.append("_");
			sb.append(feature.getParent().getName());
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
		String startTableName = joins.get(0).getFromTableName();
		String toTableName = joins.get(joins.size() - 1).getToTableName();

		ForwardRelationship firstJoin = joins.get(0);
		firstJoin.setContext(db);
		Attribute primaryKey = DBUtils.getPrimaryKey(firstJoin.getFrom(), db);

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("SELECT %1$s.%2$s, %3$s.%4$s FROM %1$s ", startTableName, primaryKey.getName(), toTableName,
				feature.getParent().getName()));
		for (ForwardRelationship join : joins) {
			sb.append(String.format("JOIN %1s ON (%1$s.%2$s = %3$s.%2$s)", join.getToTableName(),
					join.getCommonAttributeName(), join.getFromTableName()));
		}
		return sb.toString();
	}

}
