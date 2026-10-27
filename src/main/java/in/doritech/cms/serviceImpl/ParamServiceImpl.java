package in.doritech.cms.serviceImpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import in.doritech.cms.entity.ParamEntity;
import in.doritech.cms.repository.ParamRepository;
import in.doritech.cms.service.ParamService;
import in.doritech.cms.util.ApiResponse;

@Service
public class ParamServiceImpl implements ParamService {

	@Autowired
	private ParamRepository repository;

	@Override
	public ApiResponse generateCode(String type) {
		ApiResponse response = new ApiResponse();

		try {

			if (type == null || type.trim().isEmpty()) {
				response.setMessage("Type cannot be null or empty");
				response.setStatus(400);
				return response;
			}

			Optional<ParamEntity> optional = repository
					.findTopByCodeIgnoreCaseAndSerialIgnoreCaseOrderBySerialNoDesc("code", type, "Y");

			if (optional.isEmpty()) {
				response.setMessage("Type not found or inactive: " + type);
				response.setStatus(404);
				return response;
			}

			ParamEntity param = optional.get();

			Integer currentSerial = param.getDesp2() != null ? Integer.parseInt(param.getDesp2()) : 0;

			String prefix = param.getDesp1() != null ? param.getDesp1() : "";

			String previewCode = prefix + String.format("%03d", currentSerial + 1);

			response.setMessage("Preview code fetched");
			response.setStatus(200);
			response.setPayload(previewCode);

		} catch (Exception e) {

			response.setMessage("Error while fetching preview code");
			response.setStatus(500);
			response.setPayload(e.getMessage());
		}

		return response;
	}

	@Override
	public ApiResponse updateCodeValue(String code) {

		ApiResponse response = new ApiResponse();

		try {

			if (code == null || code.trim().isEmpty()) {
				response.setMessage("Code cannot be null or empty");
				response.setStatus(400);
				return response;
			}

			String prefix = code.replaceAll("\\d", "");

			Optional<ParamEntity> optional = repository.findByDesp1IgnoreCase(prefix);

			if (optional.isEmpty()) {
				response.setMessage("Code not found or inactive: " + prefix);
				response.setStatus(404);
				return response;
			}

			ParamEntity param = optional.get();

			Integer current = param.getDesp2() != null ? Integer.parseInt(param.getDesp2()) : 0;

			Integer updated = current + 1;

			param.setDesp2(String.valueOf(updated));

			repository.save(param);

			response.setMessage("Code updated successfully");
			response.setStatus(HttpStatus.OK.value());
			response.setPayload(prefix + String.format("%03d", updated));

		} catch (NumberFormatException e) {

			response.setMessage("Invalid number format in desp2");
			response.setStatus(500);
			response.setPayload(null);
		} catch (Exception e) {

			response.setMessage("Error while updating code");
			response.setStatus(500);
			response.setPayload(e.getMessage());
		}

		return response;
	}

}