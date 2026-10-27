package in.doritech.cms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.doritech.cms.service.ParamService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/param")
public class ParamController {

	@Autowired
	private ParamService service;

	@GetMapping("/generate-code")
	public ApiResponse generateCode(@RequestParam String type) {
		return service.generateCode(type);
	}

	@PutMapping("/update-code")
	public ApiResponse updateCode(@RequestParam String code) {
		return service.updateCodeValue(code);
	}
}