package com.bornfire.AccountStatement.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ScheduleHistory_Repo extends JpaRepository<ScheduleHistory_Entity, BigDecimal> {

	@Query("SELECT h FROM ScheduleHistory_Entity h WHERE h.scheduleId = :scheduleId ORDER BY h.dateSent DESC")
	List<ScheduleHistory_Entity> findByScheduleId(@Param("scheduleId") BigDecimal scheduleId);
	
	List<ScheduleHistory_Entity> findByDeliveryStatus(String deliveryStatus);
	
	@Query("SELECT h FROM ScheduleHistory_Entity h WHERE h.deliveryStatus = 'Failed' ORDER BY h.dateSent DESC")
    List<ScheduleHistory_Entity> findAllFailedDeliveries();

    @Query("SELECT COUNT(h) FROM ScheduleHistory_Entity h WHERE h.deliveryStatus = ?1 AND (h.isRerun = 'N' OR h.isRerun IS NULL ) AND h.dateSent LIKE %?2")
    long countByStatusAndMonth(String status, String monthYearSuffix);
    
    @Query("SELECT MAX(h.id) FROM ScheduleHistory_Entity h")
    BigDecimal findMaxId();
    
    @Query("SELECT h FROM ScheduleHistory_Entity h WHERE h.deliveryStatus = ?1 AND h.dateSent LIKE %?2")
    List<ScheduleHistory_Entity> findByStatusAndMonth(String status, String monthPattern);
    
}