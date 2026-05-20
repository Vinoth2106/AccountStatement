package com.bornfire.AccountStatement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bornfire.AccountStatement.entities.ScheduleHistory_Entity;
import com.bornfire.AccountStatement.entities.ScheduleHistory_Repo;
import com.bornfire.AccountStatement.entities.ScheduleStatement_Repo;
import com.bornfire.AccountStatement.entities.ScheduledStatement_Entity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleStatementService {

	@Autowired
	ScheduleStatement_Repo scheduleRepo;

	@Autowired
	ScheduleHistory_Repo historyRepo;

	private BigDecimal generateNextId() {
		List<ScheduledStatement_Entity> all = scheduleRepo.findAll();
		if (all == null || all.isEmpty()) {
			return BigDecimal.ONE;
		}
		BigDecimal max = BigDecimal.ZERO;
		for (ScheduledStatement_Entity s : all) {
			if (s.getId() != null && s.getId().compareTo(max) > 0) {
				max = s.getId();
			}
		}
		return max.add(BigDecimal.ONE);
	}

	public List<ScheduledStatement_Entity> getAllSchedules() {
		return scheduleRepo.findAll();
	}

	public Map<String, Object> saveSchedule(ScheduledStatement_Entity schedule) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			schedule.setId(generateNextId());
			scheduleRepo.save(schedule);
			result.put("status", "success");
			result.put("id", schedule.getId());
		} catch (Exception e) {
			result.put("status", "error");
			result.put("message", e.getMessage());
		}
		return result;
	}

	public List<ScheduleHistory_Entity> getHistory(BigDecimal scheduleId) {
		return historyRepo.findByScheduleId(scheduleId);
	}

}