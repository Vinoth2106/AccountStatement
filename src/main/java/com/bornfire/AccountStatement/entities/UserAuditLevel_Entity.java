package com.bornfire.AccountStatement.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="USER_AUDIT_LEVEL")
public class UserAuditLevel_Entity {
	
	@Id
    private String	audit_ref_no;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date	audit_date;
    private String	audit_screen;
    private String	audit_table;
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date	auth_time;
    private String	auth_user;
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date	entry_time;
    private String	entry_user;
    private String	event_id;
    private String	event_name;
    private String	func_code;
    private String	modi_details;
    private String	remarks;
	public String getAudit_ref_no() {
		return audit_ref_no;
	}
	public void setAudit_ref_no(String audit_ref_no) {
		this.audit_ref_no = audit_ref_no;
	}
	public Date getAudit_date() {
		return audit_date;
	}
	public void setAudit_date(Date audit_date) {
		this.audit_date = audit_date;
	}
	public String getAudit_screen() {
		return audit_screen;
	}
	public void setAudit_screen(String audit_screen) {
		this.audit_screen = audit_screen;
	}
	public String getAudit_table() {
		return audit_table;
	}
	public void setAudit_table(String audit_table) {
		this.audit_table = audit_table;
	}
	public Date getAuth_time() {
		return auth_time;
	}
	public void setAuth_time(Date auth_time) {
		this.auth_time = auth_time;
	}
	public String getAuth_user() {
		return auth_user;
	}
	public void setAuth_user(String auth_user) {
		this.auth_user = auth_user;
	}
	public Date getEntry_time() {
		return entry_time;
	}
	public void setEntry_time(Date entry_time) {
		this.entry_time = entry_time;
	}
	public String getEntry_user() {
		return entry_user;
	}
	public void setEntry_user(String entry_user) {
		this.entry_user = entry_user;
	}
	public String getEvent_id() {
		return event_id;
	}
	public void setEvent_id(String event_id) {
		this.event_id = event_id;
	}
	public String getEvent_name() {
		return event_name;
	}
	public void setEvent_name(String event_name) {
		this.event_name = event_name;
	}
	public String getFunc_code() {
		return func_code;
	}
	public void setFunc_code(String func_code) {
		this.func_code = func_code;
	}
	public String getModi_details() {
		return modi_details;
	}
	public void setModi_details(String modi_details) {
		this.modi_details = modi_details;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public UserAuditLevel_Entity(String audit_ref_no, Date audit_date, String audit_screen, String audit_table,
			Date auth_time, String auth_user, Date entry_time, String entry_user, String event_id, String event_name,
			String func_code, String modi_details, String remarks) {
		super();
		this.audit_ref_no = audit_ref_no;
		this.audit_date = audit_date;
		this.audit_screen = audit_screen;
		this.audit_table = audit_table;
		this.auth_time = auth_time;
		this.auth_user = auth_user;
		this.entry_time = entry_time;
		this.entry_user = entry_user;
		this.event_id = event_id;
		this.event_name = event_name;
		this.func_code = func_code;
		this.modi_details = modi_details;
		this.remarks = remarks;
	}
	public UserAuditLevel_Entity() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    

}
