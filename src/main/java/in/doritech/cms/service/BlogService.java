package in.doritech.cms.service;

import java.util.List;

import in.doritech.cms.request.BlogRequest;
import in.doritech.cms.util.ApiResponse;

public interface BlogService {

	ApiResponse createBlog(BlogRequest request);

	ApiResponse getBlogById(Long id);

	List<ApiResponse> getAllBlogs();

	List<ApiResponse> getBlogsByStatus(String status);

	ApiResponse updateBlog(Long id, BlogRequest request);

	ApiResponse deleteBlog(Long id);

	ApiResponse publishBlog(Long id);
}