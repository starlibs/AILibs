package ai.libs.jaicore.problems.scheduling.openshop;

/**
 * @author Gonzalo Mejia
 * @author Felix Mohr
 * @version 4.0
 */
public class Machine {

	private final String machineID; // unique identifier of the machine
	private final int availableDate; // defines the date of when the machine becomes available
	private final int initialState; // defines the initial status of the machine. Must be a capital letter (A, B, C, etc)<br>
	private final Workcenter workcenter; // the work center the machine is located in

	/**
	 * Package constructor.
	 *
	 * @param machineID
	 * @param availableDate
	 * @param initialState
	 * @param workcenter
	 */
	Machine(final String machineID, final int availableDate, final int initialState, final Workcenter workcenter) {
		super();
		this.machineID = machineID;
		this.availableDate = availableDate;
		this.initialState = initialState;
		this.workcenter = workcenter;
		workcenter.addMachine(this);
	}

	public String getMachineID() {
		return this.machineID;
	}

	public int getAvailableDate() {
		return this.availableDate;
	}

	public int getInitialState() {
		return this.initialState;
	}

	public Workcenter getWorkcenter() {
		return this.workcenter;
	}
}
