package de.upb.crc901.mlplan.mlj;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import de.upb.crc901.automl.pipeline.service.MLPipelinePlan;
import de.upb.crc901.automl.pipeline.service.MLPipelinePlan.MLPipe;
import de.upb.crc901.mlplan.multiclass.MLPlanMySQLConnector;
import de.upb.crc901.mlplan.multiclass.core.MLUtil;
import de.upb.crc901.mlplan.multiclass.core.PlanExecutor;
import jaicore.basic.FileUtil;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.ml.experiments.Experiment;
import jaicore.planning.graphgenerators.task.TaskPlannerUtil;
import jaicore.planning.model.ceoc.CEOCAction;
import scala.annotation.cloneable;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;

public class ResultToDatabaseTransferer {

	static Map<String, Integer> totalClassifierCount = new HashMap<>();
	static Set<String> datasets = new HashSet<>();

	static Pattern patternForClassifier = Pattern.compile("weka\\.classifiers\\.[^.]*\\.([^:]*):__construct");
	static Pattern patternForSearcher = Pattern.compile("weka\\.attributeSelection\\.([^:]*):__construct");

	public static void main(String[] args) throws Exception {

		String[] timeouts = { "60", "3600", "86400" };
		String[] evals = { "5-70-MCCV", "10-CV" };
		String[] selectionTechs = { "selection", "noselection" };
		for (String timeout : timeouts) {

			/* evaluations of Auto-WEKA */
			final Pattern awPattern = Pattern.compile("best classifier: (.*)arguments: \\[(.*)\\]	attribute search: (.*)attribute search arguments: \\[(.*)\\]	attribute evaluation: (.*)	attribute evaluation arguments: \\[(.*)\\]	metric: errorRate");
			transferResultsToDatabase(new File("results/MLJ2018/" + timeout + "/Auto-WEKA"), Integer.parseInt(timeout), "Auto-WEKA", "default",
					new MLPlanMySQLConnector("isys-db.cs.upb.de", "mlplan", "UMJXI4WlNqbS968X", "mlplan_results_mlj2018"), line -> {
						String[] parts = line.split(",");

						/* recover the plan */
						StringBuilder sb = new StringBuilder();
						for (int i = 7; i < parts.length; i++) {
							if (sb.length() > 0)
								sb.append(",");
							sb.append(parts[i]);
						}
						String lastField = sb.toString().trim();
						

						Matcher m = awPattern.matcher(lastField);
						if (!m.find())
							System.err.println("Ignoring description " + lastField);
						String classifier = m.group(1).trim() + "[" + m.group(2).trim() + "]";
						String searcher = m.group(3).trim().equals("null") ? "" : (m.group(3).trim() + "[" + m.group(4).trim() + "]");
						String evaluator =  m.group(5).trim().equals("null") ? "" : (m.group(5).trim() + "[" + m.group(6).trim() + "]/");
						return new ExperimentResult(Integer.parseInt(parts[2].trim()), Double.parseDouble(parts[3].trim()), -1, classifier,
								searcher, evaluator, false);
					});

			/* evaluations of ML-Plan */
			// for (String evalTech : evals) {
			// for (String selectionTech : selectionTechs) {
			// File folder = new File("results/MLJ2018/" + timeout + "/" + evalTech + "-" + selectionTech);
			//
			//
			// transferResultsToDatabase(folder, Integer.parseInt(timeout), "ML-Plan" , evalTech + "-" + selectionTech, new MySQLMLPlanExperimentLogger("isys-db.cs.upb.de", "mlplan",
			// "UMJXI4WlNqbS968X", "mlplan_results_mlj2018"), line -> {
			// String[] parts = line.split(",");
			//
			// /* recover the plan */
			// StringBuilder sb = new StringBuilder();
			// for (int i = 7; i < parts.length; i++) {
			// if (sb.length() > 0)
			// sb.append(",");
			// sb.append(parts[i]);
			// }
			// String planAsString = sb.toString().trim();
			// List<String> commands = new ArrayList<>();
			// if (!planAsString.isEmpty()) {
			// commands = SetUtil.unserializeList(planAsString);
			// for (int i = 0; i < commands.size(); i++) {
			// String current = commands.get(i);
			// if (current.contains("(") && !current.contains(")")) {
			// commands.set(i, current + ", " + commands.get(i + 1));
			// commands.remove(i + 1);
			// i--;
			// }
			// }
			// }
			//
			// String searcher = "";
			// String evaluator = "";
			// String classifier = "";
			// if (!commands.isEmpty()) {
			//
			// /* recover pipeline from string */
			// TaskPlannerUtil util = new TaskPlannerUtil(null);
			// List<CEOCAction> plan = util.recoverPlanFromActionEncoding(MLUtil.getPlanningProblem(new File("testrsc/automl3.testset"), null).getDomain(), commands);
			// MLPipelinePlan.hostJASE = "localhost:8000";
			// MLPipelinePlan plPlan = new MLPipelinePlan();
			// PlanExecutor executor = new PlanExecutor(new Random());
			// try {
			// Map<ConstantParam, Object> variables = executor.executePlan(plan, new HashMap<>());
			// List<ASSearch> searcherObjects = variables.values().stream().filter(o -> o instanceof ASSearch).map(o -> (ASSearch) o).collect(Collectors.toList());
			// List<ASEvaluation> evaluatorObjects = variables.values().stream().filter(o -> o instanceof ASEvaluation).map(o -> (ASEvaluation) o).collect(Collectors.toList());
			// Optional<Classifier> classifierObject = variables.values().stream().filter(o -> o instanceof Classifier).map(o -> (Classifier) o).findAny();
			// if (!searcherObjects.isEmpty()) {
			// plPlan.addWekaAttributeSelection(searcherObjects.get(searcherObjects.size() - 1), evaluatorObjects.get(evaluatorObjects.size() - 1));
			// MLPipe attrSelection = plPlan.getAttrSelections().get(0);
			// String preprocessor = attrSelection.getStringEncoding();
			// String[] ppParts = preprocessor.split("/");
			// searcher = ppParts[0];
			// evaluator = ppParts[1];
			// }
			// plPlan.setClassifier(classifierObject.get());
			// classifier = plPlan.getClassifierPipe().getStringEncoding();
			// } catch (Throwable e) {
			// e.printStackTrace();
			// }
			// }
			// return new ExperimentResult(Integer.parseInt(parts[2].trim()), Double.parseDouble(parts[3].trim()), Double.parseDouble(parts[4].trim()) / 10000, classifier, searcher, evaluator, false);
			// });
			// }
			// }
		}
	}

