package defaultEval.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import hasco.model.BooleanParameterDomain;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;

public class SMACOptimizer extends Optimizer{
	
	

	public SMACOptimizer(Component preProcessor, Component classifier, String dataSet) {
		super(preProcessor, classifier, dataSet);
	}

	@Override
	public ComponentInstance optimize() {
		// generate params datei
		PrintStream pcsStream = null;
		try {
			pcsStream = new PrintStream(new FileOutputStream(new File("test.pcs")));
		
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
				pcsStream.println("} [" + p.getDefaultValue() + "]");
			}
		}
		
		
		pcsStream.close();
		
		// generate py-wrapper file
		PrintStream pyWrapperStream = null;
		// TODO do not generate if allready there
		
		try {
			pyWrapperStream = new PrintStream(new FileOutputStream(new File("run_" + preProcessor.getName() + classifier.getName() + ".py")));
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
		pyWrapperStream.print("call(\"java");
		
		pyWrapperStream.print(" " + dataSet);
		
		for (Parameter parameter : parameterList) {
			pyWrapperStream.print(" " + parameter.getName());
		}
		
		pyWrapperStream.print(" " + preProcessor.getName() + classifier.getName() + dataSet);
		
		pyWrapperStream.println("\")");
		
		
		
		// read result file
		
		pyWrapperStream.println("file = open(\"Results/"+getResultName()+".txt\", \"r\")");
		pyWrapperStream.println("yValue = float(file.read())");
		
		
		pyWrapperStream.println("print \"Result for SMAC: SUCCESS, 0, 0, %f, 0\" % yValue");
		
		pyWrapperStream.close();
		
		
		// start SMAC
		
		
		
		
		return null;
	}
	

	private String getResultName() {
		return preProcessor.getName() + classifier.getName() + dataSet;
	}
	
	
	private String getConverter(ParameterDomain pd) {
		if(pd instanceof NumericParameterDomain) {
			NumericParameterDomain n_pd = (NumericParameterDomain) pd;
			return n_pd.isInteger() ? "integer" : "float";
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
