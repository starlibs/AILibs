package defaultEval.core.experiment;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import jaicore.basic.SQLAdapter;


public class PerformanceOrderHelper {

	public static Properties settings = new Properties();
	
	
	public static void main(String[] args) throws SQLException, FileNotFoundException, IOException {
		settings.load(new FileReader("helper.properties"));
		for(String arg: args) {
			settings.put(arg.split("=")[0], arg.split("=")[1]);
		}
		
		for(String data : readData()) {
			new PerformanceOrderHelper().createRanking(data);
		}
		
	}
	
	
	
	class ResultEntry implements Comparable<ResultEntry>{
		double performace;
		String classifierName;
		String preprocessorName;
		
		public ResultEntry(double per, String clas, String pre) {
			performace = per;
			preprocessorName = pre;
			classifierName = clas;
		}
		
		@Override
		public int compareTo(ResultEntry o) {
			return Double.compare(performace, o.performace);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("[");
			if(checkVerboseLevel(3)) {
				sb.append(preprocessorName);
				sb.append(", ");
				sb.append(classifierName);
				sb.append(", ");
			}
			sb.append(performace);
			sb.append("]");
			return sb.toString();
		}
		
		public boolean isEqualPipeline(ResultEntry e) {
			return classifierName.equals(e.classifierName) && preprocessorName.equals(e.preprocessorName);
		}
		
	}
	
	
	public SQLAdapter adapter;
	
	
	public PerformanceOrderHelper() {
		adapter = new SQLAdapter(settings.getProperty("db.host"), settings.getProperty("db.username"), settings.getProperty("db.password"), settings.getProperty("db.database"));
				
	}
	
	
	
	
	/**
	 * Creates a ranking based on the given Dataset
	 * 
	 * @param dataset
	 * @throws SQLException 
	 */
	private void createRanking(String dataset) throws SQLException {
		ArrayList<ResultEntry> resultsDefault = new ArrayList<>();
		ArrayList<ResultEntry> resultsOptimized = new ArrayList<>();
		
		for(String classifierName : readClassifiers()) {
			for(String p : readPreprocessors()) {
				
				for(String optimizerName : new String[] {"default", "SMAC", "Hyperband"}) {
					StringBuilder queryStringSB = new StringBuilder();
					queryStringSB.append("SELECT * FROM ");
					queryStringSB.append(settings.getProperty("db.table"));
					queryStringSB.append(" WHERE ");					
					
					queryStringSB.append("dataset");
					queryStringSB.append(" =");
					queryStringSB.append("'");
					queryStringSB.append(dataset);
					queryStringSB.append("'");
					
					queryStringSB.append(" AND ");
					
					queryStringSB.append("classifier");
					queryStringSB.append(" =");
					queryStringSB.append("'");
					queryStringSB.append(classifierName);
					queryStringSB.append("'");
					
					queryStringSB.append(" AND ");
					
					queryStringSB.append("optimizer");
					queryStringSB.append("=");
					queryStringSB.append("'");
					queryStringSB.append(optimizerName);
					queryStringSB.append("'");
					
					queryStringSB.append(" AND ");
					
					queryStringSB.append("preprocessor");
					queryStringSB.append("=");
					queryStringSB.append("'");
					queryStringSB.append(p);
					queryStringSB.append("';");
					
					
					ResultSet rs = this.adapter.getPreparedStatement(queryStringSB.toString()).executeQuery();
					double performance = 0;
					int i = 0;
					while (rs.next()) {
						if(rs.getString("pctIncorrect") != null && !rs.getString("pctIncorrect").equals("")) {
							performance = rs.getDouble("pctIncorrect");
							i++;	
						}
					}
					
					if(i > 0) {
						performance /= i;
						switch (optimizerName) {
						case "default":
							resultsDefault.add(new ResultEntry(performance, classifierName, p));
							break;

						default:
							ResultEntry old = null;
							for (ResultEntry resultEntry : resultsOptimized) {
								if(resultEntry.classifierName.equals(classifierName) && resultEntry.preprocessorName.equals(p)) {
									old = resultEntry;
									break;
								}
							}
							
							if(old != null) {
								if(old.performace > performance) {
									resultsOptimized.remove(old);
									resultsOptimized.add(new ResultEntry(performance, classifierName, p));
								}
							}else {
								resultsOptimized.add(new ResultEntry(performance, classifierName, p));	
							}
							
							break;
						}
					}else {
						if(checkVerboseLevel(5)) {
							System.out.println("No Data found for: " + p + ", " +  classifierName + ", " + optimizerName);
						}
					}
				}
			}
		}
		
		// make sure in both lists are the same pipelines
		resultsDefault.removeIf((e) ->{
			for (ResultEntry resultEntry : resultsOptimized) {
				if(resultEntry.isEqualPipeline(e)) {
					return false;
				}
			}
			return true;
		});
		resultsOptimized.removeIf((e) ->{
			for (ResultEntry resultEntry : resultsDefault) {
				if(resultEntry.isEqualPipeline(e)) {
					return false;
				}
			}
			return true;
		});
		
		// sort
		Collections.sort(resultsDefault);
		Collections.sort(resultsOptimized);
		
		
		System.out.println("RESULTS FOR: '" + dataset + "'");
		System.out.println();
		
		System.out.println("DEFAULT RESULTS: " + resultsDefault.size());
		System.out.println(resultsDefault);
		System.out.println("OPTIMIZED RESULTS: " + resultsOptimized.size());		
		System.out.println(resultsOptimized);
		
		// compare the first k
		int k_max = Math.min(Integer.valueOf(settings.getProperty("k", "20")), resultsDefault.size());
		
		
		for(int k = 1; k <= k_max; k++) {
			int n = 0;
			
			for(int i = 0; i < k; i++) {
				for (int j = 0; j < k; j++) {
					if(resultsDefault.get(i).isEqualPipeline(resultsOptimized.get(j))) {
						n++;
					}
				}	
			}
			System.out.println( n + " pipeline(s) are/is in the top k=" + k + " of both lists.");
		}
		System.out.println();
		
		// where are the top l from optimize in default
		int l_max = Math.min(Integer.valueOf(settings.getProperty("l", "20")), resultsDefault.size());
		
		for(int l = 0; l < l_max; l++) {
			for(int i = 0; i < resultsDefault.size(); i++) {
				if(resultsDefault.get(i).isEqualPipeline(resultsOptimized.get(l))) {
					System.out.println("Optimized index " + l + " is on index " + i + " with default config");
				}
			}
		}
		System.out.println();
		
		
		// what would be the result if k would have been k'=m
		int m_max = Math.min(Integer.valueOf(settings.getProperty("m", "20")), resultsDefault.size());
		
		for(int m = 0; m < m_max; m++) {
			for (int j = 0; j < resultsOptimized.size(); j++) {
				if(resultsDefault.get(m).isEqualPipeline(resultsOptimized.get(j))) {
					System.out.println("Default index " + m + " is on index " + j + " with optimized config");
				}
			}	
		}
		
		System.out.println();
		
		
		
		System.out.println();
		System.out.println();
		
	}
	
	private static Collection<String> readClassifiers(){
		ArrayList<String> result = new ArrayList<>();
		for (String string : settings.getProperty("classifiers").split(",")) {
			result.add(string.trim());
		}
		return result;
	}
	
	private static Collection<String> readPreprocessors(){
		ArrayList<String> result = new ArrayList<>();
		for (String string : settings.getProperty("preprocessors").split(",")) {
			result.add(string.trim());
		}
		return result;
	}
	
	private static Collection<String> readData(){
		ArrayList<String> result = new ArrayList<>();
		for (String string : settings.getProperty("datasets").split(",")) {
			result.add(string.trim());
		}
		return result;
	}
	
	/**
	 * 1 is most important
	 * 
	 * @param level
	 * @return
	 */
	private static boolean checkVerboseLevel(int level) {
		int v = Integer.valueOf(settings.getProperty("verbose"));
		return level <= v;
	}
	
	
}
