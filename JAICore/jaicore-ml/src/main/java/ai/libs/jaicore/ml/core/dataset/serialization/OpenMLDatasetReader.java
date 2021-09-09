package ai.libs.jaicore.ml.core.dataset.serialization;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.descriptor.IDatasetDescriptor;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.IDatasetDeserializer;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.control.ILoggingCustomizable;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.openml.apiconnector.xml.Task;
import org.openml.apiconnector.xml.Task.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.ml.core.dataset.Dataset;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;

public class OpenMLDatasetReader implements IDatasetDeserializer<ILabeledDataset<ILabeledInstance>>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(OpenMLDatasetReader.class);

	private ArffDatasetAdapter adapter = new ArffDatasetAdapter();

	private static final OpenmlConnector connector = new OpenmlConnector();

	public static ILabeledDataset<ILabeledInstance> get(final int openmlid) throws DatasetDeserializationFailedException {
		return new OpenMLDatasetReader().deserializeDataset(openmlid);
	}

	public ILabeledDataset<ILabeledInstance> deserializeDataset(final int openMLId) throws DatasetDeserializationFailedException {
		try {
			DataSetDescription dsd = connector.dataGet(openMLId);
			if (dsd.getDefault_target_attribute().contains(",")) {
				throw new IllegalArgumentException("The dataset with ID " + openMLId + " cannot be read as it is a multi-target dataset which is currently not supported.");
			}
			ILabeledDataset<ILabeledInstance> dataset = this.deserializeDataset(openMLId, dsd.getDefault_target_attribute());
			if (dsd.getIgnore_attribute() != null) {
				for (String columnNameToIgnore : dsd.getIgnore_attribute()) {
					if (dataset.getListOfAttributes().stream().anyMatch(a -> a.getName().equals(columnNameToIgnore))) {
						dataset.removeColumn(columnNameToIgnore);
					} else {
						this.logger.warn("Ignored attribute \"{}\" is not a column of the dataset. Ignoring to ignore it!", columnNameToIgnore);
					}
				}
			}
			if (dsd.getRow_id_attribute() != null && dataset.getInstanceSchema().getAttributeList().stream().anyMatch(x -> x.getName().equals(dsd.getRow_id_attribute()))) {
				dataset.removeColumn(dsd.getRow_id_attribute());
			}
			return dataset;
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException("Could not deserialize OpenML dataset with id " + openMLId, e);
		}
	}

	public ILabeledDataset<ILabeledInstance> deserializeDataset(final int openMLId, final String targetAttribute) throws DatasetDeserializationFailedException {
		try {
			DataSetDescription dsd = connector.dataGet(openMLId);
			if (dsd.getDefault_target_attribute().contains(",")) {
				throw new IllegalArgumentException("The dataset with ID " + openMLId + " cannot be read as it is a multi-target dataset which is currently not supported.");
			}

			File arffFile = connector.datasetGet(dsd);
			Dataset ds = (Dataset) (this.adapter.deserializeDataset(new FileDatasetDescriptor(arffFile), targetAttribute));
			ds.addInstruction(new ReconstructionInstruction(OpenMLDatasetReader.class.getMethod("get", int.class), openMLId));
			return ds;
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException("Could not deserialize OpenML dataset with id " + openMLId, e);
		}
	}

	public File getArffFileOfOpenMLID(final int id) throws Exception {
		DataSetDescription dsd = connector.dataGet(id);
		return connector.datasetGet(dsd);
	}

	@Override
	public ILabeledDataset<ILabeledInstance> deserializeDataset(final IDatasetDescriptor descriptor) throws DatasetDeserializationFailedException, InterruptedException {
		if (!(descriptor instanceof OpenMLDatasetDescriptor)) {
			throw new IllegalArgumentException("Only openml descriptors supported.");
		}
		OpenMLDatasetDescriptor cDescriptor = (OpenMLDatasetDescriptor) descriptor;
		return this.deserializeDataset(cDescriptor.getOpenMLId());
	}

	public List<ILabeledDataset<ILabeledInstance>> loadTaskFold(final int openmlTaskID, final int fold) throws Exception {
		OpenmlConnector con = new OpenmlConnector();
		Task task = con.taskGet(openmlTaskID);
		File file = con.taskSplitsGet(task);
		ILabeledDataset<? extends ILabeledInstance> splitDescription = this.adapter.readDataset(file);
		List<Integer> fitFold = new ArrayList<>();
		List<Integer> predictFold = new ArrayList<>();

		for (ILabeledInstance i : splitDescription) {
			if (((int) (double) i.getLabel()) == fold) {
				int instanceIndex = (int) (double) i.getAttributeValue(1);
				switch (((ICategoricalAttribute) splitDescription.getAttribute(0)).getLabels().get((int) i.getAttributeValue(0))) {
				case "TRAIN":
					fitFold.add(instanceIndex);
					break;
				case "TEST":
					predictFold.add(instanceIndex);
					break;
				default:
					/* ignore this case */
					break;
				}
			}
		}

		ILabeledDataset<ILabeledInstance> dataset = null;
		for (Input input : task.getInputs()) {
			if (input.getName().equals("source_data")) {
				dataset = this.deserializeDataset(input.getData_set().getData_set_id(), input.getData_set().getTarget_feature());
			}
		}
		return SplitterUtil.getRealizationOfSplitSpecification(dataset, Arrays.asList(fitFold, predictFold));
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.adapter.setLoggerName(this.getLoggerName() + ".arffadapter");
	}
}
