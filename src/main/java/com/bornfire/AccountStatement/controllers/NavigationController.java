package com.bornfire.AccountStatement.controllers;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	public String NewStatementRequest(@RequestParam(name = "frequency", required = false) String frequency,
			@RequestParam(name = "formmode", required = false) String formmode, Model md,@RequestParam(name = "Account", required = false) String Account
			,@RequestParam(name = "Accountnum", required = false) String Accountnum,@RequestParam(name = "accountname", required = false) String accountname,
			@RequestParam(value = "fd",required = false) String fromdate,@RequestParam(name = "customerId", required = false) String customerId,  @RequestParam(required=false) String accounts,
			@RequestParam(value = "td",required = false) String todate,HttpServletRequest req) throws ParseException {
		
		if(formmode==null) {
			List<Cust_table_entity> custlist=cust_table_rep.getcustlist();
			md.addAttribute("custlist", custlist);
			md.addAttribute( "accountTypes",cust_table_rep.getDistinctAccountTypes());
			md.addAttribute("formmode","StatementRequest");
			
		}else if(formmode.equals("Preview")) {
			
			md.addAttribute("formmode",formmode);
			md.addAttribute("tranInquiry", transactionInquiryRep.findAllCustomind(Account));
			
			
			
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
			
			
			List<TransactionInquiry> tranlist =transactionInquiryRep.findAllCustominddate(Account,fromdate,todate);
			System.out.println("tranlistsize="+tranlist.size());
			md.addAttribute("tranInquiry", tranlist);
			
			BigDecimal closingBalance =
				    generalMasterTbRepo.getSumBalanceBetweenDates(Accountnum, fromdate, todate);
			md.addAttribute("closingBalance",closingBalance);
			md.addAttribute("acid",Account);
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
			
			
		}
		else if(formmode.equals("MultiPreview")) {

		    md.addAttribute("formmode", formmode);

		    List<String> accountList =
		            Arrays.asList(accounts.split(","));

		    md.addAttribute("accountList", accountList);

		    md.addAttribute("fromDate", fromdate);

		    md.addAttribute("toDate", todate);

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
	public String StatementHistory(@RequestParam(name = "frequency", required = false) String frequency, Model md,
			HttpServletRequest req) {
		
		List<Cust_table_entity> custlist=cust_table_rep.getcustlist();
		md.addAttribute("custlist", custlist);

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

	    GeneralMasterTbEntity account =
	            generalMasterTbRepo
	            .findByAcctNumber(accountNo);

	    List<TransactionInquiry> tranlist =
	            transactionInquiryRep
	            .findAllCustominddate(
	                    account.getAcid(),
	                    fromDate,
	                    toDate);

	    BigDecimal totalCredit = BigDecimal.ZERO;

	    BigDecimal totalDebit = BigDecimal.ZERO;

	    for(TransactionInquiry txn : tranlist){

	        if("C".equals(txn.getPart_tran_type())){

	            totalCredit =
	                    totalCredit.add(txn.getTran_amt());

	        }else if("D".equals(txn.getPart_tran_type())){

	            totalDebit =
	                    totalDebit.add(txn.getTran_amt());

	        }
	    }

	    response.put("customerName",
	            account.getAcct_name());

	    response.put("accountNumber",
	            account.getAcct_number());

	    response.put("currency",
	            account.getAcct_crncy_code());

	    response.put("openingBalance",
	            account.getAcct_balance_amt_ac());

	    response.put("closingBalance",
	            account.getAcct_balance_amt_ac());

	    response.put("acid",
	            account.getAcid());

	    response.put("customerId",
	            account.getCust_id());

	    response.put("totalCredit",
	            totalCredit);

	    response.put("totalDebit",
	            totalDebit);

	    List<Map<String,Object>> txns =
	            new ArrayList<>();

	    for(TransactionInquiry txn : tranlist){

	        Map<String,Object> t =
	                new HashMap<>();

	        t.put("tranDate",
	                new SimpleDateFormat("dd-MM-yyyy")
	                .format(txn.getTran_date()));

	        t.put("tranId",
	                txn.getTran_id());

	        t.put("partTranType",
	                "C".equals(txn.getPart_tran_type())
	                ? "Credit" : "Debit");

	        t.put("valueDate",
	                new SimpleDateFormat("dd-MM-yyyy")
	                .format(txn.getValue_date()));

	        t.put("particular",
	                txn.getTran_particular());

	        t.put("debit",
	                "D".equals(txn.getPart_tran_type())
	                ? txn.getTran_amt()
	                : "-");

	        t.put("credit",
	                "C".equals(txn.getPart_tran_type())
	                ? txn.getTran_amt()
	                : "-");

	        t.put("currency",
	                txn.getTran_crncy_code());

	        txns.add(t);

	    }

	    response.put("transactions", txns);

	    return response;

	}
	

	
}
