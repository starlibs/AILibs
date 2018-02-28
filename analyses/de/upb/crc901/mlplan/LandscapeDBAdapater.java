package de.upb.crc901.mlplan;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import de.upb.crc901.mlplan.core.MLPipeline;
import de.upb.crc901.mlplan.core.MySQLMLPlanExperimentLogger;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

public class LandscapeDBAdapater extends MySQLMLPlanExperimentLogger {

	public LandscapeDBAdapater() {
		super("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "mlplan_results");
	}

	public void addEntry(String dataset, MLPipeline pipeline, int score) {
		
		String searcherName = "", searcherParams = "", evaluatorName = "", evaluatorParams = "";
		if (!pipeline.getPreprocessors().isEmpty()) {
			ASSearch searcher = pipeline.getPreprocessors().get(0).getSearcher();
			ASEvaluation evaluation = pipeline.getPreprocessors().get(0).getEvaluator();
			
			searcherName = searcher.getClass().getSimpleName();
			searcherParams = searcher instanceof OptionHandler ? Arrays.toString(((OptionHandler)searcher).getOptions()) : "";
			evaluatorName = evaluation.getClass().getSimpleName();
			evaluatorParams = evaluation instanceof OptionHandler ? Arrays.toString(((OptionHandler)evaluation).getOptions()) : "";
		}
		Classifier classifier = pipeline.getBaseClassifier();
		String classifierName = classifier.getClass().getSimpleName();
		String classifierParams = classifier instanceof OptionHandler ? Arrays.toString(((OptionHandler)classifier).getOptions()) : "";
		
		PreparedStatement stmt = null;
		try {
			stmt = getPreparedStatement("INSERT INTO `landscapeanalysis` (dataset, searcher, searcherparams, evaluator, evaluatorparams, classifier, classifierparams, pipeline, score) VALUES (?,?,?,?,?,?,?,?,?)");
			stmt.setString(1, dataset);
			stmt.setString(2, searcherName);
			stmt.setString(3, searcherParams);
			stmt.setString(4, evaluatorName);
			stmt.setString(5, evaluatorParams);
			stmt.setString(6, classifierName);
			stmt.setString(7, classifierParams);
			stmt.setString(8, pipeline.toString());
			stmt.setInt(9, score);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void readDataBase() throws Exception {
		try {
			// Result set get the result of the SQL query
			ResultSet resultSet = executeQuery("SELECT * FROM `landscapeanalysis`");
			writeResultSet(resultSet);

			// PreparedStatements can use variables and are more efficient
			// preparedStatement = connect.prepareStatement("insert into feedback.comments values (default, ?, ?, ?, ? , ?, ?)");
			// // "myuser, webpage, datum, summary, COMMENTS from feedback.comments");
			// // Parameters start with 1
			// preparedStatement.setString(1, "Test");
			// preparedStatement.setString(2, "TestEmail");
			// preparedStatement.setString(3, "TestWebpage");
			// preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
			// preparedStatement.setString(5, "TestSummary");
			// preparedStatement.setString(6, "TestComment");
			// preparedStatement.executeUpdate();

			// preparedStatement = connect.prepareStatement("SELECT myuser, webpage, datum, summary, COMMENTS from feedback.comments");
			// resultSet = preparedStatement.executeQuery();
			// writeResultSet(resultSet);
			//
			// // Remove again the insert comment
			// preparedStatement = connect.prepareStatement("delete from feedback.comments where myuser= ? ; ");
			// preparedStatement.setString(1, "Test");
			// preparedStatement.executeUpdate();
			//
			// resultSet = statement.executeQuery("select * from feedback.comments");
			// writeMetaData(resultSet);

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	private void writeResultSet(ResultSet resultSet) throws SQLException {
		// ResultSet is initially before the first data set
		while (resultSet.next()) {
			// It is possible to get the columns via name
			// also possible to get the columns via the column number
			// which starts at 1
			// e.g. resultSet.getSTring(2);
			String dataset = resultSet.getString("dataset");
			String preprocessor = resultSet.getString("preprocessor");
			String classifier = resultSet.getString("classifier");
			String pipeline = resultSet.getString("pipeline");
			Integer score = resultSet.getInt("score");
			System.out.println("Dataset: " + dataset);
			System.out.println("Preprocessor: " + preprocessor);
			System.out.println("Classifier: " + classifier);
			System.out.println("Pipeline: " + pipeline);
			System.out.println("Score: " + score);
		}
	}
}
