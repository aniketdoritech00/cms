package in.doritech.cms.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BlogRequest {

	@NotBlank(message = "Blog title is required")
	@Size(max = 255)
	private String title;

//    @NotBlank(message = "Blog slug is required")
//    private String slug;

	@Size(max = 1000)
	private String summary;

	@NotBlank(message = "Blog content is required")
	private String content;

	private Set<Long> categoryIds;

	public BlogRequest() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

//    public String getSlug() {
//        return slug;
//    }
//
//    public void setSlug(String slug) {
//        this.slug = slug;
//    }

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Set<Long> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(Set<Long> categoryIds) {
		this.categoryIds = categoryIds;
	}
}