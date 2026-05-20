package com.bornfire.AccountStatement.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bornfire.AccountStatement.entities.Cust_table_entity;
import com.bornfire.AccountStatement.entities.Cust_table_rep;
import com.bornfire.AccountStatement.entities.GeneralMasterTbEntity;
import com.bornfire.AccountStatement.entities.GeneralMasterTbRep;
import com.bornfire.AccountStatement.entities.ScheduleHistory_Entity;
import com.bornfire.AccountStatement.entities.ScheduledStatement_Entity;
import com.bornfire.AccountStatement.services.ScheduleStatementService;

@Controller
@ConfigurationProperties("default")
public class NavigationController {
	
	@Autowired
	Cust_table_rep cust_table_rep; 
	
	@Autowired
	GeneralMasterTbRep generalMasterTbRepo;
	
	
	@RequestMapping(value = "Dashboard", method = { RequestMethod.GET, RequestMethod.POST })
	public String dashboard(@RequestParam(name = "frequency", required = false) String frequency, Model md,
			HttpServletRequest req) {

		return "AccountStatementDashboard";
	}
	
	@RequestMapping(value = "Accounts", method = { RequestMethod.GET, RequestMethod.POST })
	public String Accounts(@RequestParam(name = "frequency", required = false) String frequency, Model md,
			HttpServletRequest req) {
		
		List<Cust_table_entity> custlist=cust_table_rep.getcustlist();
		md.addAttribute("custlist", custlist);

		return "Accounts";
	}
	
	@GetMapping("/getCustomerAccounts")
	@ResponseBody
	public List<GeneralMasterTbEntity> getCustomerAccounts(
	        @RequestParam("cust_id") String cust_id){

	    return generalMasterTbRepo.findAllCustom(cust_id);

	}
	
	@GetMapping("/getCustomerAccountsbytype")
	@ResponseBody
	public List<Object> getCustomerAccountsbytype(
	        @RequestParam("accountType") String accountType){

	    return generalMasterTbRepo.findAllCustombytype(accountType);

	}
	
	
	
	@RequestMapping(value = "NewStatementRequest", method = { RequestMethod.GET, RequestMethod.POST })
	public String NewStatementRequest(@RequestParam(name = "frequency", required = false) String frequency, Model md,
			HttpServletRequest req) {
		
		List<Cust_table_entity> custlist=cust_table_rep.getcustlist();
		md.addAttribute("custlist", custlist);
		md.addAttribute( "accountTypes",cust_table_rep.getDistinctAccountTypes()
			    );
		return "StatementRequest";
	}
	



	@Autowired
	ScheduleStatementService ScheduleStatementService;

	@RequestMapping(value = "ScheduleStatements", method = { RequestMethod.GET, RequestMethod.POST })
	public String ScheduleStatements(Model md, HttpServletRequest req) {
		md.addAttribute("scheduleList", ScheduleStatementService.getAllSchedules());
		return "ScheduleStatements";
	}

	@RequestMapping(value = "saveScheduleStatements", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> saveSchedule(@RequestParam("scheduleName") String scheduleName,
			@RequestParam("frequency") String frequency, @RequestParam("dayDesc") String dayDesc,
			@RequestParam("runTime") String runTime, @RequestParam("startDate") String startDate,
			@RequestParam("outputFormat") String outputFormat, @RequestParam("status") String status,
			@RequestParam(value = "recipients", required = false, defaultValue = "") String recipients) {
		ScheduledStatement_Entity s = new ScheduledStatement_Entity();
		s.setScheduleName(scheduleName);
		s.setFrequency(frequency);
		s.setDayDesc(dayDesc);
		s.setRunTime(runTime);
		s.setStartDate(startDate);
		s.setOutputFormat(outputFormat);
		s.setStatus(status);
		s.setRecipients(recipients);
		return ScheduleStatementService.saveSchedule(s);
	}

	@RequestMapping(value = "historyScheduleStatements", method = RequestMethod.POST)
	@ResponseBody
	public List<ScheduleHistory_Entity> getHistory(@RequestParam("scheduleId") BigDecimal scheduleId) {
		return ScheduleStatementService.getHistory(scheduleId);
	}


	@RequestMapping(value = "StatementHistory", method = { RequestMethod.GET, RequestMethod.POST })
	public String StatementHistory(@RequestParam(name = "frequency", required = false) String frequency, Model md,
			HttpServletRequest req) {
		
		List<Cust_table_entity> custlist=cust_table_rep.getcustlist();
		md.addAttribute("custlist", custlist);

		return "StatementHistory";
	}
	
	
	
	
	
	


}
