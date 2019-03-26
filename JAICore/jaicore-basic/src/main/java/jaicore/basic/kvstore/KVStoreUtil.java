package jaicore.basic.kvstore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jaicore.basic.SQLAdapter;
import jaicore.basic.ValueUtil;

public class KVStoreUtil {

	public static String kvStoreCollectionToLaTeXTable(final KVStoreCollection kvStoreCollection, final String rowIndex, final String columnIndex) {
		StringBuilder sb = new StringBuilder();
		Table<String> table = new Table<>();
		for (KVStore store : kvStoreCollection) {
			String rowValue = store.getAsString(rowIndex).replaceAll("\\_", "\\\\_");
			String columnValue = store.getAsString(columnIndex).replaceAll("\\_", "\\\\_");
			String tableEntry = ValueUtil.valueToString(store.getAsDouble("meanAccuracy"), 2) + "+-" + ValueUtil.valueToString(store.getAsDouble("stdAccuracy"), 2);
			table.add(columnValue, rowValue, tableEntry);
		}
		sb.append(table.toLaTeX());
		return sb.toString();
	}

	public static String kvStoreCollectionToLaTeXTable(final KVStoreCollection kvStoreCollection, final String rowIndex, final String columnIndex, final String cellFormatting) {

		StringBuilder sb = new StringBuilder();
		Table<String> table = new Table<>();
		for (KVStore store : kvStoreCollection) {

			String[] cellFormattingSplit = store.getAsString(cellFormatting).split("#");
			List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

			String rowValue = store.getAsString(rowIndex).replaceAll("\\_", "\\\\_");
			String columnValue = store.getAsString(columnIndex).replaceAll("\\_", "\\\\_");

			StringBuilder tableEntryBuilder = new StringBuilder();
			for (String cellKey : cleanedCellFormatting) {
				if (!store.containsKey(cellKey)) {
					tableEntryBuilder.append(cellKey);
				} else {
					tableEntryBuilder.append(store.getAsString(cellKey));
				}
			}

			table.add(columnValue, rowValue, tableEntryBuilder.toString());
		}
		sb.append(table.toLaTeX());
		return sb.toString();
	}

	public static String kvStoreCollectionToLaTeXTable(final KVStoreCollection kvStoreCollection, final String rowIndex, final String columnIndex, final String cellFormatting, final String missingEntry) {

		StringBuilder sb = new StringBuilder();
		Table<String> table = new Table<>();
		for (KVStore store : kvStoreCollection) {

			String[] cellFormattingSplit = store.getAsString(cellFormatting).split("#");
			List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

			String rowValue = store.getAsString(rowIndex).replaceAll("\\_", "\\\\_");
			String columnValue = store.getAsString(columnIndex).replaceAll("\\_", "\\\\_");

			StringBuilder tableEntryBuilder = new StringBuilder();
			for (String cellKey : cleanedCellFormatting) {
				if (!store.containsKey(cellKey)) {
					tableEntryBuilder.append(cellKey);
				} else {
					tableEntryBuilder.append(store.getAsString(cellKey));
				}
			}

			table.add(columnValue, rowValue, tableEntryBuilder.toString());
		}
		sb.append(table.toLaTeX(missingEntry));
		return sb.toString();
	}

	public static String kvStoreCollectionToCSVTable(final KVStoreCollection kvStoreCollection, final String rowIndex, final String columnIndex, final String cellFormatting, final String standardValue) {

		StringBuilder sb = new StringBuilder();
		Table<String> table = new Table<>();
		for (KVStore store : kvStoreCollection) {

			String[] cellFormattingSplit = store.getAsString(cellFormatting).split("#");
			List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

			String rowValue = store.getAsString(rowIndex).replaceAll("\\_", "\\\\_");
			String columnValue = store.getAsString(columnIndex).replaceAll("\\_", "\\\\_");

			StringBuilder tableEntryBuilder = new StringBuilder();
			for (String cellKey : cleanedCellFormatting) {
				if (!store.containsKey(cellKey)) {
					tableEntryBuilder.append(cellKey);
				} else {
					tableEntryBuilder.append(store.getAsString(cellKey));
				}
			}

			table.add(columnValue, rowValue, tableEntryBuilder.toString());
		}
		sb.append(table.toCSV(standardValue));
		return sb.toString();
	}

