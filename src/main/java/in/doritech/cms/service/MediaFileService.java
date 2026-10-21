package in.doritech.cms.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import in.doritech.cms.util.ApiResponse;

public interface MediaFileService {

	ApiResponse uploadFile(MultipartFile file);

	ApiResponse getMediaFileById(Long id);

	List<ApiResponse> getAllMediaFiles();

	ApiResponse deleteMediaFile(Long id);
}