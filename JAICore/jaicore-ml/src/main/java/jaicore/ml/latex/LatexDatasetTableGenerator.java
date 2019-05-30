package jaicore.ml.latex;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class LatexDatasetTableGenerator {
	private static final Logger logger = LoggerFactory.getLogger(LatexDatasetTableGenerator.class);
	private final List<DataSource> datasets = new ArrayList<>();
	private int numMajorColumns = 1;
	private String caption = "Dataset overview";
	private String label = "tab:datasets";

	public void addLocalFiles(final File... files) throws Exception {
		this.addLocalFiles(Arrays.asList(files));
	}

	public void addLocalFiles(final List<File> files) throws Exception {
		for (File file : files) {
			this.datasets.add(new DataSource(file.getCanonicalPath()));
		}
	}

	public void addOpenMLDatasets(final int... datasetIds) throws Exception {
		OpenmlConnector client = new OpenmlConnector();
		for (int id : datasetIds) {
			DataSetDescription description = client.dataGet(id);
			File file = description.getDataset("key");
			this.datasets.add(new DataSource(file.getCanonicalPath()));
		}
	}

	public List<DataSource> getDatasets() {
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
				DataSource source = this.datasets.get(k);
				String datasetName = source.toString();
				String numInstances = "?";
				String numAttributes = "?";
				String numClasses = "?";
				try {
					Instances inst = source.getDataSet();
					inst.setClassIndex(inst.numAttributes() - 1);
					datasetName = inst.relationName().replaceAll("(&|_)", "");
					numInstances = String.valueOf(inst.size());
					numAttributes = String.valueOf(inst.numAttributes() - 1);
					numClasses = String.valueOf(inst.numClasses());
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
