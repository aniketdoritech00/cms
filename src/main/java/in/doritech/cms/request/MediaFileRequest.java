package in.doritech.cms.request;

import jakarta.validation.constraints.NotBlank;

public class MediaFileRequest {

    @NotBlank(message = "Original filename is required")
    private String originalFilename;

    private String contentType;

    public MediaFileRequest() {
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}