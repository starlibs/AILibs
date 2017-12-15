package de.upb.crc901.taskconfigurator.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jaicore.basic.FileUtil;

public class ResultPreparer {

	static Map<String, Integer> totalClassifierCount = new HashMap<>();
	static Set<String> datasets = new HashSet<>();

	static Pattern patternForClassifier = Pattern.compile("weka\\.classifiers\\.[^.]*\\.([^:]*):__construct");
	static Pattern patternForSearcher = Pattern.compile("weka\\.attributeSelection\\.([^:]*):__construct");

	public static void main(String[] args) throws Exception {
		File folderOfPreliminaryResults = new File("results/JML/86400/10CV-selection");
		prepareCompleteResults(folderOfPreliminaryResults);
	}

	public static void prepareCompleteResults(File folderOfPreliminaryResults) throws Exception {
		System.out.print("Extracting solution log summaries from logfiles ...");
		writeAllExperimentStats(folderOfPreliminaryResults);
		System.out.println(" done");
		System.out.print("Creating summaries of results ...");
		createShortVersionOfClusterResults(folderOfPreliminaryResults);
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
					e.printStackTrace();
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

	public static void createShortVersionOfClusterResults(File folderOfPreliminaryResults) throws Exception {

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
								if (line.trim().isEmpty())
									continue;
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
									continue;
								seenSeeds.add(seed);
								double loss = Double.valueOf(lineParts[3].trim());
								String plan = lineParts[7];

								/* determine whether phase 2 was reached (only applies for our technique) */
								boolean phase2Reached = false;
								boolean isAUTOWekaJob = !line.contains("__construct");
								double believedLoss = isAUTOWekaJob ? 0 : Double.valueOf(lineParts[4].trim()) / 100f;
								if (!isAUTOWekaJob) {
									File logFile = new File(solutionLogFolder + File.separator + "solutions-"
											+ clusterJobId + "-" + experimentId + "-" + dataset + "-" + seed + ".csv");
									if (logFile.exists()) {
									phase2Reached = FileUtil.readFileAsList(logFile.toString()).stream().map(l -> Integer.valueOf(l.split(",")[7].trim())).filter(val -> val == 1)
											.findAny().isPresent();
									}
									else
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

								fw.write(seed + ", " + loss + ", " + believedLoss + ", " + classifier + ", " + searcher + ", " + evaluator + ", " + (phase2Reached ? 1 : 0) + "\n");
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
}
