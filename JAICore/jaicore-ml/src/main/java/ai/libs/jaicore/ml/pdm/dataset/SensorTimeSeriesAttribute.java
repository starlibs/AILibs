package ai.libs.jaicore.ml.pdm.dataset;

import java.util.StringJoiner;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IObjectAttribute;

import ai.libs.jaicore.ml.core.dataset.schema.attribute.AGenericObjectAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;

public class SensorTimeSeriesAttribute extends AGenericObjectAttribute<SensorTimeSeries> implements IObjectAttribute<NumericAttribute> {

	private static final long serialVersionUID = 7375915385236514621L;

	private static final String SENSOR_TIME_SERIES_BORDER_FLAG = "\"";
	private static final String EMPTY_STRING = "";
	private static final String DATA_POINT_SEPARATOR = " ";
	private static final String SPLIT_MULTIPLE_WHITESPACES = "\\s+";
	private static final String TIMESTEP_VALUE_SEPARATOR = "#";

	// TODO: add ranges for values (min/max)

	public SensorTimeSeriesAttribute(final String name) {
		super(name);
	}

	@Override
	public boolean isValidValue(final Object value) {
		return (value instanceof SensorTimeSeries);
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[TS] " + this.getName();
	}

	@Override
	public IAttributeValue getAsAttributeValue(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof SensorTimeSeriesAttributeValue) {
				return new SensorTimeSeriesAttributeValue(this, ((SensorTimeSeriesAttributeValue) object).getValue());
			} else {
				return new SensorTimeSeriesAttributeValue(this, (SensorTimeSeries) object);
			}
		}
		throw new IllegalArgumentException("No valid value for this attribute");
	}

	@Override
	public double toDouble(final Object object) {
		throw new UnsupportedOperationException("Not yet implemented in SensorTimeSeriesAttribute");
	}

	/**
	* {@inheritDoc} Returns format: "t1:v1 t2:v2 ... tn:vn"
	*/
	@Override
	public String serializeAttributeValue(final Object value) {
		StringJoiner sj = new StringJoiner(DATA_POINT_SEPARATOR);
		SensorTimeSeries sensorTimeSeries = (SensorTimeSeries) value;
		for (int t = 0; t <= sensorTimeSeries.getLength(); t++) {
			if (sensorTimeSeries.getValueOrNull(t) != null) {
				sj.add(t + TIMESTEP_VALUE_SEPARATOR + sensorTimeSeries.getValueOrNull(t));
			}
		}
		return SENSOR_TIME_SERIES_BORDER_FLAG + sj.toString() + SENSOR_TIME_SERIES_BORDER_FLAG;
	}

	/**
	* {@inheritDoc} Given format:: "t1:v1 t2:v2 ... tn:vn"
	*/
	@Override
	public Object deserializeAttributeValue(String string) {
		string = string.replace(SENSOR_TIME_SERIES_BORDER_FLAG, EMPTY_STRING);
		String[] splittedString = string.split(SPLIT_MULTIPLE_WHITESPACES);
		SensorTimeSeries sensorTimeSeries = new SensorTimeSeries();
		for (int i = 0; i < splittedString.length; i++) {
			String[] dataPoint = splittedString[i].split(TIMESTEP_VALUE_SEPARATOR);
			sensorTimeSeries.addValue(Integer.parseInt(dataPoint[0]), Double.parseDouble(dataPoint[1]));
		}
		return sensorTimeSeries;
	}

	@Override
	protected SensorTimeSeries getValueAsTypeInstance(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof SensorTimeSeriesAttributeValue) {
				return ((SensorTimeSeriesAttributeValue) object).getValue();
			} else {
				return (SensorTimeSeries) object;
			}
		}
		throw new IllegalArgumentException("No valid value for this attribute");
	}

}
