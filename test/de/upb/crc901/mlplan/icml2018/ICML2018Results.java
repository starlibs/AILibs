package de.upb.crc901.mlplan.icml2018;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import de.upb.crc901.mlplan.core.MySQLMultiLabelExperimentLogger;

public class ICML2018Results {

	private final static MySQLMultiLabelExperimentLogger expLogger = new MySQLMultiLabelExperimentLogger("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X",
			"mlplan_multilabel_results");

	public static void main(String[] args) throws Exception {
		try {
			ResultSet rs = expLogger.getResults("icml2018");
			System.out.println(resultSet2LatexTable(rs));
		} finally {
			expLogger.close();
		}
	}

	public static String resultSet2LatexTable(ResultSet rs) throws SQLException {
		StringBuilder sb = new StringBuilder();
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();

		while (rs.next()) {
			for (int col = 1; col <= cols; col++) {
				if (col > 1)
					sb.append("&");

				/* if this is a mean column */
				if (meta.getColumnLabel(col).toLowerCase().contains("mean") && meta.getColumnLabel(col + 1).toLowerCase().contains("std")) {
					if (rs.getString(col) != null) {
						sb.append("$");
						sb.append(rs.getDouble(col));
						sb.append("\\pm");
						sb.append(rs.getDouble(col + 1));
						sb.append("$");
					} else {
						sb.append("-");
					}
					col++;
					continue;
				}

				switch (meta.getColumnType(col)) {
				case Types.INTEGER: {
					sb.append(rs.getInt(col));
					break;
				}
				default: {
					sb.append(rs.getString(col));
					break;
				}
				}
			}
			sb.append("\\\\\n");
		}
		return sb.toString();
	}
}
