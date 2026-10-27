package in.doritech.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.ParamEntity;

@Repository
public interface ParamRepository extends JpaRepository<ParamEntity, Integer> {

	List<ParamEntity> findByCode(String code);

	void deleteByCode(String code);

	Optional<ParamEntity> findByDesp2IgnoreCase(String desp2);

	List<ParamEntity> findByCodeIgnoreCaseAndSerialIgnoreCaseAndDesp3IgnoreCase(String code, String serial,
			String active);

	List<ParamEntity> findByCodeIgnoreCaseAndSerialIgnoreCaseAndDesp3IgnoreCase(String code, String serial,
			String desp3, Sort sort);

	boolean existsByCode(String code);

	Optional<ParamEntity> findTopByCodeIgnoreCaseAndSerialIgnoreCaseOrderBySerialNoDesc(String code, String serial,
			String desp3);

	Optional<ParamEntity> findByDesp1IgnoreCase(String prefix);

	Optional<ParamEntity> findByCodeAndSerial(@Param("code") String code, @Param("serial") String serial);

}