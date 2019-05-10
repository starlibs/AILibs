package jaicore.experiments.databasehandle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jaicore.basic.FileUtil;
import jaicore.experiments.Experiment;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.IExperimentDatabaseHandle;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import jaicore.experiments.exceptions.ExperimentUpdateFailedException;

public class ExperimenterFileDBHandle implements IExperimentDatabaseHandle {

	private final File file;
	private final Set<ExperimentDBEntry> knownExperiments = new HashSet<>();
	private final List<String> keyFields = new ArrayList<>();

	public ExperimenterFileDBHandle(final File file) {
		super();
		this.file = file;
	}

	@Override
	public void setup(final IExperimentSetConfig config) throws ExperimentDBInteractionFailedException {
		keyFields.clear();
		keyFields.addAll(config.getKeyFields());
	}

	//	public List<String> getKeyFieldsDefinedInFile() throws ExperimentDBInteractionFailedException {
	//		try {
	//			String prefixRegExp = "^[^#]*#[ ]*fields:(.*)";
	//			Pattern p = Pattern.compile(prefixRegExp);
	//			for (String line : FileUtil.readFileAsList(file)) {
	//
	//			}
	//						Matcher m = p.matcher(firstLine);
	//
	//						return Arrays.asList(firstLine.split(",")).stream().map(s -> s.tri)
	//		} catch (IOException e) {
	//			throw new ExperimentDBInteractionFailedException(e);
	//		}
	//	}

	@Override
	public Collection<ExperimentDBEntry> getConductedExperiments() throws ExperimentDBInteractionFailedException {
		List<ExperimentDBEntry> experiments = new ArrayList<>();
		if (!file.exists() || !file.isFile()) {
			return experiments;
		}
		try {
			for (String line : FileUtil.readFileAsList(file)) {
				List<String> values = Arrays.asList(line.split(",")).stream().map(String::trim).collect(Collectors.toList());
				int id = Integer.parseInt(values.remove(0));
				int memory = Integer.parseInt(values.remove(0));
				int cpus = Integer.parseInt(values.remove(0));
				Map<String, String> keyValues = new HashMap<>();
				for (int i = 0; i < keyFields.size(); i++) {
					keyValues.put(keyFields.get(i), values.isEmpty() ? null : values.get(i));
				}
				Experiment experiment = new Experiment(memory, cpus, keyValues);
				experiments.add(new ExperimentDBEntry(id, experiment));
			}
		} catch (IOException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
		knownExperiments.addAll(experiments);
		return experiments;
	}

	@Override
	public ExperimentDBEntry createAndGetExperiment(final Experiment experiment) throws ExperimentDBInteractionFailedException {

		/* check if the experiment exists */
		Optional<ExperimentDBEntry> knownEntry = getConductedExperiments().stream().filter(e -> e.getExperiment().equals(experiment)).findFirst();
		if (knownEntry.isPresent()) {
			throw new IllegalArgumentException("Does already exist.");
		}

		try (FileWriter fw = new FileWriter(file, true)) {
			int id = (keyFields.size() + 1);
			fw.write(id + ", " + experiment.getMemoryInMB() + ", " + experiment.getNumCPUs() + ", " + System.currentTimeMillis() + "\n");
			ExperimentDBEntry entry = new ExperimentDBEntry(id, experiment);
			knownExperiments.add(entry);
			return entry;
		} catch (IOException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public void updateExperiment(final ExperimentDBEntry exp, final Map<String, ? extends Object> values) throws ExperimentUpdateFailedException {

	}

	@Override
	public void finishExperiment(final ExperimentDBEntry exp) throws ExperimentDBInteractionFailedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void finishExperiment(final ExperimentDBEntry exp, final Throwable errror) throws ExperimentDBInteractionFailedException {
		// TODO Auto-generated method stub

	}

}
