package com.bornfire.AccountStatement.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditServicesRep extends JpaRepository<AuditServicesEntity, String> {

	@Query(value = "select * from AS_USER_AUDIT ", nativeQuery = true)
	List<AuditServicesEntity> getauditService();

	@Query("SELECT a FROM AuditServicesEntity a WHERE "
			+ "(LOWER(a.event_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(a.entry_user) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(a.func_code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(a.remarks) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(a.audit_date) LIKE LOWER(CONCAT('%', :keyword, '%'))) " + "ORDER BY a.audit_date DESC")
    Page<AuditServicesEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM AuditServicesEntity a ORDER BY a.audit_date DESC")
    Page<AuditServicesEntity> findAllByOrderByDateDesc(Pageable pageable);
}
