package de.upb.crc901.mlplan.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.VariableParam;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class PlanExecutor {

	private static final Logger logger = LoggerFactory.getLogger(PlanExecutor.class);
	private final Random random;

	public PlanExecutor(Random random) {
		super();
		this.random = random;
	}

	/**
	 * 
	 * @param plan
	 * @param variables
	 * @return Returns the objects that exist at the end of the execution
	 * @throws Throwable
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public Map<ConstantParam, Object> executePlan(List<CEOCAction> plan, Map<ConstantParam, Object> planInputData) throws Throwable, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {

		/* initialize a copy of the data. The variables object will maintain the objects we know */
		Map<ConstantParam, Object> variables = new HashMap<>(planInputData);

		/* the node object is the node in the execution tree we are currently in (relevant only for caching) */

		for (CEOCAction a : plan) {

			/** otherwise, we now perform this computation and create a new node for it in the execution tree **/

			/* create local copies of the operation, its inputs and outputs. Using this, we then match the operation with a concrete java method */
			CEOCOperation op = a.getOperation();
			Map<VariableParam, ConstantParam> grounding = a.getGrounding();
			ConstantParam outParam = op.getOutputs().isEmpty() ? null : grounding.get(op.getOutputs().get(0));
			List<ConstantParam> inputs = new ArrayList<>(
					SetUtil.difference(op.getParams(), op.getOutputs()).stream().map(v -> a.getGrounding().get(v)).collect(Collectors.toList()));

			/* the whole switch-statement: determine whether this is a "native" statement or a real java call. */

			/** @TODO: Problem: Eigentlich bräuchte man ein cloneable um wirkliche Kopien von den Objekten zu erzeugen. Sonst können wir den ExecutionTree gar nicht so bauen. */
			switch (a.getOperation().getName()) {
			case "copyNormalizedInstances": {
				variables.put(inputs.get(1), variables.get(inputs.get(0)));
			}
			case "noaddSingleParam":
			case "noaddValuedParam":
			case "inheritInstanceProp":
			case "continueParamRefine":
			case "stopParamRefine":
			case "setLocal":
			case "setActive":
			case "setStopped":
			case "noop": {
				break;
			}
			case "computeValue": {
				if (!(variables.get(inputs.get(0)) instanceof List<?>)) {
					throw new IllegalArgumentException("Input " + inputs.get(0) + " is not a value refinement list.");
				}
				List<String> vList = (List<String>) variables.get(inputs.get(0));
				double lb = Double.parseDouble(inputs.get(1).getName());
				double ub = Double.parseDouble(inputs.get(2).getName());
				double d = Double.parseDouble(inputs.get(3).getName());
				String type = inputs.get(4).getName();
				int x = Integer.parseInt(inputs.get(5).getName());
    			double pValue;
	    		if ( type.equals("LOG_INTEGER") || type.equals("LOG_NUMERIC") )
					pValue = Math.log10(d);
	    		else
	    			pValue = d;
//System.err.println(vList);
	    		for ( String s : vList ) {
					int i = Integer.parseInt(s);
					pValue += i*Math.pow(10,x);
					x--; 
				}					
	    		if ( type.equals("LOG_INTEGER") || type.equals("LOG_NUMERIC") )
					pValue = Math.pow(10,pValue);
				pValue = Math.min(pValue,ub);
				pValue = Math.max(pValue,lb);
	    		if ( type.equals("INTEGER") || type.equals("LOG_INTEGER") )
					variables.put(outParam, (int)(Math.floor(pValue)));
				else
					variables.put(outParam, pValue);
				break;
			}
			case "initValueList":
			case "getOptionList": {
				variables.put(outParam, new ArrayList<>());
				break;
			}
			case "addSingleParam":
			case "addOption": {
				if (!(variables.get(inputs.get(0)) instanceof List<?>))
					throw new IllegalArgumentException("Input " + inputs.get(0) + " is not an option list.");
				else
					((List<String>) variables.get(inputs.get(0))).add(inputs.get(1).getName()); // here we implicitly assume that the value of the input is a constant
				break;
			}
			case "addValuedParam":
			case "addOptionPair": {
				if (!(variables.get(inputs.get(0)) instanceof List<?>))
					throw new IllegalArgumentException("Input " + inputs.get(0) + " is not an option list.");
				else {
					/* here we implicitly assume that the value of the input is a constant */
					((List<String>) variables.get(inputs.get(0))).add(inputs.get(1).getName());
					if (variables.containsKey(inputs.get(2))) {
						((List<String>) variables.get(inputs.get(0))).add(variables.get(inputs.get(2)).toString());
						// List<String> aList = (List<String>)variables.get(inputs.get(0));
						// System.out.println(aList);
					} else {
						((List<String>) variables.get(inputs.get(0))).add(inputs.get(2).getName());
					}
				}
				break;
			}
			case "addLocalOptions": {
				if (!(variables.get(inputs.get(0)) instanceof List<?>))
					throw new IllegalArgumentException("Input " + inputs.get(0) + " is not an option list.");
				else {
					/* here we implicitly assume that the value of the input is a constant */
					((List<String>) variables.get(inputs.get(0))).add(inputs.get(1).getName());
					String[] localParams = inputs.get(2).getName().split(" ");
					for (int i = 0; i < localParams.length; i++) {
						((List<String>) variables.get(inputs.get(0))).add(localParams[i]);
					}
				}
				break;
			}
			case "assignTo": { // TODO: appendOprions instead??
				if (variables.containsKey(inputs.get(0))) {
					variables.put(outParam, variables.get(inputs.get(0)));
				} else {
					variables.put(outParam, inputs.get(0));
				}
				break;
			}
			case "concatenateWithName": { // TODO: appendOptions instead??
				if (!(variables.get(inputs.get(1)) instanceof List<?>))
					throw new IllegalArgumentException("Input " + inputs.get(1) + " is not an option list.");
				else {
					/* here we implicitly assume that the value of the input is a constant */
					String qString = "" + variables.get(inputs.get(0));
					List<String> aList = (List<String>) variables.get(inputs.get(1));
					for (int i = 0; i < aList.size(); i++) {
						qString += " ";
						if ( containsSpace(aList.get(i)) ) {
							qString += "\"";
							if ( aList.get(i).indexOf("\"") == -1 ) {
								qString += aList.get(i);
							} else {
								qString += escapeQuotes(aList.get(i));								
							}
							qString += "\"";
						} else {
							qString += aList.get(i);
						}
					}
					variables.put(outParam, qString);
				}
				break;
			}
			case "concatenate": { // TODO: appendOprions instead??
				if (!(variables.get(inputs.get(0)) instanceof List<?>))
					throw new IllegalArgumentException("Input " + inputs.get(0) + " is not an option list.");
				else {
					/* here we implicitly assume that the value of the input is a constant */
					String qString = "";
					List<String> aList = (List<String>) variables.get(inputs.get(0));
					for (int i = 0; i < aList.size(); i++) {
						if ( containsSpace(aList.get(i)) ) {
							qString += "\"";
							if ( aList.get(i).indexOf("\"") == -1 ) {
								qString += aList.get(i);
							} else {
								qString += escapeQuotes(aList.get(i));								
							}
							qString += "\"";
						} else {
							qString += aList.get(i);
						}
						if (i < aList.size() - 1)
							qString += " ";
					}
					variables.put(outParam, qString);
				}
				continue;
			}
			case "appendOptions": {
				if (!(variables.get(inputs.get(0)) instanceof List<?>) || !(variables.get(inputs.get(1)) instanceof List<?>))
					throw new IllegalArgumentException("Input " + inputs.get(0) + " or " + inputs.get(1) + " is not an option list.");
				else {
					((List<String>) variables.get(inputs.get(0))).addAll((List<String>) variables.get(inputs.get(1)));
				}
				break;
			}
			case "selectFeatures": {
				variables.put(inputs.get(0), selectFeatures((Instances) variables.get(inputs.get(0))));
				break;
			}
			case "compileOptionListToArray": {
				List<String> oList = (List<String>) variables.get(inputs.get(0));
				String[] opts = new String[oList.size()];
				oList.toArray(opts);
				variables.put(outParam, opts);
				break;
			}

			/* now consider the case that methods are called using reflection */
			default: {
				Object obj = null;
				java.lang.reflect.Method method = null;
				Object[] params = null;
				switch (a.getOperation().getName()) {
				case "split": {

					Class<?> clazz = Class.forName("util.ml.WekaUtil");
					method = clazz.getMethod("getStratifiedSplit", weka.core.Instances.class, Random.class, double[].class);
					params = new Object[] { variables.get(inputs.get(0)), random, new double[] { .6f } };
					break;
				}
				case "retrieveTrain": {
					Class<?> clazz = List.class;
					obj = variables.get(inputs.get(0));
					if (obj == null)
						throw new IllegalArgumentException("Cannot invoke get item from null list. Vars: " + inputs);
					method = clazz.getMethod("get", int.class);
					params = new Object[] { 0 };
					break;
				}
				case "retrieveTest": {
					Class<?> clazz = List.class;
					obj = variables.get(inputs.get(0));
					if (obj == null)
						throw new IllegalArgumentException("Cannot invoke get item from null list. Vars: " + inputs);
					method = clazz.getMethod("get", int.class);
					params = new Object[] { 1 };
					break;
				}

				default:
					String[] opSplit = a.getOperation().getName().split(":");
					Class<?> clazz = Class.forName(opSplit[0]);

					/* if this is a constructor call, create the object */
					if (opSplit[1].equals("__construct")) {
						Class<?>[] inputClasses = new Class<?>[inputs.size()];
						params = new Object[inputClasses.length];
						for (int i = 0; i < inputs.size(); i++) {
							params[i] = variables.get(inputs.get(i));
							inputClasses[i] = params[i].getClass();
						}
						Constructor<?> ctor = clazz.getConstructor(inputClasses);
						variables.put(outParam, ctor.newInstance(params));
						logger.info("Binding new object {} to variable {}.", variables.get(outParam), outParam);
						continue;
					}
					/* otherwise, prepare everything for the method invocation */
					else {

						/* try to determine the method called here */
						method = getMethod(clazz, opSplit[1], inputs, variables);
						if (method == null)
							throw new IllegalArgumentException("Could not find the method " + opSplit[1] + " for class " + clazz + " to be called with inputs " + inputs + ". ");

						if (Modifier.isStatic(method.getModifiers())) {
							params = new Object[method.getParameterCount()];
							for (int i = 0; i < inputs.size(); i++) {
								params[i] = variables.get(inputs.get(i));
							}
							if (method.isVarArgs() && inputs.size() <= params.length) {
								params[params.length - 1] = new Object[0];
							}
						}

						/* if this is not a static call, use first element as the object on which the method is invoked */
						else {
							obj = variables.get(inputs.get(0));

							params = new Object[method.getParameterCount()];
							Class<?>[] requiredInputTypes = method.getParameterTypes();
							for (int i = 1; i < inputs.size(); i++) {
								params[i - 1] = variables.get(inputs.get(i));
								if (!(requiredInputTypes[i - 1].isAssignableFrom(params[i - 1].getClass())
										|| requiredInputTypes[i - 1].equals(int.class) && params[i - 1].getClass().equals(Integer.class)))
									throw new IllegalArgumentException("The " + (i - 1) + "-th param of " + method.getName() + " must be " + requiredInputTypes[i - 1] + ", but "
											+ params[i - 1].getClass() + " is given!");
							}
							if (method.isVarArgs() && inputs.size() <= params.length) {
								params[params.length - 1] = new Object[0];
							}
						}
					}
					break;
				}

				/* execute the configured step */
				if (method == null)
					throw new IllegalStateException(
							"No method to invoke for " + a.getEncoding() + " with param types " + a.getParameters() + " where variables = " + variables.keySet());
				if (method.getParameterCount() > 0 && (params == null || params.length != method.getParameterCount()))
					throw new IllegalStateException("Cannot invoke " + obj + "." + method.getName() + " with " + (params == null ? "NULL" : params.length) + " parameters where "
							+ method.getParameterCount() + " are expected.");
				boolean hasReturnValue = !method.getReturnType().getName().equals("void");
				if (!hasReturnValue && a.getOperation().getOutputs().size() > 0)
					throw new IllegalArgumentException("Plan contains action " + a + " whose method has a return value, but the operation supposes that there are "
							+ a.getOperation().getOutputs().size() + " outputs.");
				Object out = null;

				if (obj != null && !method.getDeclaringClass().isInstance(obj))
					throw new IllegalArgumentException(
							"Cannot invoke method " + method.getName() + " of class " + method.getDeclaringClass().getName() + " on object of type " + obj.getClass().getName());
				try {
					out = method.invoke(obj, params);
				} catch (InvocationTargetException e) {
					System.err.println("Error when invoking " + method.getName() + " on " + obj + " with inputs "
							+ Arrays.asList(params).stream().map(i -> i instanceof String[] ? Arrays.toString((String[]) i) : i).collect(Collectors.toList()));
					throw e.getTargetException();
				}

				/* update knowledge */
				if (hasReturnValue && outParam != null) {
					if (out == null)
						throw new IllegalStateException("Output of " + a.getEncoding() + " is NULL but must not be null!");
					variables.put(outParam, out);
				}
			}
			}

		}
		return variables;
	}

	/**
	 * 
	 * @param clazz
	 * @param methodName
	 * @param inputs
	 * @param variables
	 *            Variables that currently exist in the context
	 * @return
	 */
	private java.lang.reflect.Method getMethod(Class<?> clazz, String methodName, List<ConstantParam> inputs, Map<ConstantParam, Object> variables) {

		Class<?>[] staticInputClasses = new Class<?>[inputs.size()];
		for (int i = 0; i < inputs.size(); i++) {
			if (!variables.containsKey(inputs.get(i)))
				throw new IllegalStateException("Cannot use " + inputs.get(i) + " as an input, because it has not been defined!");
			staticInputClasses[i] = variables.get(inputs.get(i)).getClass();
		}
		Class<?>[] objectInputClasses = new Class<?>[inputs.size() - 1];
		for (int i = 1; i < inputs.size(); i++) {
			if (!variables.containsKey(inputs.get(i)))
				throw new IllegalStateException("Cannot use " + inputs.get(i) + " as an input, because it has not been defined!");
			objectInputClasses[i - 1] = variables.get(inputs.get(i)).getClass();
		}

		for (java.lang.reflect.Method m : clazz.getMethods()) {
			if (m.getName().equals(methodName)) {

				/* check method considering all types */
				if (Modifier.isStatic(m.getModifiers())) {
					if (doInputsMatchSignature(m, staticInputClasses))
						return m;
				}

				/* check method ignoring the first type */
				else {
					if (doInputsMatchSignature(m, objectInputClasses))
						return m;
				}
			}
		}
		return null;
	}

	private boolean doInputsMatchSignature(Method m, Class<?>[] inputClasses) {
		Class<?>[] types = m.getParameterTypes();
		boolean varArgs = m.isVarArgs();

		/* initial response on parameter count */
		if (!((!varArgs && types.length == inputClasses.length) || (varArgs && types.length - 1 <= inputClasses.length)))
			return false;

		/* then check subsumation */
		for (int i = 0; i < types.length; i++) {
			if ((!varArgs || i < types.length - 1) && !(types[i].isAssignableFrom(inputClasses[i]) || types[i].equals(int.class) && inputClasses[i].equals(Integer.class))) {
				return false;
			} else if (varArgs && i == types.length - 1 && inputClasses.length >= types.length) {
				System.out.println("OK");
			}
		}
		return true;
	}

	private Instances selectFeatures(Instances data) {
		List<Integer> indexes = new ArrayList<>();
		AttributeSelection attsel = new AttributeSelection();
		// FuzzyRoughSubsetEval eval = new FuzzyRoughSubsetEval();
		// HillClimber search = new HillClimber();
		CfsSubsetEval eval = new CfsSubsetEval();
		GreedyStepwise search = new GreedyStepwise();
		attsel.setEvaluator(eval);
		attsel.setSearch(search);
		try {
			attsel.SelectAttributes(data);
			for (int att : attsel.selectedAttributes())
				indexes.add(att);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Instances reducedData = new Instances(data);
		int removedItems = 0;
		try {
			for (int i = 0; i < data.numAttributes(); i++) {
				if (!indexes.contains(i)) {
					String[] options = new String[2];
					options[0] = "-R"; // "range"
					options[1] = String.valueOf(1 + i - removedItems); // first attribute
					Remove remove = new Remove(); // new instance of filter
					remove.setOptions(options); // set options
					remove.setInputFormat(reducedData);
					reducedData = Filter.useFilter(reducedData, remove);
					removedItems++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return reducedData;
	}

	private String escapeQuotes(String param) {
		String[] paramParts = param.split("\"");
		
		String requoted = "";
		for ( int i=0 ; i < paramParts.length ; i++ ) {
			String part = paramParts[i];
			if ( i == paramParts.length-1 ) {
				if ( ! param.endsWith("\"") ) {
					requoted += part;
					continue;
				}
			}
			int count = 0;
			while ( part.endsWith("\\") ) {
				count++;
			    part = part.substring(0, part.length()-1);
			}
			requoted += part;
			for ( int j=0 ; j<count ; j++ )
				requoted += "\\\\";
			requoted += "\\\"";			
		}
		return requoted;
	}
	
	private void concatenateSublists(List<String> list) {
		while ( true ) {
			int i = list.lastIndexOf("@DOUBLEQUOTESTART");
			if ( i < 0 ) break;
			String result = "";
			for ( int j = i+1 ; j < list.size() ; j++ ) {
				String s = list.get(j);
				list.remove(j);
				if ( s.equals("@DOUBLEQUOTEEND") )
					break;
				if ( result.isEmpty() )
					result = s;
				else
					result += " " + s;
			}
			result = escapeQuotes(result);
		}
	}

	private boolean containsSpace(String param) {
		
		String[] paramParts = param.split("\"");
		
		int offset = 0;
		
		while ( offset < paramParts.length ) {
			if ( paramParts[offset].indexOf(" ") >= 0 ) {
				return true;
			}
			if ( offset == paramParts.length-1 ) {
				if ( param.endsWith("\"") ) {
					System.err.println("Error in paramter string >> " + param + " <<: unbalanced quotes!");
					break;
				} else {
					return false;
				}
			}

			if ( paramParts[offset].endsWith("\\") ) {
				System.err.println("Error in paramter string >> " + param + " <<: outermost quote is escaped!");
				break;
			}
			
			int index = offset;
			index++;
			while ( index < paramParts.length ) {
				if ( ! paramParts[index].endsWith("\\") ) {
					break;
				}
				index++;
			}
			
			if ( (index == paramParts.length) ||
				 (index == paramParts.length-1)	&& !param.endsWith("\"") ) {
				System.err.println("Error in paramter string >> " + param + " <<: unbalanced quotes!");
				break;
			}
			if ( index == paramParts.length-1 ) {
				return false;
			}
			offset = index+1;
		}
		
		return true;
	}

}
