package pl.dogesoulseller.thegg.api.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import pl.dogesoulseller.thegg.api.response.GenericResponse;

@Api(tags = { "Administration" })
@RestController
public class AdminController {
	@GetMapping("/api/admin")
	public RequestEntity<GenericResponse> test() {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
	}
}
