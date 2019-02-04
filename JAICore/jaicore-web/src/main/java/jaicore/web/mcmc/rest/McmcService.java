package jaicore.web.mcmc.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jaicore.web.mcmc.rest.message.ErrorResponse;
import jaicore.web.mcmc.rest.message.McmcRequest;
import jaicore.web.mcmc.rest.message.McmcResponse;

@RestController
@RequestMapping("/mcmc")
public class McmcService {

	@PostMapping("/modelparams")
	public ResponseEntity<McmcResponse> computeModelParams(@RequestBody McmcRequest request) {
		McmcResponse dummyResponse = getDummyResponse();
		return ResponseEntity.ok().body(dummyResponse);
	}

	private McmcResponse getDummyResponse() {
		McmcResponse response = new McmcResponse();
		Map<String, Double> weights = new HashMap<>();
		weights.put("pow3", 1.0);
		response.setWeights(weights);

		Map<String, Double> fun1Params = new HashMap<>();
		fun1Params.put("a", 0.5);
		fun1Params.put("alpha", 0.7);
		fun1Params.put("c", 10.0);

		Map<String, Map<String, Double>> params = new HashMap<>();
		params.put("pow3", fun1Params);

		response.setParameters(params);

		return response;
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		ErrorResponse er = new ErrorResponse(e.getLocalizedMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(er);
	}

}
