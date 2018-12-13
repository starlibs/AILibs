package jaicore.planning.hierarchical.problems.ceocstn.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFileChooser;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class CEOCSTN2Shop2 {

	private static String packageName;

	/**
	 * Writes the given Problem into the output file
	 * 
	 * @param problem the problem form which the domain should be written into a
	 *                file
	 * @param output  into this file
	 * @throws IOException
	 */
	public static void printDomain(CEOCSTNPlanningProblem problem, File output) throws IOException {

		FileWriter fileWriter = new FileWriter(output);
		try (BufferedWriter bw = new BufferedWriter(fileWriter)) {

			String fileName = output.getName();
			fileName = fileName.substring(0, fileName.indexOf(".")).toLowerCase();

			// print the package if one is availiable
			if (packageName != "") {
				bw.write("(int-package : " + packageName + ")");
				bw.flush();
			}

			// Writing of the domain file
			bw.write("(defun define-" + fileName + "-domain()\n");
			bw.write(indent(1) + "(let (( * define-silently* t))\n");
			bw.flush();

			bw.write(indent(1) + "(defdomain (" + fileName + " :redinfe-ok t)(\n");
			bw.flush();

			// print the operations
//		problem.getDomain().getOperations().stream().forEach(operation-> {
//			try {
//				printOperation(bw, operation, 3);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
//		
//		problem.getDomain().getMethods().stream().forEach(method->{
//			try {
//				printMethod(bw, method, 3);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
//		
			bw.write(indent(1) + ")\n");
			bw.write("\t)\n)");
			bw.flush();
		}

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
		bw.write(indent(i) + "(:operator (!" + operation.getName());
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
		printMonom(bw, operation.getPrecondition(), 4);

		// print the delete List of the operation
		printMonomMap(bw, operation.getDeleteLists(), 4);

		// print the add List of the operation
		printMonomMap(bw, operation.getAddLists(), 4);

		bw.write(indent(i) + ")\n\n");
		bw.flush();

	}

	/**
	 * Prints a Map with CNFFormula as keys and Monom to the bufferedwriter
	 * 
	 * @param bw  the bufferedwriter which determines the goal location
	 * @param map the map contianing the monoms
	 * @param i   the number if indents to create in front of the map
	 * @throws IOException
	 */
	private static void printMonomMap(BufferedWriter bw, Map<CNFFormula, Monom> map, int i) throws IOException {

		map.values().stream().forEach(member -> {
			try {
				printMonom(bw, member, 4);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		bw.flush();

	}

	/**
	 * Prints a single monom into the bufferedwriter
	 * 
	 * @param bw    the bufferedwriter which determines the output
	 * @param monom the monom to write
	 * @param i     the number if indents infront of the monom
	 * @throws IOException
	 */
	public static void printMonom(BufferedWriter bw, Monom monom, int i) throws IOException {
		printMonom(bw, monom, i, false);
	}

	/**
	 * Prints a single monom into the bufferedwriter
	 * 
	 * @param bw    the bufferedwriter which determines the output
	 * @param monom the monom to write
	 * @param i     the number if indents infront of the monom
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
	 * @param bw      the bufferedwriter which determines the output
	 * @param literal the literal to write
	 * @throws IOException
	 */
	public static void printLiteral(BufferedWriter bw, Literal lit) throws IOException {
		bw.write(" (");
		bw.write(lit.getProperty());
		lit.getParameters().forEach(param -> {
			try {
				bw.write(" ?" + param.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		bw.write(")");
		bw.flush();
	}

	/**
	 * Prints a mehtod into the given writer
	 * 
	 * @param bw     the writer where the method should be written to
	 * @param method the method to write
	 * @param i      the number of indents infront of the method
	 * @throws IOException
	 */
	public static void printMethod(BufferedWriter bw, Method method, int i) throws IOException {
		bw.write(indent(i) + "(:method (" + method.getName());
		method.getParameters().forEach(param -> {
			try {
				bw.write(" ?" + param.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		bw.write(")\n");

		// write the precondition into the file
		printMonom(bw, method.getPrecondition(), i + 1);

		// print the tasknetwork
		printNetwork(bw, method.getNetwork().getRoot(), method.getNetwork(), true, 4);

		bw.write(indent(i) + ")\n");
		bw.flush();
	}

	/**
	 * prints the root of the network
	 * 
	 * @param bw
	 * @param lit
	 * @param network
	 * @param ordered
	 * @param i
	 * @throws IOException
	 */
	private static void printNetwork(BufferedWriter bw, Literal lit, TaskNetwork network, boolean ordered, int i)
			throws IOException {
		bw.write(indent(i) + "(");
		if (lit.equals(network.getRoot())) {
			if (ordered)
				bw.write(":ordered\n");
			else
				bw.write(":unordered\n");
		}
		// write the parameters of the current literal
		bw.write(indent(i + 1) + "(" + lit.getProperty());
		bw.flush();
		lit.getParameters().stream().forEach(param -> {
			try {
				bw.write(" ?" + param.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		bw.write(")\n");

		network.getSuccessors(lit).stream().forEach(literal -> {
			try {
				printNetwork(bw, literal, network, i + 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

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
	private static void printNetwork(BufferedWriter bw, Literal lit, TaskNetwork network, int i) throws IOException {
		bw.write(indent(i) + "(" + lit.getProperty());
		lit.getParameters().stream().forEach(param -> {
			try {
				bw.write(" ?" + param.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		bw.write(")\n");
	}

	public static void printProblem(CEOCSTNPlanningProblem problem, File output) throws IOException {
		FileWriter fileWriter = new FileWriter(output);
		BufferedWriter bw = new BufferedWriter(fileWriter);

		String fileName = output.getName();
		fileName = fileName.substring(0, fileName.indexOf(".")).toLowerCase();

		// print the package if one is availiable
		if (packageName != "") {
			bw.write("(int-package : " + packageName + ")");
			bw.flush();
		}

		// Writing of the domain file
		bw.write("(make-problem '" + fileName + "'-01 ' " + fileName + "\n");
		bw.write(indent(1) + "(\n");
		// print inital state
		printMonom(bw, problem.getInit(), 2, true);

		// print tasknetwork
		printNetwork(bw, problem.getNetwork().getRoot(), problem.getNetwork(), 2);

		bw.write(indent(1) + ")\n");
		bw.write(")");
		bw.flush();
	}

	// creates a number of intends;
	public static String indent(int numberOfIntends) {
		String r = "";
		for (int i = 0; i < numberOfIntends; i++) {
			r += "\t";
		}
		return r;
	}

	public static void print(CEOCSTNPlanningProblem problem) {
		print(problem, "");
	}

	public static void print(CEOCSTNPlanningProblem problem, String packageName) {
		CEOCSTN2Shop2.packageName = packageName;
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Domain-File");
		chooser.showOpenDialog(null);
		File domainFile = chooser.getSelectedFile();

		try {
			printDomain(problem, domainFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		chooser.setDialogTitle("Problem-File");
		chooser.showOpenDialog(null);
		File problemFile = chooser.getSelectedFile();
		try {
			printProblem(problem, problemFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
