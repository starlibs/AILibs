package jaicore.planning.model.task.ceocipstn;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.TaskNetwork;

public class CEOCIPSTN2JSHOP2 {


	/**
	 * Writes the given Problem into the output file
	 * 
	 * @param problem
	 *            the problem form which the domain should be written into a file
	 * @param output
	 *            into this file
	 * @throws IOException
	 */
	public static void printDomain(CEOCIPSTNPlanningProblem problem, Writer writer, String name) throws IOException {

		BufferedWriter bw = new BufferedWriter(writer);

		// Writing of the domain file

		bw.write("(defdomain " + name + "\n " + indent(1) +  " (\n");
		bw.flush();

		// print the operations
//		problem.getDomain().getOperations().stream().forEach(operation -> {
//			try {
////				printOperation(bw, operation, 2);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});

//		problem.getDomain().getMethods().stream().forEach(method -> {
//			try {
//				printMethod(bw, method, 2);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});

		bw.write(indent(1) + ")\n");
		bw.write(")");
		bw.flush();

	}

	/**
	 * Prints the operations of the domain into a FIle
	 * 
	 * @param bw
	 * @param operation
	 * @param i
	 * @throws IOException
	 */
	public static void printOperation(BufferedWriter bw, CEOCOperation operation, int i) throws IOException {
		bw.write(indent(i) + "(:operator (!" + maskString(operation.getName()));
		// print the parameter of the operation
		operation.getParams().stream().forEach(param -> {
			try {
				bw.write(" ?" + param.getName());
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		});
		bw.write(")\n ");
		// print Preconditions
		printMonom(bw, operation.getPrecondition(), i + 1);

		// print the delete List of the operation
		Map<CNFFormula, Monom> deleteLists = operation.getDeleteLists();
		boolean containsUnconditionalDelete = deleteLists.containsKey(new CNFFormula());
		if (deleteLists.size() > 1 || deleteLists.size() == 1 && !containsUnconditionalDelete)
			throw new IllegalArgumentException("The operation " + operation.getName() + " contains conditional deletes, which cannot be converted to JSHOP2 format.");
		Monom deleteList = containsUnconditionalDelete ? deleteLists.get(new CNFFormula()) : new Monom();
		printMonom(bw, deleteList, i + 1);

		// print the add List of the operation
		Map<CNFFormula, Monom> addLists = operation.getAddLists();
		boolean containsUnconditionalAdd = deleteLists.containsKey(new CNFFormula());
		Monom addList = containsUnconditionalAdd ? addLists.get(new CNFFormula()) : new Monom();
		if (addLists.size() > 1 || addLists.size() == 1 && !containsUnconditionalAdd)
			throw new IllegalArgumentException("The operation " + operation.getName() + " contains conditional adds, which cannot be converted to JSHOP2 format.");
		printMonom(bw, addList, i + 1);

		bw.write(indent(i) + ")\n\n");
		bw.flush();

	}

	/**
	 * Prints a single monom into the bufferedwriter
	 * 
	 * @param bw
	 *            the bufferedwriter which determines the output
	 * @param monom
	 *            the monom to write
	 * @param i
	 *            the number if indents infront of the monom
	 * @throws IOException
	 */
	public static void printMonom(BufferedWriter bw, Monom monom, int i) throws IOException {
		printMonom(bw, monom, i, false);
	}

	/**
	 * Prints a single monom into the bufferedwriter
	 * 
	 * @param bw
	 *            the bufferedwriter which determines the output
	 * @param monom
	 *            the monom to write
	 * @param i
	 *            the number if indents infront of the monom
	 * @throws IOException
	 */
	public static void printMonom(BufferedWriter bw, Monom monom, int i, boolean newline) throws IOException {
		bw.write(indent(i) + "(");
		monom.stream().forEach(lit -> {
			try {
				printLiteral(bw, lit);
				if (newline)
					bw.write("\n" + indent(i) + " ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		bw.write(")\n");
		bw.flush();
	}

	/**
	 * Prints a single literal into the bufferedwriter
	 * 
	 * @param bw
	 *            the bufferedwriter which determines the output
	 * @param literal
	 *            the literal to write
	 * @throws IOException
	 */
	public static void printLiteral(BufferedWriter bw, Literal lit) throws IOException {
		bw.write(" (");
		boolean negated = lit.isNegated();
		if (negated)
			bw.write("not(");
		bw.write(maskString(lit.getPropertyName()));
		lit.getParameters().forEach(param -> {
			try {
				bw.write(" ?" + maskString(param.getName()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		if (negated)
			bw.write(")");
		bw.write(")");
		bw.flush();
	}

	/**
	 * Prints a mehtod into the given writer
	 * 
	 * @param bw
	 *            the writer where the method should be written to
	 * @param method
	 *            the method to write
	 * @param i
	 *            the number of indents infront of the method
	 * @throws IOException
	 */
	public static void printMethod(BufferedWriter bw, Method method, int i) throws IOException {
		bw.write(indent(i) + "(:method");
		printLiteral(bw, method.getTask());
		bw.write("\n");
		bw.write(indent(i + 1) + method.getName());
		bw.write("\n");

		// write the precondition into the file
		printMonom(bw, method.getPrecondition(), i + 1);

		// print the tasknetwork
		printNetwork(bw, method.getNetwork(), i + 1);

		bw.write(indent(i) + ")\n");
		bw.flush();
	}


	/**
	 * prints an arbitrary literal of the network
	 * 
	 * @param bw
	 * @param lit
	 * @param network
	 * @param i
	 * @throws IOException
	 */
	private static void printNetwork(BufferedWriter bw, TaskNetwork network, int i) throws IOException {
		bw.write(indent(i) + "(");
		Literal next = network.getRoot();
		while (next != null) {
			printLiteral(bw, next);
			Iterator<Literal> it = network.getSuccessors(next).iterator();
			next = it.hasNext() ? it.next() : null;
		}
		bw.write(")\n");
	}

	public static void printProblem(CEOCIPSTNPlanningProblem problem,  Writer writer, String name) throws IOException {
		BufferedWriter bw = new BufferedWriter(writer);

		// print the package if one is availiable
//		if (packageName != "") {
//			bw.write("(int-package : " + packageName + ")");
//			bw.flush();
//		}

		// Writing of the domain file
		bw.write("(defproblem problem " + name + "\n");
		bw.write(indent(1) + "(\n");
		// print inital state
		printMonom(bw, problem.getInit(), 2, true);
		bw.write(indent(1) + ")\n");
		bw.write(indent(1) + "(\n");

		// print tasknetwork
		printNetwork(bw, problem.getNetwork(), 2);

		bw.write(indent(1) + ")\n");
		bw.write(")");
		bw.flush();
	}
	
	public static String maskString(String str) {
		str = str.replaceAll("\\.", "_");
		str = str.replaceAll(":", "__");
		str = str.replaceAll("<", "___");
		str = str.replaceAll(">", "___");
		str = str.replaceAll("\\[\\]", "Array");
		return str;
	}

	// creates a number of intends;
	public static String indent(int numberOfIntends) {
		String r = "";
		for (int i = 0; i < numberOfIntends; i++) {
			r += "\t";
		}
		return r;
	}
}
