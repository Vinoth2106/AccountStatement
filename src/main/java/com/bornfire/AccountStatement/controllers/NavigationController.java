package com.bornfire.AccountStatement.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bornfire.AccountStatement.entities.AccountDTO;
import com.bornfire.AccountStatement.entities.AuditServicesEntity;
import com.bornfire.AccountStatement.entities.AuditServicesRep;
import com.bornfire.AccountStatement.entities.Cust_table_entity;
import com.bornfire.AccountStatement.entities.Cust_table_rep;
import com.bornfire.AccountStatement.entities.GeneralMasterTbEntity;
import com.bornfire.AccountStatement.entities.GeneralMasterTbRep;
import com.bornfire.AccountStatement.entities.ScheduleHistory_Entity;
import com.bornfire.AccountStatement.entities.ScheduledStatement_Entity;
import com.bornfire.AccountStatement.entities.UserAuditLevel_Entity;
import com.bornfire.AccountStatement.entities.UserAuditRepo;
import com.bornfire.AccountStatement.entities.TransactionInquiry;
import com.bornfire.AccountStatement.entities.TransactionInquiryRep;
import com.bornfire.AccountStatement.services.ScheduleStatementService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bornfire.AccountStatement.entities.Service_audit_table_Rep;
import com.bornfire.AccountStatement.entities.Service_audit_table_entity;

@Controller
@ConfigurationProperties("default")
public class NavigationController {
	
	@Autowired
	Cust_table_rep cust_table_rep; 
	
	@Autowired
	GeneralMasterTbRep generalMasterTbRepo;	

	@Autowired
	AuditServicesRep userAuditRepo;
	
	@Autowired
	TransactionInquiryRep transactionInquiryRep;
	@Autowired
	Service_audit_table_Rep Service_audit_table_Rep;

	
	@RequestMapping(value = "Dashboard", method = { RequestMethod.GET, RequestMethod.POST })
	public String dashboard(@RequestParam(name = "frequency", required = false) String frequency, Model md,
			HttpServletRequest req) {
		
		//md.addAttribute("recentActivities", Service_audit_table_Rep.findTop4RecentActivities());
		
		md.addAttribute("recentActivities", ScheduleStatementService.getHistorylist());
		
		Map<String, String> accountsStats = calculateMonthlyGrowthStats(YearMonth.now().toString());
		md.addAttribute("accountsCount",accountsStats.get("count"));
	    md.addAttribute("accountsPercentage",accountsStats.get("percentage")+" From Last Month");
	    
		md.addAttribute("generatedCount", ScheduleStatementService.getGeneratedCountForCurrentMonth());
		md.addAttribute("generatedPercentage", ScheduleStatementService.getGeneratedTransactionTrend());
		
		md.addAttribute("failedCount", ScheduleStatementService.getFailedCountForCurrentMonth());
	    md.addAttribute("failurePercentage", ScheduleStatementService.getFailedTransactionTrend());

		return "AccountStatementDashboard";
	}
	
	@RequestMapping(value = "Accounts", method = { RequestMethod.GET, RequestMethod.POST })
	public String Accounts(@RequestParam(name = "frequency", required = false) String frequency, Model md,
			HttpServletRequest req) {
		
		List<Cust_table_entity> custlist = cust_table_rep.getRetaillist();
		md.addAttribute("custlist", custlist);

		return "Accounts";
	}
	
