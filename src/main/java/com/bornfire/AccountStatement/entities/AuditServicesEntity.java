package com.bornfire.AccountStatement.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="USER_AUDIT")

public class AuditServicesEntity {
	
	@Id
	private BigDecimal	srl_no;
	private String	log_in_user_id;
	private String	log_in_user_name;
	private String	log_in_designation;
	private BigDecimal	log_in_contact_no;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date	log_in_audit_date;
	private String	new_user_id;
	private String	new_user_name;
	public BigDecimal getSrl_no() {
		return srl_no;
	}
	public void setSrl_no(BigDecimal srl_no) {
		this.srl_no = srl_no;
	}
	public String getLog_in_user_id() {
		return log_in_user_id;
	}
	public void setLog_in_user_id(String log_in_user_id) {
		this.log_in_user_id = log_in_user_id;
	}
	public String getLog_in_user_name() {
		return log_in_user_name;
	}
	public void setLog_in_user_name(String log_in_user_name) {
		this.log_in_user_name = log_in_user_name;
	}
	public String getLog_in_designation() {
		return log_in_designation;
	}
	public void setLog_in_designation(String log_in_designation) {
		this.log_in_designation = log_in_designation;
	}
	public BigDecimal getLog_in_contact_no() {
		return log_in_contact_no;
	}
	public void setLog_in_contact_no(BigDecimal log_in_contact_no) {
		this.log_in_contact_no = log_in_contact_no;
	}
	public Date getLog_in_audit_date() {
		return log_in_audit_date;
	}
	public void setLog_in_audit_date(Date log_in_audit_date) {
		this.log_in_audit_date = log_in_audit_date;
	}
	public String getNew_user_id() {
		return new_user_id;
	}
	public void setNew_user_id(String new_user_id) {
		this.new_user_id = new_user_id;
	}
	public String getNew_user_name() {
		return new_user_name;
	}
	public void setNew_user_name(String new_user_name) {
		this.new_user_name = new_user_name;
	}
	public AuditServicesEntity(BigDecimal srl_no, String log_in_user_id, String log_in_user_name,
			String log_in_designation, BigDecimal log_in_contact_no, Date log_in_audit_date, String new_user_id,
			String new_user_name) {
		super();
		this.srl_no = srl_no;
		this.log_in_user_id = log_in_user_id;
		this.log_in_user_name = log_in_user_name;
		this.log_in_designation = log_in_designation;
		this.log_in_contact_no = log_in_contact_no;
		this.log_in_audit_date = log_in_audit_date;
		this.new_user_id = new_user_id;
		this.new_user_name = new_user_name;
	}
	public AuditServicesEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
