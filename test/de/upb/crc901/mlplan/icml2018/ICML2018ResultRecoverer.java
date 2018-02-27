package de.upb.crc901.mlplan.icml2018;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.upb.crc901.mlplan.core.MySQLMultiLabelExperimentLogger;

public class ICML2018ResultRecoverer {
	public static void main(String[] args) throws SQLException {
		int jobId = Integer.parseInt(args[0]);
		int timeout = Integer.parseInt(args[1]);
		int seed = Integer.parseInt(args[2]);
		String algo = args[3];
		String dataset = args[4];
		String results = args[5];

//		int jobId = 480;
//		int timeout = 43200;
//		int seed = 0;
//		String algo = "MultiLabelGraphBasedPipelineSearcher";
//		String dataset = "arts1";
//		String results = "4500/523/5516/4782/174";
		
		if (algo.equals("MultiLabelGraphBasedPipelineSearcher")) {
			algo = "MLPlan-Multilabel-";
		}
		else {
			algo = "BR-Auto-WEKA";
		}
		dataset = dataset.toLowerCase();
//		System.out.println(dataset);
		
		switch (dataset) {
		case "llog-f":
			dataset = "langlog";
			break;
		case "emotions":
			dataset = "musicout";
			break;
		case "genbase":
			dataset = "protein";
			break;
		case "medical":
			dataset = "medc";
			break;
		case "enron-f":
			dataset = "enron";
			break;
		default:
		}
//		dataset = dataset.substring(0, dataset.indexOf(":") - 1);
//		System.out.println(dataset);
		
		System.out.println("Recovering\n------------------------------");
		System.out.println(jobId);
		System.out.println(timeout);
		System.out.println(seed);
		System.out.println(algo);
		System.out.println(dataset);
		System.out.println(results);
		
		String[] resultSplit = results.split("/");
		int loss_f1 = Integer.parseInt(resultSplit[0]);
		int loss_hamming = Integer.parseInt(resultSplit[1]);
		int loss_exact = Integer.parseInt(resultSplit[2]);
		int loss_jaccard = Integer.parseInt(resultSplit[3]);
		int loss_rank = Integer.parseInt(resultSplit[4]);
		
		
		final MySQLMultiLabelExperimentLogger expLogger = new MySQLMultiLabelExperimentLogger("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "mlplan_multilabel_results");
		
		/* get run id */
		String query = "SELECT relevantruns.run_id, results.* FROM `relevantruns` LEFT JOIN `results` USING(`run_id`) WHERE timeout=" + timeout + " AND seed=" + seed + " AND algorithm=\"" + algo + "\" and LOWER(dataset) LIKE \"%" + dataset + "%\"";
		System.out.println(query);
		ResultSet rs = expLogger.executeQuery(query);
		rs.next();
		int run_id = rs.getInt(1);
		System.out.println(run_id);
		int result_id = rs.getInt(2);
		if (result_id == 0)
			expLogger.addResultEntry(run_id, null, loss_f1, loss_hamming, loss_exact, loss_jaccard, loss_rank);
		else
			System.out.println("Result already stored, ignoring it.");
		
		/* store results */
		expLogger.close();
	}
}
