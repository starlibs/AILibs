package ai.libs.jaicore.problems.scheduling.openshop;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ai.libs.jaicore.basic.FileUtil;

/**
 * @author Felix Mohr
 *
 */
public class OpenShopProblemReader {

	private OpenShopProblemReader() {
		/* avoids instantiation */
	}

	public static OpenShopProblem getFromJobFileWithoutSetupTimesAndWithOneMachinePerWorkcenter(final File jobFile, final OpenShopMetric metric) throws IOException {

		/* get number of work centers */
		List<String> jobFileLines = FileUtil.readFileAsList(jobFile);
		final int numJobs = Integer.parseInt(jobFileLines.remove(0));
		final int numWorkcenters = Integer.parseInt(jobFileLines.remove(0));

		/* setup work centers with no setup times (assuming that the number of operations = number of jobs */
		OpenShopProblemBuilder builder = new OpenShopProblemBuilder();
		int[][] zeroSetupTimesArray = new int[numJobs + 1][numJobs + 1];
		for (int i = 0; i <= numJobs; i++) {
			for (int j = 0; j <= numJobs; j++) {
				zeroSetupTimesArray[i][j] = 0;
			}
		}
		for (int i = 0; i < numWorkcenters; i++) {
			String wcId = "W" + (i + 1);
			String machineId = "M" + (i + 1);
			builder.withWorkcenter(wcId, zeroSetupTimesArray);
			builder.withMachineForWorkcenter(machineId, wcId, 0, 0);
		}

		/* add jobs */
		configureBuilderWithJobsFromJobFile(builder, jobFile);

		/* setup metric and return */
		builder.withMetric(metric);
		return builder.build();
	}

	/**
	 * Reads the problem from a job file, a setup times file, and a file describing the number of parallel machines in each work center.
	 *
	 * The underlying assumption here is that all jobs have the same number of operations, one for each work center. Under this assumption, naturally each operation i of some job is assigned to the i-th work center by convention. In
	 * addition, we assume that each operation has a specific status required for the machines.
	 *
	 * @param jobFile
	 * @param setupTimesFile
	 * @param parallelMachinesFile
	 * @return
	 * @throws IOException
	 */
	public static OpenShopProblem mergeFromFiles(final File jobFile, final File setupTimesFile, final File parallelMachinesFile, final OpenShopMetric metric) throws IOException {

		/* existence check for files */
		if (setupTimesFile == null || !setupTimesFile.exists()) {
			throw new IllegalArgumentException("Cannot read setup times file \"" + setupTimesFile + "\"");
		}
		if (parallelMachinesFile == null || !parallelMachinesFile.exists()) {
			throw new IllegalArgumentException("Cannot read parallel machines file \"" + parallelMachinesFile + "\"");
		}

		/* create builder */
		OpenShopProblemBuilder builder = new OpenShopProblemBuilder();

		/* read the setup times and create the work centers */
		List<String> setupTimesFileLines = FileUtil.readFileAsList(setupTimesFile);
		final int numOperations = Integer.parseInt(setupTimesFileLines.remove(0));
		final int numWorkcenters = Integer.parseInt(setupTimesFileLines.remove(0));
		setupTimesFileLines.remove(0); // remove line with initial setup cost
		int[][] setupTimesForThisWorkcenter = null;
		int[][][] setupTimesForWorkcenters = new int[numWorkcenters][numOperations + 1][numOperations + 1];
		int wcId = 0;
		int currentLineIndexWithinWorkcenter = 1; // skip first line, because this is a 0-entry for the initial setup
		for (String line : setupTimesFileLines) {
			if (line.isEmpty()) {
				if (setupTimesForThisWorkcenter != null) {
					setupTimesForWorkcenters[wcId++] = setupTimesForThisWorkcenter;
				}
				setupTimesForThisWorkcenter = new int[numOperations + 1][numOperations + 1];
				currentLineIndexWithinWorkcenter = 1;
			}
			else {
				String[] setupTimesForThisOperationAsString = line.replace("|", " ").trim().split(" ");
				int[] setupTimeForThisOperation = new int[setupTimesForThisOperationAsString.length + 1];
				for (int i = 1; i < setupTimeForThisOperation.length; i++) {
					setupTimeForThisOperation[i] = Integer.parseInt(setupTimesForThisOperationAsString[i - 1]); // first element is just always 0
				}
				setupTimesForThisWorkcenter[currentLineIndexWithinWorkcenter] = setupTimeForThisOperation;
				currentLineIndexWithinWorkcenter ++;
			}
		}
		setupTimesForWorkcenters[wcId] = setupTimesForThisWorkcenter;
		for (int i = 0; i < numWorkcenters; i++) {
			builder.withWorkcenter("W" + (i + 1), setupTimesForWorkcenters[i]);
		}

		/* read in the number of machines for the work centers */
		List<String> numMachineFileLines = FileUtil.readFileAsList(parallelMachinesFile);
		String[] numMachineTimeParts = numMachineFileLines.get(0).replace("|", " ").trim().split(" ");
		if (numMachineTimeParts.length != numWorkcenters) {
			throw new IllegalArgumentException("The number of fields in the machine file must coincide with the number of work centers defined in the other files. Here, " + numMachineTimeParts.length
					+ " numbers of machines have been defined: " + numMachineFileLines.get(0)+ ". The number of work centers however is " + numWorkcenters);
		}
		int machineIndex = 1;
		for (int i = 0; i < numWorkcenters; i++) {
			int numMachinesHere = Integer.parseInt(numMachineTimeParts[i]);
			String wcName = "W" + (i + 1);
			for (int j = 0; j < numMachinesHere; j++) {
				builder.withMachineForWorkcenter("M" + (machineIndex++), wcName, 0, 0);
			}
		}

		/* configure jobs */
		configureBuilderWithJobsFromJobFile(builder, jobFile);

		/* build the problem */
		return builder.withMetric(metric).build();
	}

