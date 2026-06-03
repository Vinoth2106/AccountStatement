package com.bornfire.AccountStatement.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "SCHEDULE_STATEMENT")
public class ScheduledStatement_Entity {

	   @Id
	    @Column(name = "ID", precision = 20, scale = 0)
	    private BigDecimal id;

	    @Column(name = "SCHEDULE_ID")
	    private BigDecimal scheduleId;

	    @Column(name = "SCHEDULE_NAME")
	    private String scheduleName;

	    @Column(name = "FREQUENCY")
	    private String frequency;

	    @Column(name = "DAY_DESC")
	    private String dayDesc;

	    @Column(name = "RUN_TIME")
	    private String runTime;

	    @Column(name = "START_DATE")
	    private String startDate;

	    @Column(name = "OUTPUT_FORMAT")
	    private String outputFormat;

	    @Column(name = "STATUS")
	    private String status;

	    @Column(name = "RECIPIENTS")
	    private String recipients;

	    @Column(name = "CUSTOMERID")
	    private String customerId;

	    @Column(name = "CUSTOMER_NAME")
	    private String customerName;

	    @Column(name = "CUSTOMER_EMAILID")
	    private String customerEmailId;

	    @Column(name = "ACCOUNT_NUMBER")
	    private String accountNumber;

	    @Column(name = "ACCOUNT_TYPE")
	    private String accountType;

	    @Column(name = "ACID")
	    private String acid;

	    @Column(name = "CURRENCY")
	    private String currency;

	    @Column(name = "CREATE_USER")
	    private String createUser;

	    @Column(name = "CREATE_TIME")
	    private Date createTime;

	    @Column(name = "MODIFY_USER")
	    private String modifyUser;

	    @Column(name = "MODIFY_TIME")
	    private Date modifyTime;

	    @Column(name = "VERIFY_USER")
	    private String verifyUser;

	    @Column(name = "VERIFY_TIME")
	    private Date verifyTime;

	    @Column(name = "ENTITY_FLG")
	    private String entityFlg;

	    @Column(name = "MODIFY_FLG")
	    private String modifyFlg;

	    @Column(name = "DEL_FLG")
	    private String delFlg;

		public BigDecimal getId() {
			return id;
		}

		public void setId(BigDecimal id) {
			this.id = id;
		}

		public BigDecimal getScheduleId() {
			return scheduleId;
		}

		public void setScheduleId(BigDecimal scheduleId) {
			this.scheduleId = scheduleId;
		}

		public String getScheduleName() {
			return scheduleName;
		}

		public void setScheduleName(String scheduleName) {
			this.scheduleName = scheduleName;
		}

		public String getFrequency() {
			return frequency;
		}

		public void setFrequency(String frequency) {
			this.frequency = frequency;
		}

		public String getDayDesc() {
			return dayDesc;
		}

		public void setDayDesc(String dayDesc) {
			this.dayDesc = dayDesc;
		}

		public String getRunTime() {
			return runTime;
		}

		public void setRunTime(String runTime) {
			this.runTime = runTime;
		}

		public String getStartDate() {
			return startDate;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public String getOutputFormat() {
			return outputFormat;
		}

		public void setOutputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getRecipients() {
			return recipients;
		}

		public void setRecipients(String recipients) {
			this.recipients = recipients;
		}

		public String getCustomerId() {
			return customerId;
		}

		public void setCustomerId(String customerId) {
			this.customerId = customerId;
		}

		public String getCustomerName() {
			return customerName;
		}

		public void setCustomerName(String customerName) {
			this.customerName = customerName;
		}

		public String getCustomerEmailId() {
			return customerEmailId;
		}

		public void setCustomerEmailId(String customerEmailId) {
			this.customerEmailId = customerEmailId;
		}

		public String getAccountNumber() {
			return accountNumber;
		}

		public void setAccountNumber(String accountNumber) {
			this.accountNumber = accountNumber;
		}

		public String getAccountType() {
			return accountType;
		}

		public void setAccountType(String accountType) {
			this.accountType = accountType;
		}

		public String getAcid() {
			return acid;
		}

		public void setAcid(String acid) {
			this.acid = acid;
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

		public String getCreateUser() {
			return createUser;
		}

		public void setCreateUser(String createUser) {
			this.createUser = createUser;
		}

		public Date getCreateTime() {
			return createTime;
		}

		public void setCreateTime(Date createTime) {
			this.createTime = createTime;
		}

		public String getModifyUser() {
			return modifyUser;
		}

		public void setModifyUser(String modifyUser) {
			this.modifyUser = modifyUser;
		}

		public Date getModifyTime() {
			return modifyTime;
		}

		public void setModifyTime(Date modifyTime) {
			this.modifyTime = modifyTime;
		}

		public String getVerifyUser() {
			return verifyUser;
		}

		public void setVerifyUser(String verifyUser) {
			this.verifyUser = verifyUser;
		}

		public Date getVerifyTime() {
			return verifyTime;
		}

		public void setVerifyTime(Date verifyTime) {
			this.verifyTime = verifyTime;
		}

		public String getEntityFlg() {
			return entityFlg;
		}

		public void setEntityFlg(String entityFlg) {
			this.entityFlg = entityFlg;
		}

		public String getModifyFlg() {
			return modifyFlg;
		}

		public void setModifyFlg(String modifyFlg) {
			this.modifyFlg = modifyFlg;
		}

		public String getDelFlg() {
			return delFlg;
		}

		public void setDelFlg(String delFlg) {
			this.delFlg = delFlg;
		}

		public ScheduledStatement_Entity(BigDecimal id, BigDecimal scheduleId, String scheduleName, String frequency,
				String dayDesc, String runTime, String startDate, String outputFormat, String status, String recipients,
				String customerId, String customerName, String customerEmailId, String accountNumber,
				String accountType, String acid, String currency, String createUser, Date createTime, String modifyUser,
				Date modifyTime, String verifyUser, Date verifyTime, String entityFlg, String modifyFlg,
				String delFlg) {
			super();
			this.id = id;
			this.scheduleId = scheduleId;
			this.scheduleName = scheduleName;
			this.frequency = frequency;
			this.dayDesc = dayDesc;
			this.runTime = runTime;
			this.startDate = startDate;
			this.outputFormat = outputFormat;
			this.status = status;
			this.recipients = recipients;
			this.customerId = customerId;
			this.customerName = customerName;
			this.customerEmailId = customerEmailId;
			this.accountNumber = accountNumber;
			this.accountType = accountType;
			this.acid = acid;
			this.currency = currency;
			this.createUser = createUser;
			this.createTime = createTime;
			this.modifyUser = modifyUser;
			this.modifyTime = modifyTime;
			this.verifyUser = verifyUser;
			this.verifyTime = verifyTime;
			this.entityFlg = entityFlg;
			this.modifyFlg = modifyFlg;
			this.delFlg = delFlg;
		}

		public ScheduledStatement_Entity() {
			super();
			// TODO Auto-generated constructor stub
		}
	    
	    

	

}