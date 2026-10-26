package in.doritech.cms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.doritech.cms.service.BlogService;
import in.doritech.cms.service.MediaFileService;
import in.doritech.cms.service.NewsService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/publicNewsBlog")
@CrossOrigin(allowedHeaders = "*")
public class PublicNewsBlogController {

	private final BlogService blogService;
	private final NewsService newsService;
	private final MediaFileService mediaFileService;

	public PublicNewsBlogController(BlogService blogService, NewsService newsService,
			MediaFileService mediaFileService) {
		this.blogService = blogService;
		this.newsService = newsService;
		this.mediaFileService = mediaFileService;
	}

	@GetMapping("/getAllBlogsWithPagination")
	public ApiResponse getAllBlogsWithPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return blogService.getAllBlogsWithPagination(page, size);
	}

	@GetMapping("/getAllNewsWithPagination")
	public ApiResponse getAllNewsWithPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return newsService.getAllNewsWithPagination(page, size);
	}

	@GetMapping("/getNewsById/{id}")
	public ApiResponse getNewsById(@PathVariable Long id) {
		return newsService.getNewsById(id);
	}

	@GetMapping("/getBlogById/{id}")
	public ApiResponse getBlogById(@PathVariable Long id) {
		return blogService.getBlogById(id);
	}

	@GetMapping("/getImageById/{id}")
	public ResponseEntity<byte[]> getImageById(@PathVariable Long id) {
		return mediaFileService.getImageById(id);
	}

}
