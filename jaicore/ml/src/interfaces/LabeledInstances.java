package jaicore.ml.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface LabeledInstances<L> extends List<LabeledInstance<L>> {
	
	public int getNumberOfRows();
	public int getNumberOfColumns();
	public String toJson();
	public void addAllFromJson(String jsonString) throws IOException;
	public void addAllFromJson(File jsonFile) throws IOException;
	public ArrayList<L> getOccurringLabels();
}
