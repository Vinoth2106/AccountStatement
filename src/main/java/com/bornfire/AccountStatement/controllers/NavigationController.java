package com.bornfire.AccountStatement.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DateFormat;
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
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.bornfire.AccountStatement.entities.AccountDTO;
import com.bornfire.AccountStatement.entities.AuditServicesEntity;
import com.bornfire.AccountStatement.entities.AuditServicesRep;
import com.bornfire.AccountStatement.entities.Cust_table_entity;
import com.bornfire.AccountStatement.entities.Cust_table_rep;
import com.bornfire.AccountStatement.entities.GeneralMasterTbEntity;
import com.bornfire.AccountStatement.entities.GeneralMasterTbRep;
import com.bornfire.AccountStatement.entities.RRReportRepo;
import com.bornfire.AccountStatement.entities.ScheduleHistory_Entity;
import com.bornfire.AccountStatement.entities.ScheduleHistory_Repo;
import com.bornfire.AccountStatement.entities.ScheduledStatement_Entity;
import com.bornfire.AccountStatement.entities.UserAuditLevel_Entity;
import com.bornfire.AccountStatement.entities.UserAuditRepo;
import com.bornfire.AccountStatement.entities.TransactionInquiry;
import com.bornfire.AccountStatement.entities.TransactionInquiryRep;
import com.bornfire.AccountStatement.services.ScheduleStatementService;
import com.bornfire.AccountStatement.services.RegulatoryReportServices;
import com.bornfire.AccountStatement.services.ReportServices;
import com.bornfire.AccountStatement.services.BRF001ReportService;
import com.bornfire.AccountStatement.services.BRF_DetailExcel_Service;
import com.bornfire.AccountStatement.services.CalculationService;
import com.bornfire.AccountStatement.services.Exceltopdfservice;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.JRException;

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
	ScheduleHistory_Repo historyRepo;

	@Autowired
	AuditServicesRep userAuditRepo;
	
	@Autowired
	TransactionInquiryRep transactionInquiryRep;
	@Autowired
	Service_audit_table_Rep Service_audit_table_Rep;

	@Autowired
	RRReportRepo rrReportlist;
	
	@Autowired
	ReportServices reportServices;
	
	@Autowired
	RegulatoryReportServices regreportServices;
	
	@Autowired
	BRF001ReportService BRF001ReportService;
	
	@Autowired
	BRF_DetailExcel_Service brf_DetailExcel_Service;
	
	@Autowired
	Exceltopdfservice exceltopdfservice;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private static final Logger logger = LoggerFactory.getLogger(NavigationController.class);
	
	private String pagesize;

	public String getPagesize() {
		return pagesize;
	}

	public void setPagesize(String pagesize) {
		this.pagesize = pagesize;
	}
	
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
			md.addAttribute("receiptNo", "DR20260001");
			md.addAttribute("transactionRef", "TXN123456");
			md.addAttribute("valueDate", new Date());
			md.addAttribute("branchName", "Main Branch");

			md.addAttribute("fdNumber", "FD100001");
			md.addAttribute("depositAmount", "100000");
			md.addAttribute("fdInterestRate", "5.25%");
			md.addAttribute("maturityDate", "04-06-2027");
			md.addAttribute("maturityAmount", "105250");
			md.addAttribute("currency", "AED");
			md.addAttribute("interestRate", "5.25%");
			md.addAttribute("interestAmount", "5250");
			
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
			md.addAttribute("selectedStatements", selectedStatements);
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
	
	@RequestMapping(value = "rerunAccounts", method = RequestMethod.POST)
	@ResponseBody
	public String RerunAccounts(@RequestParam("scheduleId") BigDecimal scheduleId,@RequestParam("runId") BigDecimal runId,
			@RequestParam("accountNumber") String accountNumber) {
		
		ScheduleHistory_Entity  dataaccount = ScheduleStatementService.getfieldrerun(scheduleId,runId,accountNumber);
		dataaccount.setDeliveryStatus("Success");
		dataaccount.setErrorReason("Success");
		historyRepo.save(dataaccount);
		
		return "Reruned Sucessfully";
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
	
	
	@RequestMapping(value = "monthly1", method = { RequestMethod.GET, RequestMethod.POST })
	public String monthly1(Model md, HttpServletRequest req, @RequestParam(value = "report_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date report_date) {
		String roleId = (String) req.getSession().getAttribute("ROLEID");

		// md.addAttribute("reportvalue", "RBS Reports");
		// md.addAttribute("reportid", "RBSReports");

//		String domainid = (String) req.getSession().getAttribute("DOMAINID");
		// md.addAttribute("reportsflag", "reportsflag");
		
		 String sql = "SELECT * FROM RR_RPT_MAST t " +
                 "WHERE t.REMARKS_5 = 'M1' " +
                 "AND t.END_DATE = ( " +
                 "  SELECT MAX(t2.END_DATE) FROM RR_RPT_MAST t2 " +
                 "  WHERE t2.RPT_CODE = t.RPT_CODE AND t2.REMARKS_5 = 'M1' " +
                 ") ORDER BY t.RPT_CODE";

        List<Map<String, Object>> reportList = jdbcTemplate.queryForList(sql);	
		
		md.addAttribute("menu", "Monthly 1 - BRF Report");
		md.addAttribute("reportlist", reportList);

		// md.addAttribute("reportlist", rrReportlist.getReportList());
		//md.addAttribute("reportlist", rrReportlist.getReportListmonthly1());//all list of M1
		//md.addAttribute("reportlist", rrReportlist.findReportsByRemarks("M1"));

//		if(report_date!=null && !report_date.equals(null)) {
//			System.out.println("report_date"+report_date);
//			md.addAttribute("reportlist", rrReportlist.findDataByDate(report_date,"M1"));
//			//md.addAttribute("reportlist", rrReportlist.findDataMissing(report_date))//missing data for this date
//			md.addAttribute("reportDate", report_date);
//		}
		
		return "BRF/RRReports";
	}
	@GetMapping("/checkDomainFlagwithdate")
	@ResponseBody
	public ResponseEntity<String> checkDomainFlagwithdate(@RequestParam String rptcode,
			@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,Model md) {
		//System.out.println("Date : " + date);
		md.addAttribute("reportDate", "2025-12-31");
		List<Date> datelist = rrReportlist.getdatelist(rptcode);
		System.out.println("rptcode="+rptcode);
		for (Date eachdate : datelist) {
			//System.out.println("Each date : " + eachdate);
			if (eachdate != null && eachdate.compareTo(date) == 0) {
				return ResponseEntity.ok("ENABLED");
			}
		}
		return ResponseEntity.ok("DISABLED");
	}
	
	@GetMapping("/checkDomainFlag")
	@ResponseBody
	public ResponseEntity<String> checkDomainFlag(@RequestParam String rptcode) {
		
		String sql = "SELECT * FROM RR_RPT_MAST WHERE RPT_CODE = ?";
		
		List<Map<String, Object>> report = jdbcTemplate.queryForList(sql, rptcode);
		
		if (report != null && !report.isEmpty()) {
	        for (Map<String, Object> each : report) {
	            // Extracts the DOMAIN column value from the map
	            String domain = (String) each.get("DOMAIN");
	            
	            if ("Y".equalsIgnoreCase(domain)) {
	                return ResponseEntity.ok("ENABLED");
	            }
	        }
	        return ResponseEntity.ok("DISABLED");
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
	    }
		
	}
	
	@Autowired
	CalculationService CalculationService;
	
	@RequestMapping(value = "BRFValidations", method = { RequestMethod.GET, RequestMethod.POST })
	public String BRFValidations(Model md, @RequestParam(value = "rptcode", required = false) String rptcode,
			@RequestParam(value = "todate", required = false) String todate, HttpServletRequest req) {
		String roleId = (String) req.getSession().getAttribute("ROLEID");
		System.out.println("role id issssssssssssssssssssssssssss" + roleId);

		// md.addAttribute("reportvalue", "RBS Reports");
		// md.addAttribute("reportid", "RBSReports");

		String domainid = (String) req.getSession().getAttribute("DOMAINID");
		// md.addAttribute("reportsflag", "reportsflag");
		// md.addAttribute("menu", "RBS Data Maintenance");

		System.out.println("Report Date : "+todate);
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
		LocalDate parsedDate = LocalDate.parse(todate, inputFormatter);
		String formattedDate = parsedDate.format(dateFormatter);
		System.out.println("Report_Date Formatted Date : " + formattedDate);
		
		//md.addAttribute("reportlist", brfValidationsRepo.getValidationList(rptcode));
		String sql1 = "SELECT * FROM RR_RPT_MAST WHERE rpt_code = ? AND end_date = ?";
		Map<String, Object> report = jdbcTemplate.queryForMap(sql1, rptcode, formattedDate);

		md.addAttribute("reportlist1", report);
		//md.addAttribute("reportlist1", rrReportlist.getReportbyrptcode(rptcode));
		md.addAttribute("RoleId", roleId);

		md.addAttribute("rpt_date", todate);
		md.addAttribute("rptcode", rptcode);
		
		String sql = "SELECT * FROM BBRF_REPORT_VALIDATION_TABLE WHERE rpt_code = ? ORDER BY srl_no";

		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, rptcode);
		
		for (Map<String, Object> row : list) {

		    String srcFormula = (String) row.get("SRC_FORMULA");
		    String destFormula = (String) row.get("DEST_FORMULA");

		    if (srcFormula != null && destFormula != null) {

		        BigDecimal srcValue =
		                CalculationService.calculate(srcFormula, formattedDate);

		        BigDecimal destValue =
		                CalculationService.calculate(destFormula, formattedDate);

		        if (srcValue.compareTo(destValue) == 0) {
		            row.put("CUR_STATUS", "Y");
		        } else {
		            row.put("CUR_STATUS", "N");
		        }
		    }
		}
		md.addAttribute("reportlist",list);
		return "BRF/BRFValidations";
	}
	
	DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
	
	List<String> pageSizes = Arrays.asList("A2", "A3", "A4");
	
	@RequestMapping(value = "Reports/{reportid}", method = RequestMethod.POST)
	public ModelAndView reportView(@PathVariable("reportid") String reportid,
			@RequestParam(value = "function", required = false) String function,
			@RequestParam("asondate") String asondate, @RequestParam(required = false) String fromdate,
			@RequestParam("todate") String todate, @RequestParam(value = "currency", required = false) String currency,
			@RequestParam(value = "subreportid", required = false) String subreportid,
			@RequestParam(value = "secid", required = false) String secid,
			@RequestParam(value = "dtltype", required = false) String dtltype,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "page", required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size,
			@RequestParam(value = "reportingTime", required = false) String reportingTime, Model md,
			HttpServletRequest req, BigDecimal srl_no) {

		String userid = (String) req.getSession().getAttribute("USERID");
		String roleid = (String) req.getSession().getAttribute("ROLEID");
		String accesscode = (String) req.getSession().getAttribute("ACCESSCODE");
		// Logging Navigation
		if (dtltype.equals("report")) {
			md.addAttribute("menu", "XBRLReports");
			/*
			 * loginServices.SessionLogging("REPORTS" + reportid, "M8",
			 * req.getSession().getId(), userid, req.getRemoteAddr(), "ACTIVE");
			 */
		} else {
			md.addAttribute("menu", "XBRLArchives");
			/*
			 * loginServices.SessionLogging("ARCHREPORTS" + reportid, "M9",
			 * req.getSession().getId(), userid, req.getRemoteAddr(), "ACTIVE");
			 */
		}

		logger.info("Get Report :" + reportid);
		logger.info("Get Report :" + asondate);
		try {
			asondate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(asondate));
			fromdate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(fromdate));
			todate = dateFormat.format(new SimpleDateFormat("dd/MM/yyyy").parse(todate));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		logger.info("Get Report :" + asondate);
		int currentPage = page.orElse(0);
		int pageSize = size.orElse(Integer.parseInt(pagesize));

		logger.info("Assigning Model Attributes :" + reportid);
		// Assigning required Modal Attributes
		md.addAttribute("UserId", userid);
		md.addAttribute("RoleId", roleid);
		md.addAttribute("UserCol", accesscode);

		md.addAttribute("reportid", reportid);
		md.addAttribute("asondate", asondate);
		md.addAttribute("fromdate", fromdate);
		md.addAttribute("todate", todate);
		md.addAttribute("currency", currency);
		md.addAttribute("dtltype", dtltype);
		md.addAttribute("type", type);
		md.addAttribute("reportingTime", reportingTime);
		md.addAttribute("reportTitle", reportServices.getReportName(reportid));

		logger.info("Getting ModelandView :" + reportid);
		ModelAndView mv = new ModelAndView();
		System.out.println("tttttttttttttt" + userid);

		mv = regreportServices.getReportView(reportid, asondate, fromdate, todate, currency, dtltype, subreportid,
				secid, reportingTime, PageRequest.of(currentPage, pageSize), srl_no, userid);

		// System.out.println("----------------------");

		// Page<Object> sup0700RepPage = (Page<Object>)
		// mv.getModelMap().get("reportsummary");

		// sup0700RepPage.getContent().forEach((a)-> System.out.println(a.toString()));
		mv.addObject("pageSizes", pageSizes);
		
		return mv;

	}
	
	@RequestMapping(value = "Reports/{reportid}/Summary", method = RequestMethod.GET)
	public ModelAndView reportSummay(@PathVariable("reportid") String reportid,
			@RequestParam("asondate") String asondate, @RequestParam("fromdate") String fromdate,
			@RequestParam("todate") String todate, @RequestParam("currency") String currency,
			@RequestParam(value = "subreportid", required = false) String subreportid,
			@RequestParam(value = "secid", required = false) String secid,
			@RequestParam(value = "dtltype", required = false) String dtltype,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "page", required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size,
			@RequestParam(value = "reportingTime", required = false) String reportingTime, Model md, BigDecimal srl_no,
			HttpServletRequest req) {

		logger.info("Getting Report Summary :" + reportid);

		int currentPage = page.orElse(0);
		int pageSize = size.orElse(Integer.parseInt(pagesize));

		logger.info("Assigning Model Attributes :" + reportid);
		md.addAttribute("menu", "XBRLReports");
		md.addAttribute("reportid", reportid);
		md.addAttribute("asondate", asondate);
		md.addAttribute("fromdate", fromdate);
		md.addAttribute("todate", todate);
		md.addAttribute("type", type);
		md.addAttribute("currency", currency);
		md.addAttribute("reportingTime", reportingTime);
		md.addAttribute("dtltype", dtltype);
		md.addAttribute("reportTitle", reportServices.getReportName(reportid));
		md.addAttribute("reportingTime", reportingTime);
		md.addAttribute("displaymode", "summary");

		String roleId = (String) req.getSession().getAttribute("ROLEID");
		System.out.println("role id issssssssssssssssssssssssssss" + roleId);
		md.addAttribute("operation", roleId);

		logger.info("Getting ModelandView :" + reportid);
		ModelAndView mv = regreportServices.getReportView(reportid, asondate, fromdate, todate, currency, dtltype,
				subreportid, secid, reportingTime, PageRequest.of(currentPage, pageSize), srl_no, roleId);
		
		mv.addObject("pageSizes", pageSizes);

		return mv;

	}
	
	
	@RequestMapping(value = "Reports/{reportid}/Details", method = RequestMethod.GET)
	public ModelAndView reportDetail(@PathVariable("reportid") String reportid,
			@RequestParam(value = "instancecode", required = false) String instancecode,
			@RequestParam(value = "filter", required = false) String filter, @RequestParam("asondate") String asondate,
			@RequestParam("fromdate") String fromdate, @RequestParam("todate") String todate,
			@RequestParam("currency") String currency,
			@RequestParam(value = "subreportid", required = false) String subreportid,
			@RequestParam(value = "secid", required = false) String secid,
			@RequestParam(value = "dtltype", required = false) String dtltype,
			@RequestParam(value = "page", required = false) Optional<Integer> page,
			@RequestParam(value = "size", required = false) Optional<Integer> size,
			@RequestParam(value = "reportingTime", required = false) String reportingTime,@RequestParam(value = "searchVal", required = false) String searchVal, Model md) {

		logger.info("Getting Report Details :" + reportid);
		logger.info("Assigning Model Attributes :" + reportid);

		md.addAttribute("menu", "XBRLReports");
		md.addAttribute("reportid", reportid);
		md.addAttribute("asondate", asondate);
		md.addAttribute("fromdate", fromdate);
		md.addAttribute("todate", todate);
		md.addAttribute("filter", filter);
		md.addAttribute("currency", currency);
		md.addAttribute("dtltype", dtltype);
		md.addAttribute("reportingTime", reportingTime);
		// md.addAttribute("instancecode", Integer.parseInt(instancecode));
		md.addAttribute("reportTitle", reportServices.getReportName(reportid));
		md.addAttribute("displaymode", "detail");

		int currentPage = page.orElse(0);
		int pageSize = size.orElse(100);

		logger.info("Getting ModelandView :" + reportid);
		ModelAndView mv = regreportServices.getReportDetails(reportid, instancecode, asondate, fromdate, todate,
				currency, reportingTime, dtltype, subreportid, secid, PageRequest.of(currentPage, pageSize), filter,searchVal);

		return mv;

	}
	
	@RequestMapping(value = "Reports/{reportid}/PrecheckRR", method = RequestMethod.GET)
	@ResponseBody
	public String reportPreCheckRR(@PathVariable("reportid") String reportid,

			@RequestParam(required = false) String fromdate, @RequestParam("todate") String todate)
			throws ParseException {

		logger.info("Precheck for Report :" + reportid);

		if (todate.length() == 10) {
			return regreportServices.preCheckReportRBS(reportid, fromdate, todate);
		} else {

			try {
				todate = new SimpleDateFormat("dd-MM-yyyy").format(dateFormat.parse(todate));

			} catch (ParseException e) {

				e.printStackTrace();
			}

			return regreportServices.preCheckReportRBS(reportid, fromdate, todate);
		}

	}
	
	@RequestMapping(value = "Reports/CustomerDetailEditBrf1", method = RequestMethod.POST)
	@ResponseBody
	public String CustomerDetailEditBrf76(
			HttpServletRequest hs, @RequestParam("foracid") String foracid,
			@RequestParam("report_addl_criteria_1") String report_addl_criteria_1,
			@RequestParam("act_balance_amt_lc") BigDecimal act_balance_amt_lc,
			@RequestParam("report_label_1") String report_label_1,
			@RequestParam("report_name_1") String report_name_1,@RequestParam("report_date") String report_date,
			@RequestParam(value = "reason", required = false) String reason) {
		System.out.println("edit");

		System.out.println("Acct no " + foracid);
		
		System.out.println("Report Date " + report_date);
		/*
		 * AuditReasonDTO dto = new AuditReasonDTO(); dto.setReason(reason);
		 */
		return BRF001ReportService.detailChanges1(foracid, report_addl_criteria_1, act_balance_amt_lc,
				report_label_1, report_name_1,report_date);
	}
	 
	@RequestMapping(value = "Reports/{reportid}/Download", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseEntity<InputStreamResource> XBRLDownload(HttpServletResponse response,
	        @PathVariable("reportid") String reportid, @RequestParam("asondate") String asondate,
	        @RequestParam("fromdate") String fromdate, @RequestParam("todate") String todate,
	        @RequestParam("currency") String currency,
	        @RequestParam(value = "subreportid", required = false) String subreportid,
	        @RequestParam(value = "secid", required = false) String secid,
	        @RequestParam(value = "dtltype", required = false) String dtltype,
	        @RequestParam(value = "reportingTime", required = false) String reportingTime,
	        @RequestParam(value = "instancecode", required = false) String instancecode,
	        @RequestParam("filetype") String filetype, @RequestParam(value = "filter", required = false) String filter,
	        @RequestParam(value = "pagesize", required = false, defaultValue = "A3") String pagesize)
	        throws IOException, SQLException {
	    response.setContentType("application/octet-stream");

	    try {
	        logger.info(
	                "Getting download File :" + reportid + ", FileType :" + filetype + ", SubreportId :" + subreportid);
	        
	        HttpHeaders headers = new HttpHeaders();
	        
	        // Detail Excel Download
	        if ("detailexcel".equalsIgnoreCase(filetype)) {

	            byte[] excelBytes = regreportServices.detailexceldownload(reportid, todate);

	            headers.setContentType(
	                    MediaType.parseMediaType("application/vnd.ms-excel"));
	            headers.setContentDispositionFormData("attachment",
	                    reportid + "_Detail.xlsx");

	            InputStreamResource resource = new InputStreamResource(
	                    new ByteArrayInputStream(excelBytes));

	            return ResponseEntity.ok()
	                    .headers(headers)
	                    .contentLength(excelBytes.length)
	                    .body(resource);
	        }
	        
	        // Detail PDF Download
	        if ("detailpdf".equalsIgnoreCase(filetype)) {
	        	
	        	byte[] excelBytes = regreportServices.detailexceldownload(reportid, todate);
	        	
	        	byte[] pdfBytes = brf_DetailExcel_Service.convertExcelBytesToPdf(excelBytes);
	        	
	        	InputStreamResource resource =
			            new InputStreamResource(new ByteArrayInputStream(pdfBytes));

			    headers.setContentType(MediaType.APPLICATION_PDF);
			    headers.setContentDispositionFormData("attachment", reportid + "_Detail.pdf");

			    return ResponseEntity.ok()
			            .headers(headers)
			            .contentLength(pdfBytes.length)
			            .body(resource);
	        	        	
	        }
	        File repfile = null;
	        if ("BRF".equalsIgnoreCase(filetype) || "BRFEXCELTOPDF".equalsIgnoreCase(filetype)) {
	            repfile = regreportServices.getDownloadFile(reportid, asondate, fromdate, todate, currency,
	                    subreportid, secid, dtltype, reportingTime, filetype, instancecode, filter);
	            System.out.println(filter + "filter");
	        }

	        // Summary Excel
	        if ("BRF".equalsIgnoreCase(filetype)) { 

	        	headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	            headers.setContentDispositionFormData("attachment", repfile.getName());

	            InputStreamResource resource = new InputStreamResource(new FileInputStream(repfile));

	            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM)
	                    .contentLength(repfile.length()).body(resource);
	        }

	        if ("BRFEXCELTOPDF".equalsIgnoreCase(filetype)) {
	        	
	        	System.out.println("camet to excel pdf");

	        	// File → byte[]
	            byte[] excelBytes = Files.readAllBytes(repfile.toPath());

	            
	            byte[] pdfBytes = exceltopdfservice.convertExcelBytesToPdf(excelBytes, pagesize);

	            InputStreamResource resource =
	                    new InputStreamResource(new ByteArrayInputStream(pdfBytes));

	            headers.setContentType(MediaType.APPLICATION_PDF);
	            headers.setContentDispositionFormData(
	                    "attachment",
	                    repfile.getName().replace(".xlsx", ".pdf")
	            );

	            return ResponseEntity.ok()
	                    .headers(headers)
	                    .contentLength(pdfBytes.length)
	                    .body(resource);
	        }
	        
	        // **CALL COMMON AUDIT FUNCTION HERE**
//	        auditService.saveCommonAudit(reportid, filetype,todate);
	        	
	        logger.warn("Unhandled filetype for reportid {} : {}", reportid, filetype);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	        
	    } catch (JRException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}

}
