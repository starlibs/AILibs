package jaicore.basic.chunks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;

import jaicore.basic.SQLAdapter;

public class TaskChunkUtil {
	public static TaskChunk<Task> readFromCSV(final String[] columns, final File csvFile, final Map<String, String> commonFields) throws IOException {
		TaskChunk<Task> chunk = new TaskChunk<>("chunkID=csvChunk");
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().equals("") || line.trim().startsWith("#")) {
					continue;
				}

				line = line.replaceAll("\"", "");
				line = line.replaceAll(",", ";");
				String lineSplit[] = line.split(";");

				if (lineSplit.length != columns.length) {
					System.out
							.println("Malformed line in csv file: " + "Number of column heads " + columns.length + " Number of columns in line: " + lineSplit.length + " " + line);
				}

				Task t = new Task();
				t.setChunk(chunk);
				for (int i = 0; i < columns.length; i++) {
					t.store(columns[i], lineSplit[i]);

					for (Entry<String, String> commonEntry : commonFields.entrySet()) {
						t.store(commonEntry.getKey(), commonEntry.getValue());
					}
				}
				chunk.add(t);

			}
		}
		return chunk;
	}
	
	public static TaskChunk<Task> readFromMySQLResultSet(final ResultSet rs, final Map<String, String> commonFields) throws Exception {
		TaskChunk<Task> chunk = new TaskChunk<>("chunkID=mySQLChunk");
		int n = rs.getMetaData().getColumnCount();
		while (rs.next()) {

			Task t = new Task();
			t.setChunk(chunk);
			for (int i = 1; i <= n; i++) {
				t.store(rs.getMetaData().getColumnLabel(i), rs.getString(i));

				for (Entry<String, String> commonEntry : commonFields.entrySet()) {
					t.store(commonEntry.getKey(), commonEntry.getValue());
				}
			}
			chunk.add(t);

		}
		return chunk;
	}

	public static TaskChunk<Task> readFromMySQLTable(final SQLAdapter adapter, final String table, final Map<String, String> commonFields) throws Exception {
		return readFromMySQLResultSet(adapter.getRowsOfTable(table), commonFields);
		
	}
}