	@GetMapping("/getCustomersByType")
	@ResponseBody
	public List<Cust_table_entity> getCustomersByType(
	        @RequestParam String type) {

	    if ("retail".equalsIgnoreCase(type)) {
	        return cust_table_rep.getRetaillist();
	    } else if ("corporate".equalsIgnoreCase(type)) {
	        return cust_table_rep.getCorporatelist();
	    }
	    return cust_table_rep.getcustlist(); // fallback all
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
	
	
	@GetMapping("/getAccountdata")
	@ResponseBody
	public List<Object> loadAccounts(@RequestParam String filterValue){

		 return generalMasterTbRepo.findAllCustombytype(filterValue);

	}
	
	

	
	@RequestMapping(value = "NewStatementRequest", method = { RequestMethod.GET, RequestMethod.POST })
	public String NewStatementRequest(@RequestParam(name="tableData", required = false)String tableData,@RequestParam(name = "channel", required = false) String channel,@RequestParam(name = "formmode", required = false) String formmode, Model md,
			@RequestParam(value = "fd",required = false) String fromdate,@RequestParam(name = "statementtype", required = false) String statementtype,
			@RequestParam(value = "td",required = false) String todate,@RequestParam(value = "statementFormat",required = false) String statementFormat,
			@RequestParam(value="marketingFile",required = false) MultipartFile marketingFile,@RequestParam(name = "selectedStatements", required = false) String selectedStatements,
			HttpServletRequest req) throws ParseException, JsonParseException, JsonMappingException, IOException {
		
		if(formmode==null) {
			
			List<Cust_table_entity> custlist=cust_table_rep.getcustlist();
			md.addAttribute("custlist", custlist);
			md.addAttribute( "accountTypes",cust_table_rep.getDistinctAccountTypes());
			md.addAttribute( "Types",cust_table_rep.getDistinctAccountTypes());
			md.addAttribute( "schmtype",generalMasterTbRepo.getschmtype());
			md.addAttribute("formmode","StatementRequest");
			md.addAttribute("statementTypes", new ArrayList<>());
		}else if(formmode.equals("Preview")) {
			
			
			//System.out.println(marketingFile.getOriginalFilename());
			ObjectMapper mapper = new ObjectMapper();
			 List<AccountDTO> accountList=null;
			 AccountDTO accountdata=null;
			 String Accountnum=null;
			 String accountname=null;
			 String Acid=null;
			 String customerId=null;
			 
			  
			 if(tableData!=null) {
				   accountList =mapper.readValue(tableData,new TypeReference<List<AccountDTO>>() {});
				   accountdata=accountList.get(0);
			  }
			 
			 if (accountdata!=null) {
				 Accountnum=accountdata.getAccountNumber();
				 accountname=accountdata.getCustomerName();
				 Acid=accountdata.getAcid();
				 customerId=accountdata.getCustomerId();
			 }
		    
			 md.addAttribute("accountsJson",
				        new ObjectMapper().writeValueAsString(accountList));
							
				if(Accountnum!=null) {
					System.out.println("Accountnum="+Accountnum);
					List<GeneralMasterTbEntity> accountlists=generalMasterTbRepo.findbyAccountnum(Accountnum);
					if(accountlists!=null) {
						GeneralMasterTbEntity finaldata=accountlists.get(0);
						if(finaldata!=null) {
							Acid=finaldata.getAcid();
							accountname=finaldata.getAcct_name();
							customerId=finaldata.getCust_id();
						}
						
					}
					
				}	
			
			md.addAttribute("tranInquiry", transactionInquiryRep.findAllCustomind(Acid));
			List<String> statementTypes = new ArrayList<>();

			if (selectedStatements != null && !selectedStatements.isEmpty()) {
			    statementTypes = new ObjectMapper().readValue(
			        selectedStatements,
			        new TypeReference<List<String>>() {}
			    );
			}
			md.addAttribute("statementTypes", statementTypes);
			
			if (fromdate!=null & todate!=null) {
				SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MMM/yyyy");
				SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy");
				Date fromDateValue = inputFormat.parse(fromdate);
				Date toDateValue = inputFormat.parse(todate);
				fromdate = outputFormat.format(fromDateValue).toUpperCase();
				todate = outputFormat.format(toDateValue).toUpperCase();
				md.addAttribute("opr_datefd",output.format(fromDateValue).toUpperCase());
				md.addAttribute("opr_datetd",output.format(toDateValue).toUpperCase());
			}
			
			List<TransactionInquiry> tranlist =transactionInquiryRep.findAllCustominddate(Acid,fromdate,todate);
			System.out.println("tranlistsize="+tranlist.size());
			md.addAttribute("tranInquiry", tranlist);
			
			BigDecimal closingBalance = generalMasterTbRepo.getSumBalanceBetweenDates(Accountnum, fromdate, todate);
			
			BigDecimal openingBalance = transactionInquiryRep.getOpeningBalance(Acid, fromdate);

			md.addAttribute("openingBalance", openingBalance);
			
			md.addAttribute("acid",Acid);
			md.addAttribute("accountnumber",Accountnum);
			md.addAttribute("Acctname",accountname);
			md.addAttribute("customerId", customerId);
			
			BigDecimal totalCredit = BigDecimal.ZERO;
			BigDecimal totalDebit = BigDecimal.ZERO;

			for(TransactionInquiry custInq : tranlist){

			    if("C".equals(custInq.getPart_tran_type())){

			        totalCredit = totalCredit.add(custInq.getTran_amt());

			    }else if("D".equals(custInq.getPart_tran_type())){

			        totalDebit = totalDebit.add(custInq.getTran_amt());

			    }
			}

			md.addAttribute("totalCredit", totalCredit);
			md.addAttribute("totalDebit", totalDebit);
			md.addAttribute("closingBalance",(openingBalance.add(totalCredit)).subtract(totalDebit));
			md.addAttribute("accountList", accountList);
			String accountsJson =mapper.writeValueAsString(accountList);
			md.addAttribute("accountsJson", accountsJson);
			md.addAttribute("formmode",formmode);
			md.addAttribute("channel",channel);
			md.addAttribute("statementFormat", statementFormat);
			md.addAttribute("marketingFile", marketingFile);
			
		}

			
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
	
	@RequestMapping(value = "getFailedAccounts", method = RequestMethod.POST)
	@ResponseBody
	public List<ScheduleHistory_Entity> getFailedHistory(@RequestParam("scheduleId") BigDecimal scheduleId,@RequestParam("runId") BigDecimal runId) {
		
		List<ScheduleHistory_Entity>  dataaccount = ScheduleStatementService.getfieldHistory(scheduleId,runId);
		
		System.out.println("dataaccount="+dataaccount.size()+scheduleId+runId);
		return dataaccount;
	}
	
	

	@RequestMapping(value = "UserAudit", method = RequestMethod.GET)
	public String getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
		Pageable pageable = PageRequest.of(page, size); 

		Page<AuditServicesEntity> auditPage;

		if (keyword != null && !keyword.trim().isEmpty()) {
		    auditPage = userAuditRepo.searchByKeyword(keyword.trim(), pageable);
		} else {
		    auditPage = userAuditRepo.findAllByOrderByDateDesc(pageable);
		}

        model.addAttribute("page", auditPage);
        model.addAttribute("keyword", keyword);

		return "UserAudit";
	}
	@RequestMapping(value = "ServiceAudit", method = RequestMethod.GET)
	public String getServiceAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
		Pageable pageable = PageRequest.of(page, size); 

		Page<Service_audit_table_entity> auditPage;

		if (keyword != null && !keyword.trim().isEmpty()) {
		    auditPage = Service_audit_table_Rep.searchByKeyword(keyword.trim(), pageable);
		} else {
		    auditPage = Service_audit_table_Rep.findAllByOrderByDateDesc(pageable);
		}

        model.addAttribute("page", auditPage);
        model.addAttribute("keyword", keyword);

		return "ServiceAudit";
	}
	
	
	@RequestMapping(value = "StatementHistory", method = { RequestMethod.GET, RequestMethod.POST })
	public String StatementHistory(@RequestParam(name = "Status", required = false) String Status, Model md,
			HttpServletRequest req) {
		
		if(Status!=null) {
	
			md.addAttribute("Statementdata", ScheduleStatementService.getFailedDeliveries(Status));
			
		}else {
			
			md.addAttribute("Statementdata", ScheduleStatementService.getHistorylist());
		}
	
		

		return "StatementHistory";
	}
	
