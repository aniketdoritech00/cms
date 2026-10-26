package in.doritech.cms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.Role;

	
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

	boolean existsByRoleNameAndRoleIdNot(String roleName, Long id);
}