	public static Table<String> kvStoreCollectionToTable(final KVStoreCollection kvStoreCollection, final String rowIndex, final String columnIndex, final String cellFormatting, final String standardValue) {
		Table<String> table = new Table<>();
		for (KVStore store : kvStoreCollection) {

			String[] cellFormattingSplit = store.getAsString(cellFormatting).split("#");
			List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

			String rowValue = store.getAsString(rowIndex).replaceAll("\\_", "\\\\_");
			String columnValue = store.getAsString(columnIndex).replaceAll("\\_", "\\\\_");

			StringBuilder tableEntryBuilder = new StringBuilder();
			for (String cellKey : cleanedCellFormatting) {
				if (!store.containsKey(cellKey)) {
					tableEntryBuilder.append(cellKey);
				} else {
					tableEntryBuilder.append(store.getAsString(cellKey));
				}
			}

			table.add(columnValue, rowValue, tableEntryBuilder.toString());
		}
		return table;
	}

	public static KVStoreCollection readFromCSVWithHeader(final File csvFile, final Map<String, String> commonFields) throws IOException {
		return readFromCSVWithHeader(csvFile, commonFields, ";");
	}

	public static KVStoreCollection readFromCSVWithHeader(final File csvFile, final Map<String, String> commonFields, final String separator) throws IOException {
		KVStoreCollection kvStoreCollection = new KVStoreCollection();
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			boolean first = true;
			String[] columns = {};

			while ((line = br.readLine()) != null) {
				if (skipLine(line)) {
					continue;
				}

				// if the line is the first of the file not being empty nor a comment parse this line as a header
				if (first) {
					first = false;
					columns = line.split(separator);
					continue;
				}

				// read the line as a task
				KVStore t = readLine(columns, line, separator);

				// additionally store all the common field values
				for (Entry<String, String> commonEntry : commonFields.entrySet()) {
					t.put(commonEntry.getKey(), commonEntry.getValue());
				}

				// add to chunk
				t.setCollection(kvStoreCollection);
				kvStoreCollection.add(t);
			}
		}
		return kvStoreCollection;
	}

	public static KVStoreCollection readFromCSV(final String[] columns, final File csvFile, final Map<String, String> commonFields) throws IOException {
		return readFromCSV(columns, csvFile, commonFields, ";");
	}

	public static KVStoreCollection readFromCSV(final String[] columns, final File csvFile, final Map<String, String> commonFields, final String separator) throws IOException {
		KVStoreCollection kvStoreCollection = new KVStoreCollection();
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (skipLine(line)) {
					continue;
				}

				// read the line as a task
				KVStore t = readLine(columns, line, separator);

				// additionally store all the common field values
				for (Entry<String, String> commonEntry : commonFields.entrySet()) {
					t.put(commonEntry.getKey(), commonEntry.getValue());
				}

				// add to chunk
				t.setCollection(kvStoreCollection);
				kvStoreCollection.add(t);

			}
		}
		return kvStoreCollection;
	}

	private static KVStore readLine(final String[] columns, final String line, final String separator) {
		String lineSplit[] = line.split(separator);
		if (lineSplit.length != columns.length) {
			throw new IllegalArgumentException("Malformed line in csv file: " + "Number of column heads " + columns.length + " Number of columns in line: " + lineSplit.length + " " + line);
		}

		KVStore t = new KVStore();
		for (int i = 0; i < columns.length; i++) {
			t.put(columns[i], lineSplit[i]);
		}

		return t;
	}

	private static boolean skipLine(final String line) {
		// discard comments starting with # and empty lines
		return (line.trim().equals("") || line.trim().startsWith("#"));
	}

	public static KVStoreCollection readFromMySQLResultSet(final ResultSet rs, final Map<String, String> commonFields) throws Exception {
		KVStoreCollection kvStoreCollection = new KVStoreCollection();
		int n = rs.getMetaData().getColumnCount();
		while (rs.next()) {

			KVStore t = new KVStore();
			t.setCollection(kvStoreCollection);
			for (int i = 1; i <= n; i++) {
				t.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));

				for (Entry<String, String> commonEntry : commonFields.entrySet()) {
					t.put(commonEntry.getKey(), commonEntry.getValue());
				}
			}
			kvStoreCollection.add(t);
		}
		return kvStoreCollection;
	}

	public static KVStoreCollection readFromMySQLTable(final SQLAdapter adapter, final String table, final Map<String, String> commonFields) throws Exception {
		return readFromMySQLResultSet(adapter.getRowsOfTable(table), commonFields);

	}

}
