package com.bornfire.AccountStatement.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ScheduleHistory_Repo extends JpaRepository<ScheduleHistory_Entity, BigDecimal> {

	@Query("SELECT h FROM ScheduleHistory_Entity h WHERE h.scheduleId = :scheduleId ORDER BY h.dateSent DESC")
	List<ScheduleHistory_Entity> findByScheduleId(@Param("scheduleId") BigDecimal scheduleId);
}