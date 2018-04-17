package de.upb.crc901.mlplan.icml2018;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import de.upb.crc901.mlplan.multilabel.MySQLMultiLabelExperimentLogger;

public class ICML2018Results {

	private final static MySQLMultiLabelExperimentLogger expLogger = new MySQLMultiLabelExperimentLogger("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X",
			"mlplan_multilabel_results");

	public static void main(String[] args) throws Exception {
		try {
			Map<String,String> replacement = new HashMap<>();
			replacement.put("Arts1: -C -26", "arts");
			replacement.put("bibsonomy_bibtex: -C -159", "bibtex");
			replacement.put("bibsonomy_bookmarks: -C -208", "bookmarks");
			replacement.put("flags_ml: -C -12", "flags");
			
			String[] tables = {"icml2018_f1", "icml2018_exact", "icml2018_hamming", "icml2018_jaccard", "icml2018_rank"};
			for (String table : tables) {
				ResultSet rs = expLogger.getResults(table);
				System.out.println(resultSet2LatexTable(rs, replacement));
			}
		} finally {
			expLogger.close();
		}
	}

	public static String resultSet2LatexTable(ResultSet rs, Map<String,String> replacements) throws SQLException {
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
					String str = rs.getString(col);
					for (String key : replacements.keySet()) {
						str = str.replaceAll(key, replacements.get(key));
					}
					sb.append(str);
					break;
				}
				}
			}
			sb.append("\\\\\n");
		}
		return sb.toString();
	}
}
