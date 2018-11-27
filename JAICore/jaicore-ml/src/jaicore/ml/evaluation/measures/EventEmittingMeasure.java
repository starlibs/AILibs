package jaicore.ml.evaluation.measures;

import java.util.List;

import com.google.common.eventbus.EventBus;

import jaicore.basic.aggregate.IAggregateFunction;
import jaicore.ml.evaluation.MeasureAggregatedComputationEvent;
import jaicore.ml.evaluation.MeasureListComputationEvent;
import jaicore.ml.evaluation.MeasureSingleComputationEvent;

public class EventEmittingMeasure<INPUT,OUTPUT> implements IMeasure<INPUT,OUTPUT> {
	
	private final IMeasure<INPUT,OUTPUT> baseMeasure;
	private final EventBus measurementEventBus = new EventBus();
	public EventEmittingMeasure(IMeasure<INPUT, OUTPUT> baseMeasure) {
		super();
		this.baseMeasure = baseMeasure;
	}
	
	@Override
	public OUTPUT calculateMeasure(INPUT actual, INPUT expected) {
		OUTPUT o = baseMeasure.calculateMeasure(actual, expected);
		measurementEventBus.post(new MeasureSingleComputationEvent<>(actual, expected, o));
		return o;
	}
	@Override
	public List<OUTPUT> calculateMeasure(List<INPUT> actual, List<INPUT> expected) {
		List<OUTPUT> o = baseMeasure.calculateMeasure(actual, expected);
		measurementEventBus.post(new MeasureListComputationEvent<>(actual, expected, o));
		return o;
	}
	@Override
	public OUTPUT calculateMeasure(List<INPUT> actual, List<INPUT> expected,
			IAggregateFunction<OUTPUT> aggregateFunction) {
		OUTPUT o = baseMeasure.calculateMeasure(actual, expected, aggregateFunction);
		measurementEventBus.post(new MeasureAggregatedComputationEvent<>(actual, expected, aggregateFunction, o));
		return o;
	}

	@Override
	public OUTPUT calculateAvgMeasure(List<INPUT> actual, List<INPUT> expected) {
		// TODO Auto-generated method stub
		return null;
	}
}
