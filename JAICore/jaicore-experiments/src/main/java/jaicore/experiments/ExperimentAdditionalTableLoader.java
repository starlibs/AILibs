package jaicore.experiments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SQLAdapter;

/**
 * Class that creates additional tables before an experiment is started.
 * 
 * @author Helena Graf
 *
 */
public class ExperimentAdditionalTableLoader {

	private static Logger logger = LoggerFactory.getLogger(ExperimentAdditionalTableLoader.class);

	private ExperimentAdditionalTableLoader() {
	}

	/**
	 * Goes through all the files in the given directory, loads their contents and
	 * tries to execute them as SQL statements. Assumes UTF-8 Encoding of the files.
	 * Assumes that there either is only a CREATE TABLE statement or a CREATE TABLE
	 * statement and a INSERT INTO statement present in the file, no other
	 * statements, changing of the mode, setting of the time etc. Tests for the
	 * INSERT VALUES statement by String occurrence, so including the String "INSERT
	 * INTO" in the CREATE TABLE statement in any way will lead to bad behavior.
	 * 
	 * @param folder
	 *            the directory from which the files containing sql statements are loaded
	 * @param adapter
	 *            the sql connection to use for executing the statements
	 * @throws IOException
	 *             if the given folder cannot be read (does not exist, no rights,
	 *             not a directory, ...)
	 */
	public static void executeStatementsFromDirectory(final String folder, final SQLAdapter adapter) throws IOException {
		try (Stream<Path> paths = Files.walk(Paths.get(folder))) {
			paths.filter(f -> f.toFile().isFile()).forEach(file -> {
				try {
					logger.info("Execute_Statement");
					executeStatementInFile(file, adapter);
				} catch (IOException e) {
					logger.warn("Could not load sql file {} for creating tables for the experimenter: {}", file.getFileName(), e.getMessage());
				} catch (SQLException e) {
					logger.warn("Could not execute sql in the file {} and not create the table for the experimenter: {}", file.getFileName(), e.getMessage());
				}
			});
		}
	}

	private static void executeStatementInFile(final Path file, final SQLAdapter adapter) throws IOException, SQLException {
		String createTableStatement = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
		String insertValuesStatement = null;

		String insertInto = "INSERT INTO";
		if (createTableStatement.contains(insertInto)) {
			String[] parts = createTableStatement.split(insertInto);
			createTableStatement = parts[0];
			insertValuesStatement = insertInto + parts[1];
		}

		adapter.update(createTableStatement);
		logger.info("Executed SQL statement: {}", createTableStatement);

		if (insertValuesStatement != null) {
			adapter.update(insertValuesStatement);
			logger.info("Executed SQL statement: {}", createTableStatement);
		}
	}
}
