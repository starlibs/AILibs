package defaultEval.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import hasco.model.BooleanParameterDomain;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;
import hasco.serialization.ComponentLoader;
import scala.annotation.elidable;

public class SMACOptimizer extends Optimizer{
	
	

	public SMACOptimizer(Component preProcessor, Component classifier, String dataSet) {
		super(preProcessor, classifier, dataSet);
	}

	@Override
	public ComponentInstance optimize() {
		// generate params file
		PrintStream pcsStream = null;
		try {
			pcsStream = new PrintStream(new FileOutputStream(new File(Main.getEnvironmentPath()+"/pcs/"+buildFileName()+".pcs")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (Parameter p : parameterList) {
			ParameterDomain pd = p.getDefaultDomain();

			// Numeric (integer or real/double)
			if(pd instanceof NumericParameterDomain) {
				NumericParameterDomain n_pd = (NumericParameterDomain) pd;
				pcsStream.println(p.getName() + " " + (n_pd.isInteger() ? "integer" : "real") + " [" + n_pd.getMin() + ", " +n_pd.getMax() + "] [" + p.getDefaultValue().toString() + "]" );
			}
			
			// Boolean (categorical)
			else if(pd instanceof BooleanParameterDomain) {
				BooleanParameterDomain b_pd = (BooleanParameterDomain) pd;
				pcsStream.println(p.getName() + " categorical {true, false} [" + p.getDefaultValue().toString() + "]" );
			}
			
			//categorical
			else if(pd instanceof CategoricalParameterDomain) {
				CategoricalParameterDomain c_pd = (CategoricalParameterDomain) pd;
				pcsStream.print(p.getName() + " categorical {");
				for (int i = 0; i < c_pd.getValues().length; i++) {
					pcsStream.print(c_pd.getValues()[i]);
					if(i != c_pd.getValues().length - 1) {
						pcsStream.print(",");
					}
				}
				pcsStream.println("} [" + p.getDefaultValue().toString() + "]");
			}
		}
		
		
		pcsStream.close();
		
		// generate py-wrapper file
		PrintStream pyWrapperStream = null;
		// TODO do not generate if allready there
		
		try {
			pyWrapperStream = new PrintStream(new FileOutputStream(new File(Main.getEnvironmentPath() + "/py_wrapper/" + buildFileName() + ".py")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		pyWrapperStream.println("#!/usr/bin/python");
		pyWrapperStream.println("import sys, math");
		pyWrapperStream.println("from subprocess import call");
		
		for (Parameter parameter : parameterList) {
			pyWrapperStream.println( parameter.getName() + " = " + getInitialValue(parameter.getDefaultDomain()));
		}
		
		pyWrapperStream.println("for i in range(len(sys.argv)-1): ");
		int i = 0;
		for (Parameter parameter : parameterList) {
			if(i == 0) {
				pyWrapperStream.println("\t if (sys.argv[i] == '-" + parameter.getName() + "'):");
				
			}else {
				pyWrapperStream.println("\t elif(sys.argv[i] == '-" + parameter.getName() + "'):");
			}
			
			pyWrapperStream.println("\t \t " + parameter.getName() + " = "+getConverter(parameter.getDefaultDomain())+"(sys.argv[i+1])");
			i++;
		}
		
		// run java programm
		pyWrapperStream.print("call(\"java -jar "+Main.getEnvironmentPath()+"/PipelineEvaluator.jar");
		
		pyWrapperStream.print(" " + dataSet);
		
		
		if(preProcessor != null) {
			pyWrapperStream.print(" " + preProcessor.getName());
			
			for (Parameter parameter : preProcessor.getParameters()) {
				pyWrapperStream.print(" \"+ \"{:.9f}\".format("+parameter.getName()+") + \""); // TODO only for floats 
			}	
		}else {
			pyWrapperStream.print(" null");
		}
		
		pyWrapperStream.print(" " + classifier.getName());
		
		for (Parameter parameter : classifier.getParameters()) {
			pyWrapperStream.print(" \"+ \"{:.9f}\".format("+parameter.getName()+") + \"");
		}
		
		
		pyWrapperStream.print(" " + Main.getEnvironmentPath()+"/results/" + buildFileName() + ".txt");
		
		pyWrapperStream.println("\")");
		
		
		// read result file
		
		pyWrapperStream.println("file = open(\""+Main.getEnvironmentPath()+"\\\\results\\\\"+buildFileName()+".txt\", \"r\")");
		pyWrapperStream.println("yValue = float(file.read())");
		
		
		pyWrapperStream.println("print \"Result for SMAC: SUCCESS, 0, 0, %f, 0\" % yValue");
		
		pyWrapperStream.close();
		
		
		// start SMAC
		try {
			Runtime rt = Runtime.getRuntime();
			
			// TODO build better
			System.out.println(Main.getEnvironmentPath() + "/optimizer/smac/smac.bat "+
									"--run-obj QUALITY " + 
									"--use-instances false "+
							"--numberOfRunsLimit 10 "+
							"--pcs-file "+Main.getEnvironmentPath()+"/pcs/"+buildFileName()+".pcs "+
							"--algo \"python "+Main.getEnvironmentPath()+"/py_wrapper/"+buildFileName()+".py\"\" ");
			
			Process proc = rt.exec(
							Main.getEnvironmentPath() + "/optimizer/smac/smac.bat "+
									"--run-obj QUALITY " + 
									"--use-instances false "+
							"--numberOfRunsLimit 10 "+
							"--pcs-file "+Main.getEnvironmentPath()+"/pcs/"+buildFileName()+".pcs "+
							"--algo \"python "+Main.getEnvironmentPath()+"/py_wrapper/"+buildFileName()+".py\"\" "
					); // TODO settings
			proc.waitFor();
			int exitValue = proc.exitValue();
			System.out.println("aksd: " + exitValue);
			
//			Scanner sc = new Scanner(proc.getInputStream());
//			while (sc.hasNextLine()) {
//				String string = (String) sc.nextLine();
//				System.out.println(string);
//			}
//			sc.close();
			
		} catch (IOException | InterruptedException e) {
			// TODO
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	
	public static void main(String[] args) {
		ComponentLoader cloader = new ComponentLoader();
		ComponentLoader ploader = new ComponentLoader();
		
		try {
			Util.loadClassifierComponents(cloader);
			Util.loadPreprocessorComponents(ploader);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Component classifier = null;
		for (Component c : cloader.getComponents()) {
			if(c.getName().equals("weka.classifiers.functions.Logistic")) {
				classifier = c;
			}
		}
		
		
		new SMACOptimizer(null, classifier, "breast-cancer").optimize();
		
		
	}
	
	
	
	private String buildFileName() {
		return (preProcessor != null) ? preProcessor.getName() : "null" + "_" + classifier.getName() + "_" + dataSet;
	}

	
	private String getConverter(ParameterDomain pd) {
		if(pd instanceof NumericParameterDomain) {
			NumericParameterDomain n_pd = (NumericParameterDomain) pd;
			return n_pd.isInteger() ? "int" : "float";
		}
		return "";
	}
	
	
	private String getInitialValue(ParameterDomain pd) {
		if(pd instanceof NumericParameterDomain) {
			return "0";
		}
		
		// Boolean (categorical)
		else if(pd instanceof BooleanParameterDomain) {
			return "'true'";
		}
		
		//categorical
		else if(pd instanceof CategoricalParameterDomain) {
			return "''";
		}
		
		return "0";
	}
	
	
	
	
}
