package com.bornfire.AccountStatement.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ScheduleStatement_Repo extends JpaRepository<ScheduledStatement_Entity, BigDecimal> {
	
	@Query(value = "select NVL(max(id),0)+1 from SCHEDULE_STATEMENT", nativeQuery = true)
	BigDecimal getid();
	
	@Query(value = "select NVL(max(SCHEDULE_ID),0)+1 from SCHEDULE_STATEMENT", nativeQuery = true)
	BigDecimal getSCHEDULEID();
	
	@Query(value = "SELECT * FROM SCHEDULE_STATEMENT A WHERE A.ID = (SELECT MIN(B.ID) FROM SCHEDULE_STATEMENT B WHERE B.SCHEDULE_ID = A.SCHEDULE_ID )", nativeQuery = true)
	List<ScheduledStatement_Entity> getScheduledata() ;

	
}