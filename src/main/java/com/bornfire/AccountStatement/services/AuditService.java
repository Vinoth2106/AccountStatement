package com.bornfire.AccountStatement.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bornfire.AccountStatement.entities.AuditServicesEntity;
import com.bornfire.AccountStatement.entities.AuditServicesRep;
import com.bornfire.AccountStatement.entities.Service_audit_table_Rep;
import com.bornfire.AccountStatement.entities.Service_audit_table_entity;
import com.bornfire.AccountStatement.entities.UserProfile;

@Service
public class AuditService {

	@Autowired
	private AuditServicesRep auditServicesRep;

	@Autowired
	Service_audit_table_Rep Service_audit_table_Reps;

	public void createLoginAudit(final String customerId, final String functionCode, final String screenName,
			final Map<String, String> changeDetails, final String tableName, final UserProfile user) {
		try {
			final UUID auditID = UUID.randomUUID();
			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			String userId = null;
			String username = null;
			if (attr != null) {
				HttpServletRequest request = attr.getRequest();
				userId = (String) request.getSession().getAttribute("USERID");
				username = (String) request.getSession().getAttribute("USERNAME");
			}
			final Date currentDate = new Date();

			AuditServicesEntity audit = new AuditServicesEntity();
			audit.setAudit_ref_no(auditID.toString());
			audit.setAudit_date(currentDate);
			audit.setEntry_time(currentDate);
			audit.setEntry_user(user.getUserid());
			audit.setEntry_user_name(user.getUsername());
			audit.setFunc_code(functionCode);
			audit.setRemarks(functionCode+" Successfully ");
			audit.setAudit_table(tableName);
			audit.setAudit_screen(screenName);
			audit.setEvent_id(user.getUserid());
			audit.setEvent_name(user.getUsername());

			if (changeDetails != null && !changeDetails.isEmpty()) {
				StringBuilder changes = new StringBuilder();
				changeDetails
						.forEach((field, value) -> changes.append(field).append(": ").append(value).append("||| "));

				audit.setChange_details(changes.toString());
			}

			if ("VERIFY".equalsIgnoreCase(functionCode)) {
				audit.setAuth_user(userId);
				audit.setAuth_user_name(username);
				audit.setAuth_time(currentDate);
			}

			audit.setReport_id(customerId);

//			System.out.println(audit);
			auditServicesRep.save(audit);

		} catch (Exception e) {
			System.err.println("Error creating business audit: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void createBusinessAudit(final String customerId, final String functionCode, final String screenName,
			final Map<String, String> changeDetails, final String tableName) {
		try {
			System.out.println("Entered Service Audit");
			final UUID auditID = UUID.randomUUID();
			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			// System.out.println("ServletRequestAttributes : "+attr);
			String userId = null;
			String username = null;
			if (attr != null) {
				HttpServletRequest request = attr.getRequest();
				userId = (String) request.getSession().getAttribute("USERID");
				username = (String) request.getSession().getAttribute("USERNAME");
			}
			final Date currentDate = new Date();

			Service_audit_table_entity audit = new Service_audit_table_entity();
			audit.setAudit_ref_no(auditID.toString());
			audit.setAudit_date(currentDate);
			audit.setEntry_time(currentDate);
			audit.setEntry_user(userId);
			audit.setEntry_user_name(username);
			audit.setFunc_code(functionCode);
			audit.setAudit_table(tableName);
			audit.setAudit_screen(screenName);
			audit.setEvent_id(userId);
			audit.setEvent_name(username);

			if (changeDetails != null && !changeDetails.isEmpty()) {
				StringBuilder changes = new StringBuilder();
				changeDetails
						.forEach((field, value) -> changes.append(field).append(": ").append(value).append("||| "));

				audit.setModi_details(changes.toString());
			}

			if ("VERIFY".equalsIgnoreCase(functionCode)) {
				audit.setAuth_user(userId);
				audit.setAuth_user_name(username);
				audit.setAuth_time(currentDate);
			}

			audit.setDomain_id(customerId);

//			System.out.println(audit);
			Service_audit_table_Reps.save(audit);

		} catch (Exception e) {
			System.err.println("Error creating business audit: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void createBusinessAuditbackground(final String customerId, final String functionCode,
			final String screenName, final Map<String, String> changeDetails, final String tableName,
			ServletRequestAttributes attr) {
		try {
			System.out.println("Entered Service Audit");
			final UUID auditID = UUID.randomUUID();
			// ServletRequestAttributes attr = (ServletRequestAttributes)
			// RequestContextHolder.getRequestAttributes();
			// System.out.println("ServletRequestAttributes : "+attr);
			String userId = null;
			String username = null;
			if (attr != null) {
				HttpServletRequest request = attr.getRequest();
				userId = (String) request.getSession().getAttribute("USERID");
				username = (String) request.getSession().getAttribute("USERNAME");
			}
			final Date currentDate = new Date();

			Service_audit_table_entity audit = new Service_audit_table_entity();
			audit.setAudit_ref_no(auditID.toString());
			audit.setAudit_date(currentDate);
			audit.setEntry_time(currentDate);
			audit.setEntry_user(userId);
			audit.setEntry_user_name(username);
			audit.setFunc_code(functionCode);
			audit.setAudit_table(tableName);
			audit.setAudit_screen(screenName);
			audit.setEvent_id(userId);
			audit.setEvent_name(username);

			if (changeDetails != null && !changeDetails.isEmpty()) {
				StringBuilder changes = new StringBuilder();
				changeDetails
						.forEach((field, value) -> changes.append(field).append(": ").append(value).append("||| "));

				audit.setModi_details(changes.toString());
			}

			if ("VERIFY".equalsIgnoreCase(functionCode)) {
				audit.setAuth_user(userId);
				audit.setAuth_user_name(username);
				audit.setAuth_time(currentDate);
			}

			audit.setDomain_id(customerId);

//			System.out.println(audit);
			Service_audit_table_Reps.save(audit);

		} catch (Exception e) {
			System.err.println("Error creating business audit: " + e.getMessage());
			e.printStackTrace();
		}
	}
/*
	public String fetchChanges(@RequestParam(required = false) String audit_ref_no) {

		return auditServicesRep.getchanges(audit_ref_no);
	}
*/
}
