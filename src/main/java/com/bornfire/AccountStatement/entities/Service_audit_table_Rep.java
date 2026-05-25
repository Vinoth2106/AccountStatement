package com.bornfire.AccountStatement.entities;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface Service_audit_table_Rep extends JpaRepository<Service_audit_table_entity, String> {

	@Query(value = "select * from AS_SERVICE_AUDIT_TABLE", nativeQuery = true)
	List<Service_audit_table_entity> getauditListLocalvalues();

	@Query(value = "select * from AS_SERVICE_AUDIT_TABLE where AUDIT_TABLE = 'Kyc_corporate'", nativeQuery = true)
	List<Service_audit_table_entity> getauditListLocalvalues1();

	@Query(value = "select * from AS_SERVICE_AUDIT_TABLE where TRUNC(AUDIT_DATE) = ?1", nativeQuery = true)
	List<Service_audit_table_entity> getauditListOpeartion(Date audit_date);

	@Query("SELECT a FROM Service_audit_table_entity a WHERE "
			+ "(LOWER(a.audit_ref_no) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(a.audit_table) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(a.func_code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(a.entry_user) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(a.entry_time) LIKE LOWER(CONCAT('%', :keyword, '%'))) " + "ORDER BY a.entry_time DESC")
	Page<Service_audit_table_entity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT a FROM Service_audit_table_entity a ORDER BY a.entry_time DESC")
	Page<Service_audit_table_entity> findAllByOrderByDateDesc(Pageable pageable);

	/*
	 * @Query(value = "SELECT BTDES_AUDIT_SEQ.NEXTVAL FROM dual", nativeQuery =
	 * true) Long getAuditRefUUID();
	 */

	@Query(value = "SELECT * FROM AS_SERVICE_AUDIT_TABLE ORDER BY ENTRY_TIME DESC FETCH FIRST 4 ROWS ONLY", nativeQuery = true)
	List<Service_audit_table_entity> findTop4RecentActivities();
    
}
