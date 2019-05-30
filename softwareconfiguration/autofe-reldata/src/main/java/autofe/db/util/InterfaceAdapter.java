package autofe.db.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
	@Override
	public JsonElement serialize(final T object, final Type interfaceType, final JsonSerializationContext context) {
		final JsonObject wrapper = new JsonObject();
		wrapper.addProperty("type", object.getClass().getName());
		wrapper.add("data", context.serialize(object));
		return wrapper;
	}

	@Override
	public T deserialize(final JsonElement elem, final Type interfaceType, final JsonDeserializationContext context) {
		final JsonObject wrapper = (JsonObject) elem;
		final JsonElement typeName = this.get(wrapper, "type");
		final JsonElement data = this.get(wrapper, "data");
		final Type actualType = this.typeForName(typeName);
		return context.deserialize(data, actualType);
	}

	private Type typeForName(final JsonElement typeElem) {
		try {
			return Class.forName(typeElem.getAsString());
		} catch (ClassNotFoundException e) {
			throw new JsonParseException(e);
		}
	}

	private JsonElement get(final JsonObject wrapper, final String memberName) {
		final JsonElement elem = wrapper.get(memberName);
		if (elem == null) {
			throw new JsonParseException("no '" + memberName + "' member found in what was expected to be an interface wrapper");
		}
		return elem;
	}
}
