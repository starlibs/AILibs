package jaicore.ml.core.evaluation.measure;

import java.util.List;

import com.google.common.eventbus.EventBus;

import jaicore.basic.aggregate.IAggregateFunction;
import jaicore.ml.evaluation.MeasureAggregatedComputationEvent;
import jaicore.ml.evaluation.MeasureListComputationEvent;
import jaicore.ml.evaluation.MeasureSingleComputationEvent;

public class EventEmittingMeasure<INPUT, OUTPUT> implements IMeasure<INPUT, OUTPUT> {

	private final IMeasure<INPUT, OUTPUT> baseMeasure;
	private final EventBus measurementEventBus = new EventBus();

	public EventEmittingMeasure(final IMeasure<INPUT, OUTPUT> baseMeasure) {
		super();
		this.baseMeasure = baseMeasure;
	}

	@Override
	public OUTPUT calculateMeasure(final INPUT actual, final INPUT expected) {
		OUTPUT o = this.baseMeasure.calculateMeasure(actual, expected);
		this.measurementEventBus.post(new MeasureSingleComputationEvent<>(actual, expected, o));
		return o;
	}

	@Override
	public List<OUTPUT> calculateMeasure(final List<INPUT> actual, final List<INPUT> expected) {
		List<OUTPUT> o = this.baseMeasure.calculateMeasure(actual, expected);
		this.measurementEventBus.post(new MeasureListComputationEvent<>(actual, expected, o));
		return o;
	}

	@Override
	public OUTPUT calculateMeasure(final List<INPUT> actual, final List<INPUT> expected, final IAggregateFunction<OUTPUT> aggregateFunction) {
		OUTPUT o = this.baseMeasure.calculateMeasure(actual, expected, aggregateFunction);
		this.measurementEventBus.post(new MeasureAggregatedComputationEvent<>(actual, expected, aggregateFunction, o));
		return o;
	}

	@Override
	public OUTPUT calculateAvgMeasure(final List<INPUT> actual, final List<INPUT> expected) {
		throw new UnsupportedOperationException("This method is not implemented for this measure.");
	}
}
