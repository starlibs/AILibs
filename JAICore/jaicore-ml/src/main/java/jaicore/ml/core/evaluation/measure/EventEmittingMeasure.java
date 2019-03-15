package jaicore.ml.core.evaluation.measure;

import java.util.List;

import com.google.common.eventbus.EventBus;

import jaicore.basic.aggregate.IAggregateFunction;
import jaicore.ml.evaluation.MeasureAggregatedComputationEvent;
import jaicore.ml.evaluation.MeasureListComputationEvent;
import jaicore.ml.evaluation.MeasureSingleComputationEvent;

/**
 * A wrapper for emitting an event once the measure is computed for a specific input.
 *
 * @author mwever
 *
 * @param <I> The type of the inputs.
 * @param <O> The type of the measured values.
 */
public class EventEmittingMeasure<I, O> implements IMeasure<I, O> {

	/* Wrapped measure */
	private final IMeasure<I, O> baseMeasure;

	/* Event Bus to register listeners on and to which events of measurement results are posted. */
	private final EventBus measurementEventBus = new EventBus();

	/**
	 * Constructor wrapping a given measure into an EventEmittingMeasure.
	 *
	 * @param baseMeasure The measure to be wrapped.
	 */
	public EventEmittingMeasure(final IMeasure<I, O> baseMeasure) {
		super();
		this.baseMeasure = baseMeasure;
	}

	@Override
	public O calculateMeasure(final I actual, final I expected) {
		O o = this.baseMeasure.calculateMeasure(actual, expected);
		this.measurementEventBus.post(new MeasureSingleComputationEvent<>(actual, expected, o));
		return o;
	}

	@Override
	public List<O> calculateMeasure(final List<I> actual, final List<I> expected) {
		List<O> o = this.baseMeasure.calculateMeasure(actual, expected);
		this.measurementEventBus.post(new MeasureListComputationEvent<>(actual, expected, o));
		return o;
	}

	@Override
	public O calculateMeasure(final List<I> actual, final List<I> expected, final IAggregateFunction<O> aggregateFunction) {
		O o = this.baseMeasure.calculateMeasure(actual, expected, aggregateFunction);
		this.measurementEventBus.post(new MeasureAggregatedComputationEvent<>(actual, expected, aggregateFunction, o));
		return o;
	}

	@Override
	public O calculateAvgMeasure(final List<I> actual, final List<I> expected) {
		throw new UnsupportedOperationException("This method is not implemented for this measure.");
	}
}
