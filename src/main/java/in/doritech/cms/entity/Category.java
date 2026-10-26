package in.doritech.cms.entity;

import jakarta.persistence.*;

	@Entity
	@Table(name = "categories")
	public class Category {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long categoryId;
	
	    @Column(nullable = false, unique = true)
	    private String categoryName;
	
	    @Column(nullable = false, unique = true)
	    private String slug;
	
	    private String description;

    public Category() {
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}