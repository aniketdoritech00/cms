package in.doritech.cms.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import in.doritech.cms.request.BlogRequest;
import in.doritech.cms.util.ApiResponse;

public interface BlogService {

	ApiResponse createBlog(BlogRequest request);

	ApiResponse getBlogById(Long id);

	ApiResponse getAllBlogs();

	ApiResponse getBlogsByStatus(String status);

	ApiResponse updateBlog(Long id, BlogRequest request);

	ApiResponse deleteBlog(Long id);

	ApiResponse publishBlog(Long id);

	ApiResponse getAllBlogsWithPagination(int page, int size);

	ApiResponse createBlogWithImages(BlogRequest request, List<MultipartFile> images);

	ApiResponse updateBlogWithImages(Long id, BlogRequest request, List<MultipartFile> images);
}