	@RequestMapping(value = "BulkHistory", method = { RequestMethod.GET, RequestMethod.POST })
	public String BulkHistory(@RequestParam(name = "frequency", required = false) String frequency, Model md,
			HttpServletRequest req) {
		
		List<Cust_table_entity> custlist=cust_table_rep.getcustlist();
		md.addAttribute("custlist", custlist);

		return "BulkHistory";
	}
	

	
	
	@GetMapping("/getPreviewData")
	@ResponseBody
	public Map<String,Object> getPreviewData(

	        @RequestParam String accountNo,

	        @RequestParam String fromDate,

	        @RequestParam String toDate) throws Exception {

	    Map<String,Object> response =
	    new HashMap<>();

	    GeneralMasterTbEntity account =generalMasterTbRepo.findByAcctNumber(accountNo);
	    
	    if (fromDate!=null & toDate!=null) {
			SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
			SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MMM/yyyy");
			SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy");
			Date fromDateValue = inputFormat.parse(fromDate);
			Date toDateValue = inputFormat.parse(toDate);
			fromDate = outputFormat.format(fromDateValue).toUpperCase();
			toDate = outputFormat.format(toDateValue).toUpperCase();
		}
	    
	    System.out.println("Acid="+account.getAcid());
	    System.out.println(fromDate);

	    List<TransactionInquiry> tranlist = transactionInquiryRep.findAllCustominddate(account.getAcid(),fromDate,toDate);

	    System.out.println("datacount="+tranlist.size());
	    BigDecimal totalCredit = BigDecimal.ZERO;

	    BigDecimal totalDebit = BigDecimal.ZERO;

	    for(TransactionInquiry txn : tranlist){

	        if("C".equals(txn.getPart_tran_type())){
	            totalCredit =totalCredit.add(txn.getTran_amt());

	        }else if("D".equals(txn.getPart_tran_type())){
	            totalDebit =totalDebit.add(txn.getTran_amt());
	        }
	    }

	    response.put("customerName",account.getAcct_name());
	    response.put("interestRate",account.getInt_rate());
	    response.put("interestAmount",account.getEmiamount());
	    response.put("accountNumber",account.getAcct_number());
	    response.put("currency",account.getAcct_crncy_code());
	    response.put("acid",account.getAcid());
	    response.put("customerId",account.getCust_id());
	    response.put("totalCredit",totalCredit);
	    response.put("totalDebit",totalDebit);
		BigDecimal openingBalance = transactionInquiryRep.getOpeningBalance(account.getAcid(), fromDate);
		response.put("openingBalance",openingBalance);
		response.put("closingBalance",(openingBalance.add(totalCredit)).subtract(totalDebit));


	    List<Map<String,Object>> txns = new ArrayList<>();

	    for(TransactionInquiry txn : tranlist){

	        Map<String,Object> t = new HashMap<>();
	        t.put("tranDate", new SimpleDateFormat("dd-MM-yyyy") .format(txn.getTran_date()));
	        t.put("tranId",txn.getTran_id());
	        t.put("partTranType","C".equals(txn.getPart_tran_type())? "Credit" : "Debit");
	        t.put("valueDate",new SimpleDateFormat("dd-MM-yyyy").format(txn.getValue_date()));
	        t.put("particular",txn.getTran_particular());
	        t.put("debit","D".equals(txn.getPart_tran_type()) ? txn.getTran_amt() : "-");
	        t.put("credit", "C".equals(txn.getPart_tran_type()) ? txn.getTran_amt() : "-");
	        t.put("currency", txn.getTran_crncy_code()); txns.add(t);

	    }

	    response.put("transactions", txns);

	    return response;

	}
	
