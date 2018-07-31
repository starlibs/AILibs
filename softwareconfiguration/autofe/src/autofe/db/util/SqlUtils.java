package autofe.db.util;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.ForwardFeature;
import autofe.db.model.database.Path;
import autofe.db.model.relation.AbstractRelationship;

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

}
