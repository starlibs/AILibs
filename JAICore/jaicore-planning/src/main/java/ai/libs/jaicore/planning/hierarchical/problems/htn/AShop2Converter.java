package ai.libs.jaicore.planning.hierarchical.problems.htn;

import java.io.BufferedWriter;
import java.io.IOException;

import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;

public abstract class AShop2Converter {

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
	public void printMonom(final BufferedWriter bw, final Monom monom, final int i) throws IOException {
		this.printMonom(bw, monom, i, false);
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
	public void printMonom(final BufferedWriter bw, final Monom monom, final int i, final boolean newline) throws IOException {
		bw.write(this.indent(i) + "(");
		for (Literal lit : monom) {
			this.printLiteral(bw, lit);
			if (newline) {
				bw.write("\n" + this.indent(i) + " ");
			}
		}

		bw.write(")\n");
		bw.flush();
	}

	public String indent(final int numberOfIntends) {
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < numberOfIntends; i++) {
			r.append("\t");
		}
		return r.toString();
	}

	public abstract void printLiteral(final BufferedWriter bw, final Literal lit) throws IOException;
}