	@RequestMapping(value = "FailedStatements", method = { RequestMethod.GET, RequestMethod.POST })
	public String failedStatements(@RequestParam(value = "month", required = false) String selectedMonth,Model md, HttpServletRequest req) {
		if (selectedMonth == null || selectedMonth.isEmpty()) {
	        selectedMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
	    }
		md.addAttribute("selectedMonth", selectedMonth);
		md.addAttribute("failedList", ScheduleStatementService.getFailedDeliveriesByMonth(selectedMonth));
		return "FailedStatements";
	}

	@RequestMapping(value = "resendScheduleStatements", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> resendStatement(@RequestParam("historyId") BigDecimal historyId) {
		Map<String, Object> response = new HashMap<>();

		try {
			ScheduleStatementService.resendFailedStatement(historyId);
			response.put("status", "success");
			response.put("message", "Statement resent successfully.");
		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "Failed to resend: " + e.getMessage());
			e.printStackTrace();
		}

		return response;
	}
    
	@GetMapping("/dashboard-stats")
	@ResponseBody
	public Map<String, Object> getDashboardStats(@RequestParam("month") String month) {

		System.out.println("Month : "+month);
		Map<String, Object> response = new HashMap<>();
		
		Map<String, String> accountsStats = calculateMonthlyGrowthStats(month);
	    response.put("accountsCount", accountsStats.get("count"));
	    response.put("accountsPercentage", accountsStats.get("percentage"));

		long failedCount = ScheduleStatementService.getCountByMonthAndStatus(month, "Failed");
		String failedTrend = ScheduleStatementService.getTrendByMonthAndStatus(month, "Failed");

		long generatedCount = ScheduleStatementService.getCountByMonthAndStatus(month, "Success");
		String generatedTrend = ScheduleStatementService.getTrendByMonthAndStatus(month, "Success");

		NumberFormat formatter = NumberFormat.getInstance(Locale.US);

		response.put("failedCount", formatter.format(failedCount));
		response.put("failedPercentage", failedTrend);

		response.put("generatedCount", formatter.format(generatedCount));
		response.put("generatedPercentage", generatedTrend);
		return response;
	}

