package util.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class RuleReader {
	private List<Collection<String>> premises = new LinkedList<Collection<String>>();
	private List<Collection<String>> conclusions = new LinkedList<Collection<String>>();
	
	public RuleReader(String ruleFile) throws IOException {
		BufferedReader ruleReader = new BufferedReader(new FileReader(ruleFile));
		String strLine;
		int i = 0;
		while ((strLine = ruleReader.readLine()) != null) {
			if (strLine.trim().startsWith("#") || strLine.trim().equals(""))
				continue;
			String[] parts = strLine.split("->");
			if (parts.length < 2) {
				ruleReader.close();
				throw new IOException("The rule " + strLine + " has not a \"->\" symbol.");
			}
			Collection<String> premise = new HashSet<String>();
			Collection<String> conclusion = new HashSet<String>();
			for (String literalInPremise : parts[0].split("&"))
				premise.add(literalInPremise);
			for (String literalInConclusion : parts[1].split("&"))
				conclusion.add(literalInConclusion);
			premises.add(i, premise);
			conclusions.add(i, conclusion);
			i++;
		}
		ruleReader.close();
	}
	
	public int getRuleCount() { return this.premises.size(); }

	public List<Collection<String>> getPremises() {
		return premises;
	}

	public List<Collection<String>> getConclusions() {
		return conclusions;
	}
}