	interface ILineInterpreter {
		public ExperimentResult interpret(String line);
	}

	static class ExperimentResult {
		int seed;
		double loss, believedLoss;
		String classifier, searcher, evaluator;
		boolean phase2Reached;

		public ExperimentResult(int seed, double loss, double believedLoss, String classifier, String searcher, String evaluator, boolean phase2Reached) {
			super();
			this.seed = seed;
			this.loss = loss;
			this.believedLoss = believedLoss;
			this.classifier = classifier;
			this.searcher = searcher;
			this.evaluator = evaluator;
			this.phase2Reached = phase2Reached;
		}
	}

	public static void prepareCompleteResults(File folderOfPreliminaryResults, ILineInterpreter interpreter) throws Exception {
		System.out.print("Extracting solution log summaries from logfiles ...");
		writeAllExperimentStats(folderOfPreliminaryResults);
		System.out.println(" done");
		System.out.print("Creating summaries of results ...");
		createShortVersionOfClusterResults(folderOfPreliminaryResults, interpreter);
		System.out.println(" done");
		System.out.println("Classifiers: ");
		totalClassifierCount.keySet().stream().sorted().forEach(k -> System.out.println(k + ": " + totalClassifierCount.get(k)));
		System.out.println("Datasets: ");
		datasets.stream().sorted().forEach(k -> System.out.println("\t" + k));
	}

