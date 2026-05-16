package com.bornfire.AccountStatement.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;



public interface UserAuditRepo extends JpaRepository<UserAuditLevel_Entity, String> {

	@Query(value = "SELECT * FROM USER_AUDIT_LEVEL ORDER BY AUDIT_DATE DESC", nativeQuery = true)
    List<UserAuditLevel_Entity> getUserAuditList();
}
