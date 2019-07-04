package ai.libs.jaicore.planning.hierarchical.problems.ceocstn.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFileChooser;

import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.LiteralParam;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.structure.VariableParam;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCOperation;
import ai.libs.jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.AShop2Converter;
import ai.libs.jaicore.planning.hierarchical.problems.stn.Method;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class CEOCSTN2Shop2 extends AShop2Converter {

	private String packageName;

	/**
	 * Writes the given Problem into the output file
	 *
	 * @param problem
	 *            the problem form which the domain should be written into a file
	 * @param output
	 *            into this file
	 * @throws IOException
	 */
	public void printDomain(final File output) throws IOException {

		FileWriter fileWriter = new FileWriter(output);
		try (BufferedWriter bw = new BufferedWriter(fileWriter)) {

			String fileName = output.getName();
			fileName = fileName.substring(0, fileName.indexOf('.')).toLowerCase();

			// print the package if one is availiable
			if (!this.packageName.isEmpty()) {
				bw.write("(int-package : " + this.packageName + ")");
				bw.flush();
			}

			// Writing of the domain file
			bw.write("(defun define-" + fileName + "-domain()\n");
			bw.write(this.indent(1) + "(let (( * define-silently* t))\n");
			bw.flush();

			bw.write(this.indent(1) + "(defdomain (" + fileName + " :redinfe-ok t)(\n");
			bw.flush();

			bw.write(this.indent(1) + ")\n");
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
	public void printOperation(final BufferedWriter bw, final CEOCOperation operation, final int i) throws IOException {
		bw.write(this.indent(i) + "(:operator (!" + operation.getName());
		// print the parameter of the operation
		for (VariableParam param : operation.getParams()) {
			bw.write(" ?" + param.getName());
			bw.flush();
		}
		bw.write(")\n ");
		// print Preconditions
		this.printMonom(bw, operation.getPrecondition(), 4);

		// print the delete List of the operation
		this.printMonomMap(bw, operation.getDeleteLists(), 4);

		// print the add List of the operation
		this.printMonomMap(bw, operation.getAddLists(), 4);

		bw.write(this.indent(i) + ")\n\n");
		bw.flush();

	}

	/**
	 * Prints a Map with CNFFormula as keys and Monom to the bufferedwriter
	 *
	 * @param bw
	 *            the bufferedwriter which determines the goal location
	 * @param map
	 *            the map contianing the monoms
	 * @param i
	 *            the number if indents to create in front of the map
	 * @throws IOException
	 */
	private void printMonomMap(final BufferedWriter bw, final Map<CNFFormula, Monom> map, final int i) throws IOException {
		for (Monom member : map.values()) {
			this.printMonom(bw, member, i);
		}
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
	@Override
	public void printLiteral(final BufferedWriter bw, final Literal lit) throws IOException {
		bw.write(" (");
		bw.write(lit.getProperty());
		for (LiteralParam param : lit.getParameters()) {
			bw.write(" ?" + param.getName());
		}
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
	public void printMethod(final BufferedWriter bw, final Method method, final int i) throws IOException {
		bw.write(this.indent(i) + "(:method (" + method.getName());
		for (LiteralParam param : method.getParameters()) {
			bw.write(" ?" + param.getName());
		}
		bw.write(")\n");

		// write the precondition into the file
		this.printMonom(bw, method.getPrecondition(), i + 1);

		// print the tasknetwork
		this.printNetwork(bw, method.getNetwork().getRoot(), method.getNetwork(), true, 4);

		bw.write(this.indent(i) + ")\n");
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
	private void printNetwork(final BufferedWriter bw, final Literal lit, final TaskNetwork network, final boolean ordered, final int i) throws IOException {
		bw.write(this.indent(i) + "(");
		if (lit.equals(network.getRoot())) {
			if (ordered) {
				bw.write(":ordered\n");
			} else {
				bw.write(":unordered\n");
			}
		}
		// write the parameters of the current literal
		bw.write(this.indent(i + 1) + "(" + lit.getProperty());
		bw.flush();
		for (LiteralParam param : lit.getParameters()) {

			bw.write(" ?" + param.getName());
		}

		bw.write(")\n");

		for (Literal literal : network.getSuccessors(lit)) {
			this.printNetwork(bw, literal, i + 1);
		}

		bw.write(this.indent(i) + ")\n");
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
	private void printNetwork(final BufferedWriter bw, final Literal lit, final int i) throws IOException {
		bw.write(this.indent(i) + "(" + lit.getProperty());
		for (LiteralParam param : lit.getParameters()) {
			bw.write(" ?" + param.getName());
		}
		bw.write(")\n");
	}

	public void printProblem(final CEOCSTNPlanningProblem problem, final File output) throws IOException {
		FileWriter fileWriter = new FileWriter(output);
		try (BufferedWriter bw = new BufferedWriter(fileWriter)) {

			String fileName = output.getName();
			fileName = fileName.substring(0, fileName.indexOf('.')).toLowerCase();

			// print the package if one is availiable
			if (!this.packageName.isEmpty()) {
				bw.write("(int-package : " + this.packageName + ")");
				bw.flush();
			}

			// Writing of the domain file
			bw.write("(make-problem '" + fileName + "'-01 ' " + fileName + "\n");
			bw.write(this.indent(1) + "(\n");
			// print inital state
			this.printMonom(bw, problem.getInit(), 2, true);

			// print tasknetwork
			this.printNetwork(bw, problem.getNetwork().getRoot(), 2);

			bw.write(this.indent(1) + ")\n");
			bw.write(")");
			bw.flush();
		}
	}

	public void print(final CEOCSTNPlanningProblem problem) throws IOException {
		this.print(problem, "");
	}

	public void print(final CEOCSTNPlanningProblem problem, final String packageName) throws IOException {
		this.packageName = packageName;
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Domain-File");
		chooser.showOpenDialog(null);
		File domainFile = chooser.getSelectedFile();

		this.printDomain(domainFile);

		chooser.setDialogTitle("Problem-File");
		chooser.showOpenDialog(null);
		File problemFile = chooser.getSelectedFile();
		this.printProblem(problem, problemFile);
	}
}