	public static void writeAllExperimentStats(File folderOfPreliminaryResultsName) throws Exception {
		File logFolder = new File(folderOfPreliminaryResultsName + File.separator + "solutionlogs" + File.separator + "raw");
		File resultFolder = new File(folderOfPreliminaryResultsName + File.separator + "solutionlogs" + File.separator + "prepared");
		if (!logFolder.exists()) {
			System.err.println("Folder " + logFolder + " not found.");
			return;
		}
		try (Stream<Path> paths = Files.walk(logFolder.toPath())) {
			paths.filter(f -> f.toFile().isFile()).forEach(f -> {
				String name = f.toFile().getName();
				String restOfName = name;
				String[] nameParts = new String[5];
				for (int i = 0; i < 3; i++) {
					int nextIndex = restOfName.indexOf("-");
					nameParts[i] = restOfName.substring(0, nextIndex).trim();
					restOfName = restOfName.substring(nextIndex + 1);
				}
				int indexOfLastMinus = restOfName.lastIndexOf("-");
				if (indexOfLastMinus < 0 || restOfName.lastIndexOf(".") < 0 || restOfName.lastIndexOf(".") < indexOfLastMinus) {
					System.err.println("Ignoring " + restOfName + " since it has no last minus or no dot.");
					return;
				}
				nameParts[4] = restOfName.substring(indexOfLastMinus + 1, restOfName.lastIndexOf("."));
				nameParts[3] = restOfName.substring(0, indexOfLastMinus);
				datasets.add(nameParts[3]);
				try {
					writeExperimentStats(logFolder, Long.valueOf(nameParts[2]), Integer.valueOf(nameParts[1]), nameParts[3], Integer.valueOf(nameParts[4]), resultFolder);
				} catch (Exception e) {
					System.err.println("Problem with solution log " + name);
					// e.printStackTrace();
				}
			});
		}
	}

	public static void writeExperimentStats(File logFolder, long experimentId, int jobRun, String dataset, int seed, File resultFolder) throws Exception {
		if (!resultFolder.exists())
			resultFolder.mkdirs();
		File logFile = new File(logFolder + File.separator + "solutions-" + jobRun + "-" + experimentId + "-" + dataset + "-" + seed + ".log");
		File outFile = new File(resultFolder + File.separator + "solutions-" + jobRun + "-" + experimentId + "-" + dataset + "-" + seed + ".csv");

		try (FileWriter fw = new FileWriter(outFile)) {
			for (String line : FileUtil.readFileAsList(logFile.getAbsolutePath())) {
				String[] lineParts = new String[5];
				String restOfLine = line;
				for (int i = 0; i < 4; i++) {
					lineParts[i] = restOfLine.substring(0, restOfLine.indexOf(",")).trim();
					restOfLine = restOfLine.substring(restOfLine.indexOf(",") + 1);
				}
				lineParts[4] = restOfLine;

				// System.out.println(lineParts[4]);
				String classifier = getClassifierSelectedInSolution(lineParts[4]);
				if (classifier != null) {
					if (!totalClassifierCount.containsKey(classifier))
						totalClassifierCount.put(classifier, 0);
					totalClassifierCount.put(classifier, totalClassifierCount.get(classifier) + 1);
				}
				fw.write(lineParts[0] + ", " + lineParts[1] + ", " + lineParts[2] + ", " + lineParts[3] + ", " + classifier + ", " + getSearcherSelectedInSolution(lineParts[4])
						+ ", " + getEvaluatorSelectedInSolution(lineParts[4]) + ", " + (isTunedSolution(lineParts[4]) ? 1 : 0) + "\n");
			}
		}
		if (FileUtil.readFileAsList(outFile.getAbsolutePath()).size() != FileUtil.readFileAsList(logFile.getAbsolutePath()).size()) {
			System.err.println("Invalid number of transferred lines!");
		}
	}

	public static String getClassifierSelectedInSolution(String solution) {
		Matcher m = patternForClassifier.matcher(solution);
		if (!m.find()) {
			System.err.println("No classifier found");
			return null;
		}
		return m.group(1);
	}

	public static String getSearcherSelectedInSolution(String solution) {
		Matcher m = patternForSearcher.matcher(solution);
		List<String> matches = new ArrayList<>();
		String searcher = "None";
		while (m.find()) {
			matches.add(m.group(1));
		}
		if (matches.size() >= 3) {
			searcher = matches.get(1);
		}
		return searcher;
	}

	public static String getEvaluatorSelectedInSolution(String solution) {
		Matcher m = patternForSearcher.matcher(solution);
		List<String> matches = new ArrayList<>();
		String evaluator = "None";
		while (m.find()) {
			matches.add(m.group(1));
		}
		if (matches.size() >= 3) {
			evaluator = matches.get(2);
			if (evaluator.contains(" "))
				evaluator = evaluator.substring(evaluator.lastIndexOf(".") + 1);
		}
		return evaluator;
	}

	public static boolean isTunedSolution(String solution) {
		Matcher m = patternForSearcher.matcher(solution);
		List<String> matches = new ArrayList<>();
		while (m.find()) {
			matches.add(m.group(1));
		}
		return matches.size() > 3;
	}

