package in.doritech.cms.service;

import in.doritech.cms.util.ApiResponse;

public interface ParamService {

	ApiResponse generateCode(String type);

	ApiResponse updateCodeValue(String code);
}