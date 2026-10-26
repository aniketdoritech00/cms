package in.doritech.cms.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import in.doritech.cms.util.ApiResponse;

public interface MediaFileService {

	ApiResponse uploadFile(MultipartFile file);

	ApiResponse getMediaFileById(Long id);

	ApiResponse getAllMediaFiles();

	ApiResponse deleteMediaFile(Long id);

	ResponseEntity<byte[]> getImageById(Long id);
}