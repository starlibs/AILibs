package jaicore.ml.evaluation.multilabel.databaseconnection;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import jaicore.basic.SQLAdapter;
import jaicore.ml.evaluation.multilabel.databaseconnection.ClassifierDBConnection;
import jaicore.ml.evaluation.multilabel.databaseconnection.EvaluationMode;
import weka.classifiers.lazy.KStar;

/**
 * Some test for the db connector.
 * 
 * @author Helena Graf
 *
 */
public class DataBaseConnectionTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testAddPreTrainedSingleLabelClassifier() throws SQLException, IOException {
		SQLAdapter mockAdapter = mock(SQLAdapter.class);
		
		Mockito.doAnswer(invocation -> {
			String table = invocation.getArgument(0);
			Map<String, ? extends Object> map = invocation.getArgument(1);
			
			System.out.println(table);
			System.out.println(map);
			return null;
		}).when(mockAdapter).insert(any(String.class), any(Map.class));
		
		Mockito.doAnswer(invocation -> {
			String query = invocation.getArgument(0);
			List<String> values = invocation.getArgument(1);

			for (String value : values) {
				query = query.replaceFirst("\\?", value);
			}

			System.out.println("Query: add pre trained single label classifier to db:");
			System.out.println(query);
			
			return new MockResultSet();
		}).when(mockAdapter).getResultsOfQuery(any(String.class), any(List.class));

		ClassifierDBConnection connection = new ClassifierDBConnection(mockAdapter, 0);
		connection.addPreTrainedSingleLabelClassifier(new KStar(), 2);
	}
	
	@Test
	public void testAddpreTrainedMultilabelClassifier() {
		// connect.addPreTrainedMultilabelClassifier(classifierName, classifier)
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetMissingClassifierMetrics() throws SQLException {
		SQLAdapter mockAdapter = mock(SQLAdapter.class);
		Mockito.doAnswer(invocation -> {
			String query = invocation.getArgument(0);
			List<String> values = invocation.getArgument(1);

			for (String value : values) {
				query = query.replaceFirst("\\?", value);
			}

			System.out.println("Query: get missing classifier metrics:");
			System.out.println(query);

			return new MockResultSet(Arrays.asList(Arrays.asList(3)), Arrays.asList("metric_id"));
		}).when(mockAdapter).getResultsOfQuery(any(String.class), any(List.class));

		ClassifierDBConnection connection = new ClassifierDBConnection(mockAdapter, 0);
		HashMap<Integer, String> evaluatedMetrics = new HashMap<>();
		evaluatedMetrics.put(3, "metric_name_1");
		evaluatedMetrics.put(4, "metric_name_2");
		
		HashMap<Integer, String> remainingMetrics = new HashMap<>();
		remainingMetrics.put(4,  "metric_name_2");
		assertEquals(remainingMetrics, connection.getMissingClassifierMetrics(3, evaluatedMetrics));
	}
	

	@Test
	public void testMode() {
		ClassifierDBConnection connection = new ClassifierDBConnection(null, 0);

		connection.setMode(EvaluationMode.Validation);
		assertEquals(EvaluationMode.Validation, connection.getMode());

		connection.setMode(EvaluationMode.Test);
		assertEquals(EvaluationMode.Test, connection.getMode());
	}

	@Test
	public void testIsDoubleInvalid() {
		ClassifierDBConnection connection = new ClassifierDBConnection(null, 0);

		assertEquals(false, connection.isDoubleValueInvalid(9.0));
		assertEquals(false, connection.isDoubleValueInvalid(0.0));
		assertEquals(false, connection.isDoubleValueInvalid(-1.0));

		assertEquals(true, connection.isDoubleValueInvalid(Double.NaN));
		assertEquals(true, connection.isDoubleValueInvalid(Double.NEGATIVE_INFINITY));
		assertEquals(true, connection.isDoubleValueInvalid(Double.POSITIVE_INFINITY));
	}

	@Test
	public void testAddModeToValuesList() {
		ClassifierDBConnection connection = new ClassifierDBConnection(null, 0);

		connection.setMode(EvaluationMode.Test);
		ArrayList<String> list = new ArrayList<>();
		list.add("Hello");
		connection.addModeToValuesList(list);
		assertEquals(Arrays.asList("Hello", "test"), list);

		connection.setMode(EvaluationMode.Validation);
		connection.addModeToValuesList(list);
		assertEquals(Arrays.asList("Hello", "test", "val"), list);
	}
}
