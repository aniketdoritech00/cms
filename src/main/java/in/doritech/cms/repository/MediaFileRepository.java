package in.doritech.cms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.MediaFile;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

	Optional<MediaFile> findByStoredFilename(String storedFilename);

	boolean existsByStoredFilename(String storedFilename);
}