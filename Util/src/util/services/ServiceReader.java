package util.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ServiceReader {
	
	private List<String> names = new LinkedList<String>();
	private List<Collection<String>> inputs = new LinkedList<Collection<String>>();
	private List<Collection<String>> outputs = new LinkedList<Collection<String>>();
	private List<Collection<String>> preconditions = new LinkedList<Collection<String>>();
	private List<Collection<String>> effects = new LinkedList<Collection<String>>();
	
	public ServiceReader(String serviceFile) throws IOException {
		BufferedReader serviceReader = new BufferedReader(new FileReader(serviceFile));
		String strLine;
		int i = 0;
		while ((strLine = serviceReader.readLine()) != null) {
			if (strLine.trim().startsWith("#") || strLine.trim().equals(""))
				continue;
			String[] parts = strLine.split(";");
			if (parts.length < 5) {
				serviceReader.close();
				throw new IOException("The following service is not described correctly: " + strLine);
			}
			
			Collection<String> inputsOfService = new HashSet<String>();
			Collection<String> outputsOfService = new HashSet<String>();
			Collection<String> preconditionOfService = new HashSet<String>();
			Collection<String> effectOfService = new HashSet<String>();
			for (String input : parts[1].split(","))
				inputsOfService.add(input.trim());
			for (String output : parts[2].split(","))
				outputsOfService.add(output.trim());
			for (String literalInPrecondition : parts[3].split("&"))
				preconditionOfService.add(literalInPrecondition.trim());
			for (String literalInEffect : parts[4].split("&"))
				effectOfService.add(literalInEffect.trim());
			names.add(i, parts[0].trim());
			inputs.add(i, inputsOfService);
			outputs.add(i, outputsOfService);
			preconditions.add(i, preconditionOfService);
			effects.add(i, effectOfService);
			i++;
		}
		serviceReader.close();
	}
	
	public int getServiceCount () { return this.inputs.size(); }
	
	public List<String> getNames() {
		return names;
	}

	public List<Collection<String>> getInputs() {
		return inputs;
	}

	public List<Collection<String>> getOutputs() {
		return outputs;
	}

	public List<Collection<String>> getPreconditions() {
		return preconditions;
	}

	public List<Collection<String>> getEffects() {
		return effects;
	}
}
