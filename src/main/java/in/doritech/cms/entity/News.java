package in.doritech.cms.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "news_articles")
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long newsId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 1000)
    private String summary;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String status = "DRAFT";

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Employee author;

    @ManyToMany
    @JoinTable(
        name = "news_categories",
        joinColumns = @JoinColumn(name = "news_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "news_media",
        joinColumns = @JoinColumn(name = "news_id"),
        inverseJoinColumns = @JoinColumn(name = "media_id")
    )
    private Set<MediaFile> mediaFiles = new HashSet<>();

    public News() {
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getNewsId() {
        return newsId;
    }

    public void setNewsId(Long newsId) {
        this.newsId = newsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Employee getAuthor() {
        return author;
    }

    public void setAuthor(Employee author) {
        this.author = author;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Set<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(Set<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

	@Override
	public String toString() {
		return "News [newsId=" + newsId + ", title=" + title + ", slug=" + slug + ", summary=" + summary + ", content="
				+ content + ", status=" + status + ", publishedAt=" + publishedAt + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + ", author=" + author + ", categories=" + categories + ", mediaFiles="
				+ mediaFiles + "]";
	}
    
    
}