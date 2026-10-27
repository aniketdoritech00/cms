package in.doritech.cms.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import in.doritech.cms.request.NewsRequest;
import in.doritech.cms.util.ApiResponse;

public interface NewsService {

	ApiResponse createNews(NewsRequest request);

	ApiResponse getNewsById(Long id);

	ApiResponse getAllNews();

	ApiResponse getNewsByStatus(String status);

	ApiResponse updateNews(Long id, NewsRequest request);

	ApiResponse deleteNews(Long id);

	ApiResponse publishNews(Long id);

	ApiResponse getAllNewsWithPagination(int page, int size);

	ApiResponse createNewsWithImages(NewsRequest request, List<MultipartFile> images);

	ApiResponse updateNewsWithImages(Long id, NewsRequest request, List<MultipartFile> images);

	ApiResponse publishNews(List<Long> ids);
}
