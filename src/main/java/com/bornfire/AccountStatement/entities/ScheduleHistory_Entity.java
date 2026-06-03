package com.bornfire.AccountStatement.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "SCHEDULE_HISTORY")
public class ScheduleHistory_Entity {

	@Id
	@Column(name = "ID", precision = 20, scale = 0)
	private BigDecimal id;

	@Column(name = "RUN_ID", precision = 20, scale = 0)
	private BigDecimal runid;	

	@Column(name = "SCHEDULE_ID", precision = 20, scale = 0)
	private BigDecimal scheduleId;

	@Column(name = "DATE_SENT")
	private String dateSent;

	@Column(name = "OUTPUT_FORMAT")
	private String outputFormat;

	@Column(name = "DELIVERY_STATUS")
	private String deliveryStatus;

	@Column(name = "ERROR_REASON")
	private String errorReason;
	
	@Column(name = "TIME_SENT")
	private String timeSent;	

	@Column(name = "SCHEDULE_NAME")
	private String scheduleName;
	
	@Column(name = "IS_RERUN")
    private String isRerun; 
	
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

		public BigDecimal getRunid() {
			return runid;
		}

		public void setRunid(BigDecimal runid) {
			this.runid = runid;
		}

		public BigDecimal getScheduleId() {
			return scheduleId;
		}

		public void setScheduleId(BigDecimal scheduleId) {
			this.scheduleId = scheduleId;
		}

		public String getDateSent() {
			return dateSent;
		}

		public void setDateSent(String dateSent) {
			this.dateSent = dateSent;
		}

		public String getOutputFormat() {
			return outputFormat;
		}

		public void setOutputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
		}

		public String getDeliveryStatus() {
			return deliveryStatus;
		}

		public void setDeliveryStatus(String deliveryStatus) {
			this.deliveryStatus = deliveryStatus;
		}

		public String getErrorReason() {
			return errorReason;
		}

		public void setErrorReason(String errorReason) {
			this.errorReason = errorReason;
		}

		public String getTimeSent() {
			return timeSent;
		}

		public void setTimeSent(String timeSent) {
			this.timeSent = timeSent;
		}

		public String getScheduleName() {
			return scheduleName;
		}

		public void setScheduleName(String scheduleName) {
			this.scheduleName = scheduleName;
		}

		public String getIsRerun() {
			return isRerun;
		}

		public void setIsRerun(String isRerun) {
			this.isRerun = isRerun;
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

		public ScheduleHistory_Entity(BigDecimal id, BigDecimal runid, BigDecimal scheduleId, String dateSent,
				String outputFormat, String deliveryStatus, String errorReason, String timeSent, String scheduleName,
				String isRerun, String customerId, String customerName, String customerEmailId, String accountNumber,
				String accountType, String acid, String currency, String createUser, Date createTime, String modifyUser,
				Date modifyTime, String verifyUser, Date verifyTime, String entityFlg, String modifyFlg,
				String delFlg) {
			super();
			this.id = id;
			this.runid = runid;
			this.scheduleId = scheduleId;
			this.dateSent = dateSent;
			this.outputFormat = outputFormat;
			this.deliveryStatus = deliveryStatus;
			this.errorReason = errorReason;
			this.timeSent = timeSent;
			this.scheduleName = scheduleName;
			this.isRerun = isRerun;
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

		public ScheduleHistory_Entity() {
			super();
			// TODO Auto-generated constructor stub
		}

		
	    
	    

}
