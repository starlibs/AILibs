package jaicore.basic.kvstore;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Allows to arrange data of type V into a matrix structure. Filling the table works providing a column key and a row key.
 *
 * @author wever
 *
 * @param <V>
 *            Datatype of an entry's value.
 */
public class Table<V> {

	/** Standard separator for CSV data */
	private static final String STANDARD_CSV_SEPARATOR = ";";

	/** List of existing column names. */
	private final List<String> columnIndex = new LinkedList<>();
	/** List of existing row names. */
	private final List<String> rowIndex = new LinkedList<>();

	/** Data structure mapping rows to columns to data. */
	private final Map<String, Map<String, V>> tableData = new HashMap<>();

	/**
	 * Standard c'tor.
	 */
	public Table() {

	}

	/**
	 * Adds a value to the cell identified by the columnIndexValue and the rowIndexValue. Already existing values are overwritten.
	 *
	 * @param columnIndexValue
	 *            The name of the column.
	 * @param rowIndexValue
	 *            The name of the row.
	 * @param entry
	 *            The entry to add to the table's cell.
	 */
	public void add(final String columnIndexValue, final String rowIndexValue, final V entry) {
		Map<String, V> selectedRow = this.tableData.get(rowIndexValue);
		if (selectedRow == null) {
			selectedRow = new HashMap<>();
			this.tableData.put(rowIndexValue, selectedRow);
			if (!this.rowIndex.contains(rowIndexValue)) {
				this.rowIndex.add(rowIndexValue);
			}
		}
		if (!this.columnIndex.contains(columnIndexValue)) {
			this.columnIndex.add(columnIndexValue);
		}
		selectedRow.put(columnIndexValue, entry);
	}

	/**
	 * Converts the table to latex code, empty cells are filled with an empty string.
	 *
	 * @return The latex code representing this table.
	 */
	public String toLaTeX() {
		return this.toLaTeX("");
	}

	/**
	 * Converts the table to latex code, empty cells are filled with the provided missingEntry String.
	 *
	 * @param missingEntry
	 *            Value of empty table cells.
	 * @return The latex code representing this table.
	 */
	public String toLaTeX(final String missingEntry) {
		StringBuilder sb = new StringBuilder();

		sb.append("\\begin{tabular}{");
		for (int i = 0; i < this.columnIndex.size() + 1; i++) {
			sb.append("l");
		}
		sb.append("}");

		Collections.sort(this.columnIndex);
		for (String c : this.columnIndex) {
			sb.append("&");
			// sb.append("\\rotatebox{90}{");
			sb.append(c);
			// sb.append("}");
		}
		sb.append("\\\\\n");

		// Collections.sort(this.rowIndex);
		for (String r : this.rowIndex) {
			sb.append(r);

			for (String c : this.columnIndex) {
				sb.append(" & ");
				Map<String, V> selectRow = this.tableData.get(r);
				if (selectRow != null) {
					V entry = selectRow.get(c);
					if (entry != null) {
						sb.append(entry.toString().replaceAll("_", "\\_"));
					} else {
						sb.append(missingEntry);
					}
				}
			}
			sb.append("\\\\\n");
		}

		sb.append("\\end{tabular}");
		return sb.toString();
	}

	/**
	 * Converts the table into CSV format.
	 *
	 * @param standardValue
	 *            Value to assign for non-existing entries.
	 * @return String in CSV format describing the data of the table.
	 */
	public String toCSV(final String standardValue) {
		return this.toCSV(STANDARD_CSV_SEPARATOR, standardValue);
	}

	/**
	 * Converts the table into CSV format.
	 *
	 * @param separator
	 *            The symbol to separate values in the CSV format.
	 * @param standardValue
	 *            Value to assign for non-existing entries.
	 * @return String in CSV format describing the data of the table.
	 */
	public String toCSV(final String separator, final String standardValue) {
		StringBuilder sb = new StringBuilder();

		// Collections.sort(this.columnIndex);
		Collections.sort(this.rowIndex);

		boolean first = true;
		for (String c : this.columnIndex) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}
			sb.append(c);
		}
		sb.append("\n");
		for (String r : this.rowIndex) {
			// sb.append(r);

			first = true;

			for (String c : this.columnIndex) {
				if (first) {
					first = false;
				} else {
					sb.append(separator);
				}

				Map<String, V> selectRow = this.tableData.get(r);
				if (selectRow != null) {
					V entry = selectRow.get(c);
					if (entry != null) {
						sb.append(entry.toString());
					} else {
						sb.append(standardValue);
					}
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * @return Returns the index of columns of this table.
	 */
	public List<String> getColumnsOfCSV() {
		return this.columnIndex;
	}

	/**
	 * @return Returns the index of rows of this table.
	 */
	public List<String> getRowsOfCSV() {
		return this.rowIndex;
	}

	/**
	 * Allows to sort the columns of the table.
	 *
	 * @param c
	 *            The comparator to use for sorting.
	 */
	public void sortColumns(final Comparator<? super String> c) {
		this.columnIndex.sort(c);
	}

	/**
	 * Allows to sort the rows of the table.
	 *
	 * @param c
	 *            The comparator to use for sorting.
	 */
	public void sortRows(final Comparator<? super String> c) {
		this.rowIndex.sort(c);
	}

}
