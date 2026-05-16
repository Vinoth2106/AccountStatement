package com.bornfire.AccountStatement.entities;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;



public interface AuditServicesRep extends JpaRepository<AuditServicesEntity , BigDecimal> {
	
	@Query(value = "select * from USER_AUDIT ", nativeQuery = true)
	List<AuditServicesEntity> getauditService();

}
