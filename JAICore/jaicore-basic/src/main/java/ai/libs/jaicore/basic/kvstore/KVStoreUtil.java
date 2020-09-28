package ai.libs.jaicore.basic.kvstore;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.api4.java.datastructure.kvstore.IKVStore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.db.IDatabaseAdapter;

/**
 * A util for serializing and deserializing {@link KVStoreCollection}s from and to certain formats.
 * For example, one can read in a KVStoreCollection from SQL queries/result sets and csv data or generate
 * csv-formatted or latex-formatted data tables.
 *
 * @author mwever
 */
public class KVStoreUtil {

	private KVStoreUtil() {
		// prevent instantiation of this util class.
	}

	public static String kvStoreCollectionToLaTeXTable(final KVStoreCollection kvStoreCollection, final String rowIndex, final String columnIndex, final String cellFormatting) {
		return kvStoreCollectionToLaTeXTable(kvStoreCollection, rowIndex, columnIndex, cellFormatting, "");
	}

	/**
	 * Transforms a {@link KVStoreCollection} into a LaTeX table (string).
	 * @param kvStoreCollection The {@link KVStoreCollection} to be transformed.
	 * @param rowIndex The key which shall be taken as a row index.
	 * @param columnIndex The key which shall be taken as a column index.
	 * @param cellFormatting A description of how a cell of the LaTeX table should be formatted.
	 * @param standardValue A default value for empty entries.
	 * @return A string representing the data of the {@link KVStoreCollection} in LaTeX formatting.
	 */
	public static String kvStoreCollectionToLaTeXTable(final KVStoreCollection kvStoreCollection, final String rowIndex, final String columnIndex, final String cellFormatting, final String missingEntry) {
		StringBuilder sb = new StringBuilder();
		Table<String> table = new Table<>();
		for (IKVStore store : kvStoreCollection) {

			String[] cellFormattingSplit = store.getAsString(cellFormatting).split("#");
			List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

			String rowValue = store.getAsString(rowIndex).replace("\\_", "\\\\_");
			String columnValue = store.getAsString(columnIndex).replace("\\_", "\\\\_");

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

	/**
	 * Transforms a {@link KVStoreCollection} into a CSV table (string).
	 * @param kvStoreCollection The {@link KVStoreCollection} to be transformed.
	 * @param rowIndex The key which shall be taken as a row index.
	 * @param columnIndex The key which shall be taken as a column index.
	 * @param cellFormatting A description of how a cell of the CSV table should be formatted.
	 * @param standardValue A default value for empty entries.
	 * @return A string representing the data of the {@link KVStoreCollection} in CSV formatting.
	 */
	public static String kvStoreCollectionToCSVTable(final KVStoreCollection kvStoreCollection, final String rowIndex, final String columnIndex, final String cellFormatting, final String standardValue) {
		return kvStoreCollectionToTable(kvStoreCollection, rowIndex, columnIndex, cellFormatting, standardValue).toCSV(standardValue);
	}

	/**
	 * Translates a {@link KVStoreCollection} into a Table object.
	 * @param kvStoreCollection The collection of kvstores that is to be translated.
	 * @param rowIndex The key which shall be taken as a row index.
	 * @param columnIndex The key which shall be taken as a column index.
	 * @param cellFormatting A description on how a cell of the table should be formatted.
	 * @param standardValue A default value for empty cells of the table.
	 * @return A table object representing the KVStoreCollection's data for the column and row indices.
	 */
	public static Table<String> kvStoreCollectionToTable(final KVStoreCollection kvStoreCollection, final String rowIndex, final String columnIndex, final String cellFormatting, final String standardValue) {
		Table<String> table = new Table<>();
		for (IKVStore store : kvStoreCollection) {

			String[] cellFormattingSplit = store.getAsString(cellFormatting).split("#");
			List<String> cleanedCellFormatting = Arrays.stream(cellFormattingSplit).filter(x -> !x.equals("")).collect(Collectors.toList());

			String rowValue = store.getAsString(rowIndex).replace("\\_", "\\\\_");
			String columnValue = store.getAsString(columnIndex).replace("\\_", "\\\\_");

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

	/**
	 * Parses a CSV file with a header line into a KVStoreCollection. Via the commonFields map static key-value pairs can be added to each entry.
	 *
	 * @param csvFile The file containing the csv data.
	 * @param commonFields Map containing static key-value pairs that are added to each {@link IKVStore}.
	 * @return A collection of {@link IKVStore}s containing the CSV data.
	 * @throws IOException Thrown if there are issues reading the csv file.
	 */
	public static KVStoreCollection readFromCSVWithHeader(final File csvFile, final Map<String, String> commonFields) throws IOException {
		return readFromCSVWithHeader(csvFile, commonFields, ";");
	}

	/**
	 * Parses a CSV file with a header line into a KVStoreCollection. Via the commonFields map static key-value pairs can be added to each entry.
	 *
	 * @param csvFile The file containing the csv data.
	 * @param commonFields Map containing static key-value pairs that are added to each {@link IKVStore}.
	 * @return A collection of {@link IKVStore}s containing the CSV data.
	 * @throws IOException Thrown if there are issues reading the csv file.
	 */
	public static KVStoreCollection readFromCSVWithHeader(final File csvFile, final Map<String, String> commonFields, final String separator) throws IOException {
		return readFromCSVDataWithHeader(FileUtil.readFileAsList(csvFile), commonFields, separator);
	}

	/**
	 * Parses a CSV file with no header line into a KVStoreCollection. Via the commonFields map static key-value pairs can be added to each entry.
	 *
	 * @param columns A description of the columns of the csv file.
	 * @param csvFile The file containing the csv data.
	 * @param commonFields Map containing static key-value pairs that are added to each {@link IKVStore}.
	 * @return A collection of {@link IKVStore}s containing the CSV data.
	 * @throws IOException Thrown if there are issues reading the csv file.
	 */
	public static KVStoreCollection readFromCSV(final String[] columns, final File csvFile, final Map<String, String> commonFields) throws IOException {
		return readFromCSV(columns, csvFile, commonFields, ";");
	}

	/**
	 * Parses a CSV file with no header line into a KVStoreCollection. Via the commonFields map static key-value pairs can be added to each entry.
	 *
	 * @param columns A description of the columns of the csv file.
	 * @param csvFile The file containing the csv data.
	 * @param commonFields Map containing static key-value pairs that are added to each {@link IKVStore}.
	 * @param separator The separator dividing the respective entries.
	 * @return A collection of {@link IKVStore}s containing the CSV data.
	 * @throws IOException Thrown if there are issues reading the csv file.
	 */
	public static KVStoreCollection readFromCSV(final String[] columns, final File csvFile, final Map<String, String> commonFields, final String separator) throws IOException {
		return readFromCSVData(FileUtil.readFileAsList(csvFile), columns, commonFields, separator);
	}

	/**
	 * Interprets a list of strings as a line-wise csv description and parases this into a {@link KVStoreCollection}. The first line is assumed to contain the columns' names.
	 *
	 * @param data The list of CSV-styled strings.
	 * @param commonFields Map containing static key-value pairs that are added to each {@link IKVStore}.
	 * @param separator The separator dividing the respective entries.
	 * @return A collection of {@link IKVStore}s containing the CSV data.
	 * @throws IOException Thrown if there are issues reading the csv file.
	 */
	public static KVStoreCollection readFromCSVDataWithHeader(final List<String> data, final Map<String, String> commonFields, final String separator) {
		return readFromCSVData(data, data.remove(0).split(separator), commonFields, separator);
	}

	/**
	 * Interprets a list of strings as a line-wise csv description and parases this into a {@link KVStoreCollection}.
	 *
	 * @param columns A description of the columns of the csv data.
	 * @param data The list of CSV-styled strings.
	 * @param commonFields Map containing static key-value pairs that are added to each {@link IKVStore}.
	 * @param separator The separator dividing the respective entries.
	 * @return A collection of {@link IKVStore}s containing the CSV data.
	 * @throws IOException Thrown if there are issues reading the csv file.
	 */
	public static KVStoreCollection readFromCSVData(final List<String> data, final String[] columns, final Map<String, String> commonFields, final String separator) {
		KVStoreCollection kvStoreCollection = new KVStoreCollection();
		for (String line : data) {
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
		return kvStoreCollection;
	}

	private static KVStore readLine(final String[] columns, final String line, final String separator) {
		String[] lineSplit = new String[columns.length];
		String remainingString = line;
		int currentI = 0;
		while (remainingString.contains(separator)) {
			int nextSepIndex = remainingString.indexOf(separator);
			lineSplit[currentI++] = remainingString.substring(0, nextSepIndex);
			remainingString = remainingString.substring(nextSepIndex + 1);
		}
		lineSplit[currentI] = remainingString;

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

	/**
	 * Reads the result set of an sql query into a {@link KVStoreCollection}.
	 * @param rs The result set to be parsed into a {@link KVStoreCollection}.
	 * @param commonFields A map of key-value pairs that are common to each {@link IKVStore}
	 * @return A collection of {@link IKVStore}s containing the data of the result set.
	 * @throws SQLException Thrown, if there were problems regarding the processing of the SQL result set.
	 */
	public static KVStoreCollection readFromMySQLResultSet(final ResultSet rs, final Map<String, String> commonFields) throws SQLException {
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

	/**
	 * Reads the result set of the given sql query into a {@link KVStoreCollection}.
	 * @param adapter The sql adapter which is to be used to execute the query.
	 * @param query The result set to be parsed into a {@link KVStoreCollection}.
	 * @param commonFields A map of key-value pairs that are common to each {@link IKVStore}
	 * @return A collection of {@link IKVStore}s containing the data of the result set.
	 * @throws SQLException Thrown, if there were problems regarding the processing of the SQL result set.
	 */
	public static KVStoreCollection readFromMySQLQuery(final IDatabaseAdapter adapter, final String query, final Map<String, String> commonFields) throws SQLException {
		return addCommonFields(new KVStoreCollection(adapter.getResultsOfQuery(query)), commonFields);
	}

	/**
	 * Reads all rows of an SQL table into a collection of {@link IKVStore}s where each row corresponds to a IKVStore in this collection.
	 *
	 * @param adapter An {@link SQLAdapter} to issue the query to the database.
	 * @param table The table from which to read in the rows.
	 * @param commonFields Static key-value pairs which should be added to all read-in {@link IKVStore}s.
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public static KVStoreCollection readFromMySQLTable(final IDatabaseAdapter adapter, final String table, final Map<String, String> commonFields) throws SQLException {
		return addCommonFields(new KVStoreCollection(adapter.getRowsOfTable(table)), commonFields);

	}

	private static KVStoreCollection addCommonFields(final KVStoreCollection collection, final Map<String, String> commonFields) {
		collection.stream().forEach(x -> x.putAll(commonFields));
		return collection;
	}

	public static KVStoreCollection readFromJson(final JsonNode res) {
		if (res instanceof ArrayNode) {
			return readFromJsonArray((ArrayNode) res);
		} else {
			return readFromJsonArray((ArrayNode) res.get(0));
		}
	}

	public static KVStoreCollection readFromJsonArray(final ArrayNode list) {
		KVStoreCollection col = new KVStoreCollection();
		for (JsonNode node : list) {
			col.add(readObjectNodeToKVStore((ObjectNode) node));
		}
		return col;
	}

	private static IKVStore readObjectNodeToKVStore(final ObjectNode node) {
		Iterator<String> fieldNameIt = node.fieldNames();
		IKVStore store = new KVStore();

		while (fieldNameIt.hasNext()) {
			String fieldName = fieldNameIt.next();
			JsonNode value = node.get(fieldName);
			switch (value.getNodeType()) {
			case BOOLEAN:
				store.put(fieldName, value.asBoolean());
				break;
			case MISSING:
				store.put(fieldName, null);
				break;
			case NULL:
				store.put(fieldName, null);
				break;
			case NUMBER:
				store.put(fieldName, value.asText());
				break;
			case STRING:
				store.put(fieldName, value.asText());
				break;
			default:
				store.put(fieldName, value.asText());
				break;
			}
		}
		return store;
	}

}
