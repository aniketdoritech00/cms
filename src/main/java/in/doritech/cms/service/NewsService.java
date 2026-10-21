package in.doritech.cms.service;

import java.util.List;

import in.doritech.cms.request.NewsRequest;
import in.doritech.cms.util.ApiResponse;

public interface NewsService {

	ApiResponse createNews(NewsRequest request);

	ApiResponse getNewsById(Long id);

	List<ApiResponse> getAllNews();

	List<ApiResponse> getNewsByStatus(String status);

	ApiResponse updateNews(Long id, NewsRequest request);

	ApiResponse deleteNews(Long id);

	ApiResponse publishNews(Long id);
}