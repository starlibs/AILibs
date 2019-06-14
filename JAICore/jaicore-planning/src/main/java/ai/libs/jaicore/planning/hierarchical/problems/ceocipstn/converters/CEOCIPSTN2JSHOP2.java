package ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.converters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.LiteralParam;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCOperation;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.AShop2Converter;
import ai.libs.jaicore.planning.hierarchical.problems.stn.Method;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class CEOCIPSTN2JSHOP2 extends AShop2Converter {

	/**
	 * Writes the given Problem into the output file
	 *
	 * @param problem
	 *            the problem form which the domain should be written into a file
	 * @param output
	 *            into this file
	 * @throws IOException
	 */
	public void printDomain(final Writer writer, final String name) throws IOException {

		BufferedWriter bw = new BufferedWriter(writer);

		// Writing of the domain file

		bw.write("(defdomain " + name + "\n " + this.indent(1) + " (\n");
		bw.flush();

		bw.write(this.indent(1) + ")\n");
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
	public void printOperation(final BufferedWriter bw, final CEOCOperation operation, final int i) throws IOException {
		bw.write(this.indent(i) + "(:operator (!" + this.maskString(operation.getName()));
		// print the parameter of the operation
		for (LiteralParam param : operation.getParams()) {
			bw.write(" ?" + param.getName());
			bw.flush();
		}
		bw.write(")\n ");
		// print Preconditions
		this.printMonom(bw, operation.getPrecondition(), i + 1);

		// print the delete List of the operation
		Map<CNFFormula, Monom> deleteLists = operation.getDeleteLists();
		boolean containsUnconditionalDelete = deleteLists.containsKey(new CNFFormula());
		if (deleteLists.size() > 1 || deleteLists.size() == 1 && !containsUnconditionalDelete) {
			throw new IllegalArgumentException("The operation " + operation.getName() + " contains conditional deletes, which cannot be converted to JSHOP2 format.");
		}
		Monom deleteList = containsUnconditionalDelete ? deleteLists.get(new CNFFormula()) : new Monom();
		this.printMonom(bw, deleteList, i + 1);

		// print the add List of the operation
		Map<CNFFormula, Monom> addLists = operation.getAddLists();
		boolean containsUnconditionalAdd = deleteLists.containsKey(new CNFFormula());
		Monom addList = containsUnconditionalAdd ? addLists.get(new CNFFormula()) : new Monom();
		if (addLists.size() > 1 || addLists.size() == 1 && !containsUnconditionalAdd) {
			throw new IllegalArgumentException("The operation " + operation.getName() + " contains conditional adds, which cannot be converted to JSHOP2 format.");
		}
		this.printMonom(bw, addList, i + 1);

		bw.write(this.indent(i) + ")\n\n");
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
	public void printLiteral(final BufferedWriter bw, final Literal lit) throws IOException {
		bw.write(" (");
		boolean negated = lit.isNegated();
		if (negated) {
			bw.write("not(");
		}
		bw.write(this.maskString(lit.getPropertyName()));
		for (LiteralParam param : lit.getParameters()) {
			bw.write(" ?" + this.maskString(param.getName()));
		}
		if (negated) {
			bw.write(")");
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
		bw.write(this.indent(i) + "(:method");
		this.printLiteral(bw, method.getTask());
		bw.write("\n");
		bw.write(this.indent(i + 1) + method.getName());
		bw.write("\n");

		// write the precondition into the file
		this.printMonom(bw, method.getPrecondition(), i + 1);

		// print the tasknetwork
		this.printNetwork(bw, method.getNetwork(), i + 1);

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
	private void printNetwork(final BufferedWriter bw, final TaskNetwork network, final int i) throws IOException {
		bw.write(this.indent(i) + "(");
		Literal next = network.getRoot();
		while (next != null) {
			this.printLiteral(bw, next);
			Iterator<Literal> it = network.getSuccessors(next).iterator();
			next = it.hasNext() ? it.next() : null;
		}
		bw.write(")\n");
	}

	public void printProblem(final CEOCIPSTNPlanningProblem problem, final Writer writer, final String name) throws IOException {
		BufferedWriter bw = new BufferedWriter(writer);

		// Writing of the domain file
		bw.write("(defproblem problem " + name + "\n");
		bw.write(this.indent(1) + "(\n");
		// print inital state
		this.printMonom(bw, problem.getInit(), 2, true);
		bw.write(this.indent(1) + ")\n");
		bw.write(this.indent(1) + "(\n");

		// print tasknetwork
		this.printNetwork(bw, problem.getNetwork(), 2);

		bw.write(this.indent(1) + ")\n");
		bw.write(")");
		bw.flush();
	}

	public String maskString(String str) {
		str = str.replaceAll("\\.", "_");
		str = str.replaceAll(":", "__");
		str = str.replaceAll("<", "___");
		str = str.replaceAll(">", "___");
		str = str.replaceAll("\\[\\]", "Array");
		return str;
	}
}