	private static void configureBuilderWithJobsFromJobFile(final OpenShopProblemBuilder builder, final File jobFile) throws IOException {

		final int numWorkcentersDefinedInBuilder = builder.getWorkcenters().size();

		if (jobFile == null || !jobFile.exists()) {
			throw new IllegalArgumentException("Cannot read job file \"" + jobFile + "\"");
		}

		/* read job file
		 * 1st line is number of jobs
		 * 2nd line is number of work centers
		 * following lines define the matrix where [i][j] is the process time of the operation i, which is to be realized in work center j
		 **/
		Queue<String> jobFileLines = new LinkedList<>(FileUtil.readFileAsList(jobFile));
		final int numJobs = Integer.parseInt(jobFileLines.poll());
		final int numWorkcentersHere = Integer.parseInt(jobFileLines.poll());
		if (numWorkcentersDefinedInBuilder != numWorkcentersHere) {
			throw new IllegalArgumentException("Number of work centers in setup file is " + numWorkcentersDefinedInBuilder + " but in job description is " + numWorkcentersHere);
		}
		int opIndex = 1;
		for (int i = 0; i < numJobs; i++) {
			String jobId = "J" + (i + 1);
			String line = jobFileLines.poll();
			String[] processTimeParts = line.replace("|", " ").trim().split(" ");
			if (processTimeParts.length != (numWorkcentersDefinedInBuilder)) { // add one field for the first and last pipe respectively
				throw new IllegalArgumentException("Ill-defined job specification \"" + line + "\" for " + numWorkcentersDefinedInBuilder + " work centers. Split length is " + processTimeParts.length + ": " + Arrays.toString(processTimeParts));
			}
			builder.withJob(jobId, 0, Integer.MAX_VALUE, 1);
			for (int j = 0; j < numWorkcentersDefinedInBuilder; j++) {
				builder.withOperationForJob("O" + opIndex, jobId, Integer.parseInt(processTimeParts[j]), j + 1, "W" + (j + 1)); // the status is j + 1, because status 0 is the inital state
				opIndex++;
			}
		}
	}
}
