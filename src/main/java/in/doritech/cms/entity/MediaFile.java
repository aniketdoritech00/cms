package in.doritech.cms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_files")
public class MediaFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mediaId;

	@Column(nullable = false)
	private String originalFilename;

	@Column(nullable = false, unique = true)
	private String storedFilename;

	@Column(nullable = false)
	private String filePath;

	private String contentType;

	private Long fileSize;

	private LocalDateTime uploadedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uploaded_by")
	private Employee uploadedBy;

	public MediaFile() {
	}

	@PrePersist
	public void onCreate() {
		uploadedAt = LocalDateTime.now();
	}

	public Long getMediaId() {
		return mediaId;
	}

	public void setMediaId(Long mediaId) {
		this.mediaId = mediaId;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getStoredFilename() {
		return storedFilename;
	}

	public void setStoredFilename(String storedFilename) {
		this.storedFilename = storedFilename;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public Employee getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(Employee uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

}