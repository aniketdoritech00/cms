
package in.doritech.cms.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.doritech.cms.request.NewsRequest;
import in.doritech.cms.service.NewsService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/news")
public class NewsController {

	private final NewsService newsService;

	public NewsController(NewsService newsService) {
		this.newsService = newsService;
	}

	@PostMapping("/createNews")
	public ApiResponse createNews(@RequestBody NewsRequest request) {
		return newsService.createNews(request);
	}

	@PostMapping(value = "/createNewsWithImages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse createNews(@ModelAttribute NewsRequest request,
			@RequestParam("images") List<MultipartFile> images) {
		return newsService.createNewsWithImages(request, images);
	}

	@GetMapping("/getNewsById/{id}")
	public ApiResponse getNewsById(@PathVariable Long id) {
		return newsService.getNewsById(id);
	}

	@GetMapping("/getAllNews")
	public ApiResponse getAllNews() {
		return newsService.getAllNews();
	}

	@GetMapping("/getAllNewsWithPagination")
	public ApiResponse getAllNewsWithPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return newsService.getAllNewsWithPagination(page, size);
	}

	@PutMapping("/updateNews/{id}")
	public ApiResponse updateNews(@PathVariable Long id, @RequestBody NewsRequest request) {
		return newsService.updateNews(id, request);
	}

	@DeleteMapping("/deleteNews/{id}")
	public ApiResponse deleteNews(@PathVariable Long id) {
		return newsService.deleteNews(id);
	}
}