	public static Map<String, Collection<ExperimentResult>> readExperimentResultsOfFolder(File folderOfResults, ILineInterpreter interpreter) throws IOException {

		Map<String, Collection<ExperimentResult>> results = new HashMap<>();
		try (Stream<Path> paths = Files.walk(folderOfResults.toPath())) {
			paths.filter(f -> f.getParent().toFile().equals(folderOfResults) && f.toFile().getAbsolutePath().endsWith(".csv")).forEach(f -> {

				String filename = f.toFile().getName();
				Collection<ExperimentResult> resultsForFile = new ArrayList<>();
				String dataset = filename.substring(filename.indexOf("-") + 1, filename.lastIndexOf("."));
				List<Double> vals = new ArrayList<>();
				Set<Integer> seenSeeds = new HashSet<>();
				List<String> lines;
				try {
					lines = FileUtil.readFileAsList(f.toFile().getAbsolutePath());

					for (String line : lines) {
						try {
							ExperimentResult r = interpreter.interpret(line);
							if (r != null) {
								resultsForFile.add(r);
							}
						} catch (Exception e) {
							System.err.println("Problems with " + f + ", line: " + line);
						}
					}
					results.put(filename, resultsForFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
		}
		;
		return results;
	}

	public static void showAllResultsOfFolders(Collection<File> foldersOfPreliminaryResults, ILineInterpreter interpreter) throws Exception {
		for (File folder : foldersOfPreliminaryResults) {
			System.out.println(folder);
			Map<String, Collection<ExperimentResult>> results = readExperimentResultsOfFolder(folder, interpreter);
			for (String key : results.keySet()) {
				System.out.println("\t" + key);
				for (ExperimentResult result : results.get(key)) {
					System.out.println("\t\t" + result.seed + ": " + result.loss + " for " + result.searcher + "/" + result.evaluator + " -> " + result.classifier);
				}
			}
		}
	}

	public static void transferResultsToDatabase(File folderOfPreliminaryResults, int timeout, String algorithmname, String algorithmVariant, MLPlanMySQLConnector logger,
			ILineInterpreter interpreter) throws Exception {
		Map<String, Collection<ExperimentResult>> results = readExperimentResultsOfFolder(folderOfPreliminaryResults, interpreter);
		for (String filename : results.keySet()) {
			String dataset = filename.substring(filename.indexOf("-") + 1, filename.lastIndexOf("."));
			System.out.println(dataset);
			for (ExperimentResult result : results.get(filename)) {
				Experiment e = new Experiment(dataset, algorithmname, algorithmVariant, result.seed, timeout, 8, 32 * 1024, "errorRate");
				int runId = logger.createRunIfDoesNotExist(e);
				if (runId < 0) {
					System.err.println("\tSkipping duplicate entry for: " + result.seed + ": " + result.loss + " for " + result.searcher + "/" + result.evaluator + " -> "
							+ result.classifier);
				} else {
					logger.addResultEntry(runId, result.searcher.isEmpty() ? "" : result.searcher + "/" + result.evaluator, result.classifier, result.loss, result.believedLoss);
					System.out
							.println("\tAdded result entry: " + result.seed + ": " + result.loss + " for " + result.searcher + "/" + result.evaluator + " -> " + result.classifier);
				}
			}
		}
	}

	public static void createShortVersionOfClusterResults(File folderOfPreliminaryResults, ILineInterpreter interpreter) throws Exception {

		File resultFolder = new File(folderOfPreliminaryResults + File.separator + "short");
		File solutionLogFolder = new File(folderOfPreliminaryResults + File.separator + "solutionlogs" + File.separator + "prepared");
		resultFolder.mkdirs();
		try (Stream<Path> paths = Files.walk(folderOfPreliminaryResults.toPath())) {
			paths.filter(f -> f.getParent().toFile().equals(folderOfPreliminaryResults) && f.toFile().getAbsolutePath().endsWith(".csv")).forEach(f -> {
				try {
					String filename = f.toFile().getName();
					String dataset = filename.substring(filename.indexOf("-") + 1, filename.lastIndexOf("."));
					List<Double> vals = new ArrayList<>();
					Set<Integer> seenSeeds = new HashSet<>();
					try (FileWriter fw = new FileWriter(new File(resultFolder + File.separator + f.toFile().getName()))) {
						for (String line : FileUtil.readFileAsList(f.toFile().getAbsolutePath())) {
							try {
								ExperimentResult r = interpreter.interpret(line);
								if (r != null) {
									fw.write(r.seed + ", " + r.loss + ", " + r.believedLoss + ", " + r.classifier + ", " + r.searcher + ", " + r.evaluator + ", "
											+ (r.phase2Reached ? 1 : 0) + "\n");
								}
							} catch (Exception e) {
								System.err.println("Problems with " + f + ", line: " + line);
								e.printStackTrace();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}

	public static void writeMeanErrorRates(File folderOfPreliminaryResults) {
		File resultFolder = new File(folderOfPreliminaryResults + File.separator + "summaries");
		resultFolder.mkdirs();
		try (FileWriter fw = new FileWriter(new File(resultFolder + File.separator + "summaries"))) {
			try (Stream<Path> paths = Files.walk(folderOfPreliminaryResults.toPath())) {
				paths.filter(f -> f.getParent().toFile().equals(folderOfPreliminaryResults) && f.toFile().getAbsolutePath().endsWith(".csv")).forEach(f -> {
					try {
						String filename = f.toFile().getName();
						String dataset = filename.substring(filename.indexOf("-") + 1, filename.lastIndexOf("."));
						DescriptiveStatistics errorRates = new DescriptiveStatistics();
						Set<Integer> seenSeeds = new HashSet<>();
						for (String line : FileUtil.readFileAsList(f.toFile().getAbsolutePath())) {
							try {
								if (line.trim().isEmpty())
									continue;
								String[] lineParts = new String[2];
								String restOfLine = line;
								for (int i = 0; i < 1; i++) {
									lineParts[i] = restOfLine.substring(0, restOfLine.indexOf(";")).trim();
									restOfLine = restOfLine.substring(restOfLine.indexOf(";") + 1);
								}
								lineParts[1] = restOfLine;
								int seed = Integer.valueOf(lineParts[0].trim());
								if (seenSeeds.contains(seed))
									continue;
								seenSeeds.add(seed);
								double errorRate = Double.valueOf(lineParts[1].trim());
								errorRates.addValue(errorRate);
							} catch (Exception e) {
								System.err.println("Problems with " + f + ", line: " + line);
								e.printStackTrace();
							}
						}
						fw.write(dataset + ", " + errorRates.getMean() + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
			;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ILineInterpreter getDefaultLineInterpreter() {

		return new ILineInterpreter() {

			File solutionLogFolder;
			String dataset;
			Set<Integer> seenSeeds;

			@Override
			public ExperimentResult interpret(String line) {
				if (line.trim().isEmpty())
					return null;
				String[] lineParts = new String[8];
				String restOfLine = line;
				for (int i = 0; i < 7; i++) {
					lineParts[i] = restOfLine.substring(0, restOfLine.indexOf(",")).trim();
					restOfLine = restOfLine.substring(restOfLine.indexOf(",") + 1);
				}
				lineParts[7] = restOfLine;
				long experimentId = Long.valueOf(lineParts[0].trim());
				int clusterJobId = Integer.valueOf(lineParts[1].trim());
				int seed = Integer.valueOf(lineParts[2].trim());
				if (seenSeeds.contains(seed))
					return null;
				seenSeeds.add(seed);
				double loss = Double.valueOf(lineParts[3].trim());
				String plan = lineParts[7];

				/* determine whether phase 2 was reached (only applies for our technique) */
				boolean phase2Reached = false;
				boolean isAUTOWekaJob = !line.contains("__construct");
				double believedLoss = isAUTOWekaJob ? 0 : Double.valueOf(lineParts[4].trim()) / 100f;
				if (!isAUTOWekaJob) {
					File logFile = new File(solutionLogFolder + File.separator + "solutions-" + clusterJobId + "-" + experimentId + "-" + dataset + "-" + seed + ".csv");
					if (logFile.exists()) {
						try {
							phase2Reached = FileUtil.readFileAsList(logFile.toString()).stream().map(l -> Integer.valueOf(l.split(",")[7].trim())).filter(val -> val == 1).findAny()
									.isPresent();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else
						System.err.println("No log file present for " + logFile + ". Cannot determine further information.");
				}

				// String classifier = getClassifierSelectedInSolution(plan);
				String classifier = "No Classifier Info";
				String searcher = "No Seacher Info";
				String evaluator = "No Evaluator Info";
				if (!isAUTOWekaJob) {
					classifier = getClassifierSelectedInSolution(plan);
					searcher = getSearcherSelectedInSolution(plan);
					evaluator = getEvaluatorSelectedInSolution(plan);
				}
				return new ExperimentResult(seed, loss, believedLoss, classifier, searcher, evaluator, phase2Reached);
			}

		};
	}
}
