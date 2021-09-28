package ai.libs.jaicore.db.sql;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import ai.libs.jaicore.db.IDatabaseAdapter;

public abstract class ASqlBasedAdapter implements IDatabaseAdapter {

	/**
	 *
	 */
	private static final long serialVersionUID = 153647641292144496L;

	protected static final String KEY_EQUALS_VALUE_TO_BE_SET = " = (?)";
	protected static final String STR_SPACE_AND = " AND ";
	protected static final String STR_SPACE_WHERE = " WHERE ";

	protected ASqlBasedAdapter() {
	}

	@Override
	public int delete(final String table, final Map<String, ? extends Object> conditions) throws SQLException {
		StringBuilder conditionSB = new StringBuilder();
		for (Entry<String, ? extends Object> entry : conditions.entrySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(STR_SPACE_AND);
			}
			if (entry.getValue() != null) {
				conditionSB.append(entry.getKey() + KEY_EQUALS_VALUE_TO_BE_SET);
			} else {
				conditionSB.append(entry.getKey());
				conditionSB.append(" IS NULL");
			}
		}
		return this.update("DELETE FROM `" + table + "`" + STR_SPACE_WHERE + " " + conditionSB);
	}

}
