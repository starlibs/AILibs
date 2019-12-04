package ai.libs.jaicore.ml.core.dataset.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;

public class LatexDatasetTableGenerator {
	private static final Logger logger = LoggerFactory.getLogger(LatexDatasetTableGenerator.class);
	private final List<File> datasets = new ArrayList<>();
	private int numMajorColumns = 1;
	private String caption = "Dataset overview";
	private String label = "tab:datasets";

	@SuppressWarnings("serial")
	public class DataSourceCreationFailedException extends Exception {
		public DataSourceCreationFailedException(final Exception e) {
			super(e);
		}
	}

	public void addLocalFiles(final File... files) throws DataSourceCreationFailedException {
		this.addLocalFiles(Arrays.asList(files));
	}

	public void addLocalFiles(final List<File> files) throws DataSourceCreationFailedException {
		for (File file : files) {
			try {
				this.datasets.add(file);
			} catch (Exception e) {
				throw new DataSourceCreationFailedException(e);
			}
		}
	}

	public void addOpenMLDatasets(final int... datasetIds) throws Exception {
		OpenmlConnector client = new OpenmlConnector();
		for (int id : datasetIds) {
			DataSetDescription description = client.dataGet(id);
			File file = client.datasetGet(description);
			this.datasets.add(file);
		}
	}

	public List<File> getDatasets() {
		return this.datasets;
	}

	public int getNumMajorColumns() {
		return this.numMajorColumns;
	}

	public void setNumMajorColumns(final int numMajorColumns) {
		this.numMajorColumns = numMajorColumns;
	}

	public String getCaption() {
		return this.caption;
	}

	public void setCaption(final String caption) {
		this.caption = caption;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public String getLatexCode() {
		StringBuilder sb = new StringBuilder();

		/* create header */
		sb.append("\\begin{table}\r\n");
		sb.append("  \\begin{center}\r\n");
		sb.append("    \\begin{tabular}{lrrr");
		for (int i = 1; i < this.numMajorColumns; i++) {
			sb.append("l|llrrr");
		}
		sb.append("}\r\n      ");
		for (int i = 0; i < this.numMajorColumns; i++) {
			if (i > 0) {
				sb.append("& ~ & ~ &"); // have an empty field to get some spacing
			}
			sb.append("Dataset & \\#Inst.& \\#Attr. & \\#Cl.");
		}
		sb.append("\\\\\\hline\r\n");

		/* create row content */
		int rows = (int) Math.ceil(this.datasets.size() * 1f / this.numMajorColumns);
		int k = 0;
		for (int i = 0; i < rows && k < this.datasets.size(); i++) {
			sb.append("      ");
			for (int j = 0; j < this.numMajorColumns && k < this.datasets.size(); j++, k++) {
				File source = this.datasets.get(k);
				String datasetName = source.toString();
				String numInstances = "?";
				String numAttributes = "?";
				String numClasses = "?";
				try {
					ILabeledDataset<ILabeledInstance> inst = ArffDatasetAdapter.readDataset(source);
					datasetName = inst.getRelationName().replaceAll("(&|_)", "");
					numInstances = String.valueOf(inst.size());
					numAttributes = String.valueOf(inst.getNumAttributes() - 1);
					if (inst.getInstanceSchema().getLabelAttribute() instanceof ICategoricalAttribute) {
						numClasses = String.valueOf(((ICategoricalAttribute) inst.getInstanceSchema().getLabelAttribute()).getLabels().size());
					} else {
						numClasses = "inf";
					}
				} catch (Exception e) {
					logger.error("Could not read dataset from source {}", source);
				}
				if (j > 0) {
					sb.append("& & &");
				}
				sb.append(datasetName);
				sb.append(" & ");
				sb.append(numInstances);
				sb.append(" & ");
				sb.append(numAttributes);
				sb.append(" & ");
				sb.append(numClasses);
			}
			sb.append("\\\\\r\n");
		}
		sb.append("    \\end{tabular}\r\n");
		sb.append("  \\end{center}\r\n");
		sb.append("  \\caption{");
		sb.append(this.caption);
		sb.append("}\r\n  \\label{");
		sb.append(this.label);
		sb.append("}\r\n\\end{table}");
		return sb.toString();
	}
}
