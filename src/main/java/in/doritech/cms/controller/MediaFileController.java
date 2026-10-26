package in.doritech.cms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.doritech.cms.service.MediaFileService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/media")
@CrossOrigin(allowedHeaders = "*")
public class MediaFileController {

	private final MediaFileService mediaFileService;

	public MediaFileController(MediaFileService mediaFileService) {
		this.mediaFileService = mediaFileService;
	}

	@PostMapping("/upload")
	public ApiResponse uploadFile(@RequestParam MultipartFile file) {
		return mediaFileService.uploadFile(file);
	}

	@GetMapping("getMediaFileById/{id}")
	public ApiResponse getMediaFileById(@PathVariable Long id) {
		return mediaFileService.getMediaFileById(id);
	}

	@GetMapping("/getAllMediaFiles")
	public ApiResponse getAllMediaFiles() {
		return mediaFileService.getAllMediaFiles();
	}

	@DeleteMapping("deleteMediaFile/{id}")
	public ApiResponse deleteMediaFile(@PathVariable Long id) {
		return mediaFileService.deleteMediaFile(id);
	}

	@GetMapping("/getImageById/{id}")
	public ResponseEntity<byte[]> getImageById(@PathVariable Long id) {
		return mediaFileService.getImageById(id);
	}
}