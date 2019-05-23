package hasco.serialization;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import hasco.model.Dependency;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;
import jaicore.basic.sets.SetUtil.Pair;

public class ParameterDomainDeserializer extends StdDeserializer<Dependency> {

	public ParameterDomainDeserializer() {
		this(null);
	}

	public ParameterDomainDeserializer(Class<ParameterDomain> vc) {
		super(vc);
	}

	@Override
	public Dependency deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		List<Pair<Parameter, ParameterDomain>> collection1 = new LinkedList<Pair<Parameter, ParameterDomain>>();
		LinkedList<Collection<Pair<Parameter, ParameterDomain>>> collection2 = new LinkedList<Collection<Pair<Parameter, ParameterDomain>>>();
		return new Dependency(collection2, collection1);
	}

}
