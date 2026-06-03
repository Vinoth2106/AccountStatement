package com.bornfire.AccountStatement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.AccountStatement.entities.AccountDTO;
import com.bornfire.AccountStatement.entities.ScheduleHistory_Entity;
import com.bornfire.AccountStatement.entities.ScheduleHistory_Repo;
import com.bornfire.AccountStatement.entities.ScheduleStatement_Repo;
import com.bornfire.AccountStatement.entities.ScheduledStatement_Entity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
public class ScheduleStatementService {

	@Autowired
	ScheduleStatement_Repo scheduleRepo;

	@Autowired
	ScheduleHistory_Repo historyRepo;
	
	@PersistenceContext
	EntityManager entityManager;

	private BigDecimal generateNextId() {
	    BigDecimal maxId = historyRepo.findMaxId();
	    
	    if (maxId == null) {
	        return BigDecimal.ONE;
	    }
	    
	    return maxId.add(BigDecimal.ONE);
	}

	public List<ScheduledStatement_Entity> getAllSchedules() {
		return scheduleRepo.getScheduledata();
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

	public List<ScheduleHistory_Entity> getFailedDeliveries() {
		return historyRepo.findByDeliveryStatus("Failed");
	}

	@Transactional
	public void resendFailedStatement(BigDecimal historyId) throws Exception {

		ScheduleHistory_Entity historyRecord = historyRepo.findById(historyId)
				.orElseThrow(() -> new Exception("History record not found for ID: " + historyId));

		historyRecord.setIsRerun("Y");
		historyRecord.setDateSent(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
		historyRecord.setTimeSent(new SimpleDateFormat("HH:mm").format(new Date()));

		try {

			historyRecord.setDeliveryStatus("Success");
			historyRecord.setErrorReason("");

			historyRepo.saveAndFlush(historyRecord);

		} catch (Exception e) {
			historyRecord.setDeliveryStatus("Failed");
			historyRecord.setErrorReason("Rerun Error: " + e.getMessage());

			historyRepo.saveAndFlush(historyRecord);

			throw new Exception("Resend attempt failed: " + e.getMessage());
		}
	}
	public String getFailedTransactionTrend() {
	    YearMonth currentMonth = YearMonth.now();
	    YearMonth prevMonth = currentMonth.minusMonths(1);

	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("-MM-yyyy");
	    String currentMonthSuffix = currentMonth.format(formatter);
	    String prevMonthSuffix = prevMonth.format(formatter);

	    long currentCount = historyRepo.countByStatusAndMonth("Failed", currentMonthSuffix);
	    long prevCount = historyRepo.countByStatusAndMonth("Failed", prevMonthSuffix);

	    if (currentCount == 0 && prevCount == 0) {
	        return "0.0% From Last Month";
	    }

	    if (prevCount == 0) {
	        return "+100.0% From Last Month";
	    }

	    double percentageChange = ((double) (currentCount - prevCount) / prevCount) * 100.0;

	    if (percentageChange > 999.9) percentageChange = 999.9;
	    if (percentageChange < -999.9) percentageChange = -999.9;

	    String sign = percentageChange > 0 ? "+" : "";
	    return String.format("%s%.1f%% From Last Month", sign, percentageChange);
	}
	
	public long getFailedCountForCurrentMonth() {
		String currentMonthPattern = LocalDate.now().format(DateTimeFormatter.ofPattern("-MM-yyyy"));
		return historyRepo.countByStatusAndMonth("Failed", currentMonthPattern);
	}
	public List<ScheduleHistory_Entity> getFailedDeliveriesByMonth(String yyyyMm) {
	    String[] parts = yyyyMm.split("-");
	    String dbPattern = "-" + parts[1] + "-" + parts[0]; 
	    
	    return historyRepo.findByStatusAndMonth("Failed", dbPattern);
	}
	
	private String convertToDbPattern(String yyyyMm) {
	    String[] parts = yyyyMm.split("-");
	    return "-" + parts[1] + "-" + parts[0]; 
	}

	public long getCountByMonthAndStatus(String targetMonth, String status) {
	    String dbPattern = convertToDbPattern(targetMonth);
	    return historyRepo.countByStatusAndMonth(status, dbPattern);
	}

	public String getTrendByMonthAndStatus(String targetMonth, String status) {
	    
	    YearMonth currentMonth = YearMonth.parse(targetMonth);
	    YearMonth prevMonth = currentMonth.minusMonths(1);    

	    String currentPattern = convertToDbPattern(currentMonth.toString());
	    String prevPattern = convertToDbPattern(prevMonth.toString());

	    long currentCount = historyRepo.countByStatusAndMonth(status, currentPattern);
	    long prevCount = historyRepo.countByStatusAndMonth(status, prevPattern);

	    if (currentCount == 0 && prevCount == 0) {
	        return "0.0% From Last Month";
	    }
	    
	    if (prevCount == 0) {
	        return "+100.0% From Last Month";
	    }

	    double percentageChange = ((double) (currentCount - prevCount) / prevCount) * 100.0;

	    if (percentageChange > 999.9) percentageChange = 999.9;
	    if (percentageChange < -999.9) percentageChange = -999.9;

	    String sign = percentageChange > 0 ? "+" : "";
	    return String.format("%s%.1f%% From Last Month", sign, percentageChange);
	}

	public long getGeneratedCountForCurrentMonth() {
	    String currentMonthPattern = LocalDate.now().format(DateTimeFormatter.ofPattern("-MM-yyyy"));
	    return historyRepo.countByStatusAndMonth("Success", currentMonthPattern);
	}

	public String getGeneratedTransactionTrend() {
	    YearMonth currentMonth = YearMonth.now();
	    YearMonth prevMonth = currentMonth.minusMonths(1);

	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("-MM-yyyy");
	    String currentMonthSuffix = currentMonth.format(formatter);
	    String prevMonthSuffix = prevMonth.format(formatter);

	    long currentCount = historyRepo.countByStatusAndMonth("Success", currentMonthSuffix);
	    long prevCount = historyRepo.countByStatusAndMonth("Success", prevMonthSuffix);

	    if (currentCount == 0 && prevCount == 0) {
	        return "0.0% From Last Month";
	    }
	    if (prevCount == 0) {
	        return "+100.0% From Last Month";
	    }

	    double percentageChange = ((double) (currentCount - prevCount) / prevCount) * 100.0;

	    if (percentageChange > 999.9) percentageChange = 999.9;
	    if (percentageChange < -999.9) percentageChange = -999.9;

	    String sign = percentageChange > 0 ? "+" : "";
	    return String.format("%s%.1f%% From Last Month", sign, percentageChange);
	}
	
	
public String Schedule(MultipartFile marketingFile,List<AccountDTO> accountdata,String format) {
		
	
	BigDecimal Scheduledid=scheduleRepo.getSCHEDULEID();
	for(AccountDTO acc:accountdata) {
		BigDecimal id=scheduleRepo.getid();
		ScheduledStatement_Entity addnewdata=new ScheduledStatement_Entity();
		addnewdata.setId(id);
		addnewdata.setScheduleId(Scheduledid);
		addnewdata.setScheduleName(acc.getScheduleName());
		addnewdata.setRunTime(acc.getScheduleTime());
		addnewdata.setFrequency(acc.getFrequency());
		addnewdata.setStartDate(acc.getScheduleDate());
		addnewdata.setOutputFormat(format);
		addnewdata.setStatus("Active");
		addnewdata.setAccountNumber(acc.getAccountNumber());
		addnewdata.setAccountType(acc.getAccountType());
		addnewdata.setAcid(acc.getAcid());
		addnewdata.setCustomerEmailId(acc.getCustomerEmail());
		addnewdata.setCustomerId(acc.getCustomerId());
		addnewdata.setCustomerName(acc.getCustomerName());
		addnewdata.setCurrency(acc.getCurrency());
		
		scheduleRepo.save(addnewdata);
		
	}
		
		
		return "Sucessfully";
		
	}

}