package ai.libs.hyperopt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.hasco.model.Component;
import ai.libs.jaicore.logging.ToJSONStringUtil;

@JsonPropertyOrder({ "repository", "components" })
public class HASCORepository {
	private static final Logger L = LoggerFactory.getLogger(HASCORepository.class);
	private String repository = new String();
	List<Component> components = new ArrayList<>();

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			L.error(e.getMessage());
		}

		Map<String, Object> fields = new HashMap<>();
		fields.put("repository", this.repository);
		fields.put("components", this.components);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
	
}
