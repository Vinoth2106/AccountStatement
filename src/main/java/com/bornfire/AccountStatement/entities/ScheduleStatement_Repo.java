package com.bornfire.AccountStatement.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;

public interface ScheduleStatement_Repo extends JpaRepository<ScheduledStatement_Entity, BigDecimal> {
}