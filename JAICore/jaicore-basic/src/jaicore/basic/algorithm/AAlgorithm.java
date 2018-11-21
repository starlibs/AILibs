package jaicore.basic.algorithm;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;

public abstract class AAlgorithm<I,O> implements IAlgorithm<I, O>, ILoggingCustomizable {
	
	private final EventBus eventBus = new EventBus();
	
	private I input;
	private TimeOut timeout;
	private int numCPUs = -1;
	private AlgorithmState state = AlgorithmState.created;
	private boolean canceled;
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public AAlgorithm() {}
	
	public AAlgorithm(I input) {
		super();
		this.input = input;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return nextWithException();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setInput(I input) {
		this.input = input;
	}

	@Override
	public I getInput() {
		return input;
	}

	@Override
	public void registerListener(Object listener) {
		eventBus.register(listener);
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		this.numCPUs = numberOfCPUs;
	}

	@Override
	public int getNumCPUs() {
		return numCPUs;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		setTimeout(new TimeOut(timeout, timeUnit));
	}

	@Override
	public void setTimeout(TimeOut timeout) {
		this.timeout = timeout;
	}

	@Override
	public TimeOut getTimeout() {
		return timeout;
	}

	public boolean isCanceled() {
		return canceled;
	}
	
	public AlgorithmState getState() {
		return state;
	}

	protected void setState(AlgorithmState state) {
		this.state = state;
	}

	@Override
	public void setLoggerName(String name) {
		logger.info("Switching logger to {}", name);
		loggerName = name;
		logger = LoggerFactory.getLogger(name);
		logger.info("Switched to logger {}", name);
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}
	
	protected Logger getLogger() {
		return logger;
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}
	
	protected AlgorithmFinishedEvent terminate() {
		this.state = AlgorithmState.inactive;
		AlgorithmFinishedEvent finishedEvent = new AlgorithmFinishedEvent();
		this.eventBus.post(finishedEvent);
		return finishedEvent;
	}
	
	protected void post(Object e) {
		eventBus.post(e);
	}
}
