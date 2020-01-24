package ai.libs.jaicore.problems.scheduling.openshop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Title: AFS Java version 3.0</p>
 *
 * <p>Description: Algorithms for Scheduling version Java</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: Pylo.Uniandes.edu.co</p>
 *
 * <p> Class Tjob </p>
 * <p> The class Tjob provides all the attributes defined in Lekin for a job. Also provides a method to read a job from the _usr.job file.</p>
 * The attributes (members) are: <br>
 * <b> String jobId:</b> The job identifier. <br>
 * <b> int jobNumber: </b>job number (as read). <br>
 * <b> int releaseDate: </b>minimum availability date of a job. <br>
 * <b> int dueDate: </b>Due date of a job. <br>
 * <b> int weight: </b>Priority of a job. <br>
 * <b> int totalWorkTime: </b> The sum of the processing times of a job <br>
 * <b> int numberOfOperations: </b>Number of operations of a job. <br>
 * <b> Operation route[]: </b>"Routing sheet". Array with the sequence of machines which a job visits (precedence constraints).<br>
 * @author Gonzalo Mejia
 * @author Felix Mohr
 * @version 4.0
 */
/**
 * @author gmejia
 *
 */
public class Job {

	private final String jobID; // name of the job
	private final int releaseDate; // when the job arrives
	private final int dueDate; // When the job should be finished
	private final int weight; // job weight or priority
	private final List<Operation> operations = new ArrayList<>();

	Job(final String jobID, final int releaseDate, final int dueDate, final int weight) {
		super();
		this.jobID = jobID;
		this.releaseDate = releaseDate;
		this.dueDate = dueDate;
		this.weight = weight;
	}

	public String getJobID() {
		return this.jobID;
	}

	public int getReleaseDate() {
		return this.releaseDate;
	}

	public int getDueDate() {
		return this.dueDate;
	}

	public int getWeight() {
		return this.weight;
	}

	/**
	 * Package visible add operation method. Can only be invoked by the builder to make sure that the problem is not altered later.
	 *
	 * @param op
	 */
	void addOperation(final Operation op) {
		this.operations.add(op);
	}

	public List<Operation> getOperations() {
		return Collections.unmodifiableList(this.operations);
	}
} // class