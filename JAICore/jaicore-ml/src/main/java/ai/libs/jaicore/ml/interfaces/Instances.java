package ai.libs.jaicore.ml.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Instances extends List<Instance> {
	public int getNumberOfRows();
	public int getNumberOfColumns();
	public String toJson() throws JsonProcessingException;
	public void addAllFromJson(String jsonString) throws IOException;
	public void addAllFromJson(File jsonFile) throws IOException;
}
