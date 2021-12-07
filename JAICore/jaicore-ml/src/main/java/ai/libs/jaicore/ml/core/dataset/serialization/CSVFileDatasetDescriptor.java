package ai.libs.jaicore.ml.core.dataset.serialization;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.descriptor.IDatasetDescriptor;

import com.google.gson.Gson;

class CSVFileDatasetDescriptor implements IDatasetDescriptor {

	private final String csvFile;
	private final String labelColumn;
	private final List<String> categoricalColumns;
	private final List<String> ignoredColumns;
	
	public CSVFileDatasetDescriptor(String csvFile, String labelColumn, 
			List<String> categoricalColumns, List<String> ignoredColumns) {
		this.csvFile = csvFile;
		this.labelColumn = labelColumn;
		this.categoricalColumns = categoricalColumns;
		this.ignoredColumns = ignoredColumns;
	}
	
	public CSVFileDatasetDescriptor(String json) {
		CSVFileDatasetDescriptor desc = new Gson().fromJson(json, getClass());
		this.csvFile = desc.csvFile;
		this.labelColumn = desc.labelColumn;
		this.categoricalColumns = desc.categoricalColumns;
		this.ignoredColumns = desc.ignoredColumns;
	}
	
	public String getCsvFile() {
		return this.csvFile;
	}
	
	public String getLabelColumn() {
		return this.labelColumn;
	}
	
	public List<String> getCategoricalColumns() {
		return this.categoricalColumns;
	}
	
	public List<String> getIgnoredColumns() {
		return this.ignoredColumns;
	}
	
	@Override
	public String getDatasetDescription() {
		return new Gson().toJson(this);
	}
}