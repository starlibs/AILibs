package jaicore.web.mcmc.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jaicore.web.mcmc.rest.message.ErrorResponse;
import jaicore.web.mcmc.rest.message.McmcRequest;
import jaicore.web.mcmc.rest.message.McmcResponse;

@RestController
@RequestMapping("/mcmc")
public class McmcService {

	@PostMapping("/modelparams")
	public ResponseEntity<McmcResponse> computeModelParams(McmcRequest request) {
		throw new UnsupportedOperationException("Not impelmented yet!");
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		ErrorResponse er = new ErrorResponse(e.getLocalizedMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(er);
	}

}
