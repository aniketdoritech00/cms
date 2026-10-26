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

import in.doritech.cms.request.BlogRequest;
import in.doritech.cms.service.BlogService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/blogs")
public class BlogController {

	private final BlogService blogService;

	public BlogController(BlogService blogService) {
		this.blogService = blogService;
	}

	@PostMapping("/createBlog")
	public ApiResponse createBlog(@RequestBody BlogRequest request) {
		return blogService.createBlog(request);
	}

	@PostMapping(value = "/createBlogWithImages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse createBlog(@ModelAttribute BlogRequest request,
			@RequestParam("images") List<MultipartFile> images) {
		return blogService.createBlogWithImages(request, images);
	}

	@GetMapping("/getBlogById/{id}")
	public ApiResponse getBlogById(@PathVariable Long id) {
		return blogService.getBlogById(id);
	}

	@GetMapping("/getAllBlogs")
	public ApiResponse getAllBlogs() {
		return blogService.getAllBlogs();
	}

	@GetMapping("/getAllBlogsWithPagination")
	public ApiResponse getAllBlogsWithPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return blogService.getAllBlogsWithPagination(page, size);
	}

	@PutMapping("/updateBlog/{id}")
	public ApiResponse updateBlog(@PathVariable Long id, @RequestBody BlogRequest request) {
		return blogService.updateBlog(id, request);
	}

	@DeleteMapping("/deleteBlog/{id}")
	public ApiResponse deleteBlog(@PathVariable Long id) {
		return blogService.deleteBlog(id);
	}
}