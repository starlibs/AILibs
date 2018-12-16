package jaicore.basic.algorithm;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;

public abstract class AAlgorithm<I, O> implements IAlgorithm<I, O>, ILoggingCustomizable {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AAlgorithm.class);
	private String loggerName;

	/* Parameters of the algorithm. */
	private IAlgorithmConfig config;

	/* Semantic input to the algorithm. */
	private I input;

	/* State and event bus for sending algorithm events. */
	private boolean canceled;
	private AlgorithmState state = AlgorithmState.created;
	private final EventBus eventBus = new EventBus();

	/**
	 * Standard c'tor without any parameters.
	 */
	protected AAlgorithm() {
		super();
		this.config = ConfigFactory.create(IAlgorithmConfig.class);
	}

	/**
	 * C'tor providing the input for the algorithm already.
	 *
	 * @param input
	 *            The input for the algorithm.
	 */
	protected AAlgorithm(final I input) {
		this();
		this.input = input;
	}

	/**
	 * Internal c'tor overwriting the internal config to keep the config consistent.
	 *
	 * @param config
	 *            The config to take as the internal config object.
	 */
	protected AAlgorithm(final IAlgorithmConfig config) {
		super();
		this.config = config;
	}

	/**
	 * Internal c'tore overwriting the internal configuration and setting the input.
	 *
	 * @param input
	 *            The input for the algorithm.
	 * @param config
	 *            The configuration to take as the internal configuration object.
	 */
	protected AAlgorithm(final IAlgorithmConfig config, final I input) {
		this(config);
		this.input = input;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return this.state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return this.nextWithException();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setInput(final I input) {
		this.input = input;
	}

	@Override
	public I getInput() {
		return this.input;
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
	}

	@Override
	public int getNumCPUs() {
		return this.getConfig().cpus();
	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		this.getConfig().setProperty(IAlgorithmConfig.K_CPUS, numberOfCPUs + "");
	}

	@Override
	public void setTimeout(final long timeout, final TimeUnit timeUnit) {
		this.getConfig().setProperty(IAlgorithmConfig.K_TIMEOUT, new TimeOut(timeout, timeUnit).milliseconds() + "");
	}

	@Override
	public void setTimeout(final TimeOut timeout) {
		this.getConfig().setProperty(IAlgorithmConfig.K_TIMEOUT, timeout.milliseconds() + "");
	}

	@Override
	public TimeOut getTimeout() {
		return new TimeOut(this.getConfig().timeout(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @return Flag denoting whether this algorithm has been canceled.
	 */
	public boolean isCanceled() {
		return this.canceled;
	}

	/**
	 * @return The current state of the algorithm.
	 */
	public AlgorithmState getState() {
		return this.state;
	}

	/**
	 * @param state
	 *            The new state of the algorithm.
	 */
	protected void setState(final AlgorithmState state) {
		this.state = state;
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

	/**
	 * @param canceled
	 *            Sets the canceled flag to the new canceled value.
	 */
	public void setCanceled(final boolean canceled) {
		this.canceled = canceled;
	}

	/**
	 * This methods terminates the algorithm, setting the internal state to inactive and emitting the obligatory AlgorithmFinishedEvent over the event bus.
	 *
	 * @return The algorithm finished event.
	 */
	protected AlgorithmFinishedEvent terminate() {
		this.state = AlgorithmState.inactive;
		AlgorithmFinishedEvent finishedEvent = new AlgorithmFinishedEvent();
		this.eventBus.post(finishedEvent);
		return finishedEvent;
	}

	/**
	 * This methods allows for posting an event on the algorithm's event bus.
	 *
	 * @param e
	 *            The event to post on the event bus.
	 */
	protected void post(final Object e) {
		this.eventBus.post(e);
	}

	@Override
	public IAlgorithmConfig getConfig() {
		return this.config;
	}

	/**
	 * Sets the config object to the new config object.
	 *
	 * @param config
	 *            The new config object.
	 */
	public void setConfig(final IAlgorithmConfig config) {
		this.config = config;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger to {}", name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched to logger {}", name);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}
}