	public Map<String, String> calculateMonthlyGrowthStats(String monthStr) {
		YearMonth currentMonth = YearMonth.parse(monthStr);
		YearMonth previousMonth = currentMonth.minusMonths(1);
		ZoneId zone = ZoneId.systemDefault();

		Date currentStart = Date.from(currentMonth.atDay(1).atStartOfDay(zone).toInstant());
		Date currentEnd = Date.from(currentMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(zone).toInstant());

		Date prevStart = Date.from(previousMonth.atDay(1).atStartOfDay(zone).toInstant());
		Date prevEnd = Date.from(previousMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(zone).toInstant());

		//long currentCount = cust_table_rep.countByCreatedDateBetween(currentStart, currentEnd);
		long currentCount = cust_table_rep.count();
		long prevCount = cust_table_rep.countByCreatedDateBetween(prevStart, prevEnd);

		String percentageFormatted;
		if (prevCount == 0) {
			percentageFormatted = currentCount > 0 ? "+100.0%" : "0.0%";
		} else {
			double percentage = ((double) (currentCount - prevCount) / prevCount) * 100;
			percentageFormatted = (percentage > 0 ? "+" : "") + String.format("%.1f", percentage) + "%";
		}

		Map<String, String> stats = new HashMap<>();
		stats.put("count", String.format("%,d", currentCount));
		stats.put("percentage", percentageFormatted);

		return stats;
	}
	
	@GetMapping("/systemotp")
	public String showOtpForm(Model model, HttpSession session) {
		String otp = (String) session.getAttribute("otp");
		model.addAttribute("otp", otp);
		return "ASOtpvalidation.html"; // Thymeleaf or HTML page
	}
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") String userOtp, HttpSession session) {
		String actualOtp = (String) session.getAttribute("otp");
		if (actualOtp != null && actualOtp.equals(userOtp)) {
			session.removeAttribute("otp"); // Clear OTP after success
			return "redirect:/Dashboard";
		}
		return "redirect:login?invalidotp";
	}
	@GetMapping("/getSchemeType")
	@ResponseBody
	public String getSchemeType(@RequestParam String acid) {

	    String schemeType =
	    		generalMasterTbRepo.getSchemeTypeByAcid(acid);

	    return schemeType;
	}


}
