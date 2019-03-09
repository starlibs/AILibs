package de.upb.crc901.mlplan.examples.multilabel.meka;

import java.sql.SQLException;
import java.util.HashMap;

import com.google.common.eventbus.Subscribe;

/**
 * Uploads received solution events.
 * 
 * @author Helena Graf
 *
 */
public class SolutionUploader {

	/**
	 * Connection which to use to upload intermediate solution events
	 */
	private ResultsDBConnection connection;

	/**
	 * The time at which the experiment was started
	 */
	private long startTime;

	/**
	 * The current generation of the genetic algorithm
	 */
	private int currentGeneration = -1;

	/**
	 * Construct a new Solution upload that uploads received
	 * {@link IntermediateSolutionEvent}s using the given connection.
	 * 
	 * @param connection
	 *            the connection to use to upload events
	 * @param startTime
	 *            the time at which the experiment was started
	 */
	public SolutionUploader(ResultsDBConnection connection, long startTime) {
		this.connection = connection;
		this.startTime = startTime;
	}

	@Subscribe
	public void receiveIntermediateSolutionEvent(IntermediateSolutionEvent e) {

		try {
			connection.addIntermediateMeasurement(e.getSolution(), e.getValue(),
					System.currentTimeMillis() - startTime, currentGeneration);
		} catch (SQLException e1) {
			System.err.println("Could not upload intermediate solution:\n" + e + "\ndue to exception\n" + e1);
		}
	}
}
