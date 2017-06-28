package util.services;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ComparableAttributeServiceGenerator {
	
	public void create(int services, String outServices, String outRules) throws IOException {
		
		/* create services with predicates and rules */
		StringBuilder strRules = new StringBuilder();
		strRules.append("EQ(x,y) -> EQ(y,x)\n");
		strRules.append("NEQ(x,y) -> NEQ(y,x)\n");
		strRules.append("EQ(x,y) & EQ(y,z) -> EQ(x,z)\n"); // transitivity of equality
		strRules.append("LE(x,y) & LE(y,z) -> LE(x,z)\n"); // transitivity of <
		strRules.append("LEQ(x,y) & LEQ(y,z) -> LEQ(x,z)\n"); // transitivity of <=
		
		StringBuilder strServices = new StringBuilder();
		for (int i = 0; i < services; i++) {
			String inputs = "", outputs = "", precondition = "", effect = "";
			int numberOfInputs = (Math.random() > 0.5) ? 1 : 1;
			int numberOfOutputs = 1;
			for (int j = 1; j <= numberOfInputs; j++) {
				if (inputs.length() != 0) {
					inputs += ", ";
					precondition += " & ";
				}
				inputs += "i" + j;
				//precondition += "t" + type + "(i" + j + ")";
			}
			for (int j = 1; j <= numberOfOutputs; j++) {
				if (outputs.length() != 0) {
					outputs += ", ";
					//effect += " & ";
				}
				outputs += "o" + j;
				//effect += "t" + type + "(o" + j + ")";
			}
			
			/* introduce three new predicates */
			String p1 = "A" + i; // postcondition
			String p2 = "C" + i; // comparison-predicate (CHEAPER,LARGER...)
			
			String[] comparators = {"NEQ", "EQ", "LEQ", "LE"};
			
			/* determine precondition of service */
			precondition = "T(i1)";
			
			/* determine preconditions, and effects of service, and the rules */
			if (numberOfInputs == 1) {
				effect = "T(o1) & " + p1 + "(i1,o1)";
				for (String b : comparators) {
					
					/* rule for filter 1 */
					strRules.append(p1 + "(x,y) & " + b + "(y,z) -> " + p2 + b + "(x,z)\n");
				}
				
				/* rule for selector */
				strRules.append(p1 + "(x1,y1) & " + p1 + "(x2,y2) & " + "LEQ(y1,y2) -> SM" + i + "(x1,x2)\n");
			}
			else {
				effect = p1 + "(i1,i2,o1)";
				
				for (String b : comparators) {
					
					/* rule for filter 1 */
					strRules.append(p1 + "(x,q,y) & " + b + "(y,z) -> " + p2 + b + "(x,q,z)\n");
					
					/* rule for selector */
					strRules.append(p1 + "(x1,q,y1) & " + p1 + "(x2,q,y2) & " + b + "(y1,y2) -> " + p2 + b + "(x1,x2,q)\n");
				}
			}
			strServices.append("serv" + (i + 1) + "; " + inputs + "; " + outputs + "; " + precondition + "; " + effect + "\n");
		}
		
		/* write services to file */
		FileWriter streamServiceFile = new FileWriter(outServices);
		BufferedWriter writerServiceFile = new BufferedWriter(streamServiceFile);
		writerServiceFile.write(strServices.toString());
		writerServiceFile.close();
		
		/* write rules to file */
		FileWriter streamRuleFile = new FileWriter(outRules);
		BufferedWriter writerRuleFile = new BufferedWriter(streamRuleFile);
		writerRuleFile.write(strRules.toString());
		writerRuleFile.close();
	}
}