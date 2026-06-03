package com.bornfire.AccountStatement.entities;

public class AccountDTO {
	
	private String customerId;
    private String customerName;
    private String customerEmail;
    private String accountNumber;
    private String accountType;
    private String acid;
    private String currency;
    private String scheduleType;;
    private String scheduleName;
    private String frequency;
    private String scheduleDate;
    private String scheduleTime;
    private String offerAlert;
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
	public String getCustomerEmail() {
		return customerEmail;
	}
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
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
	public String getScheduleType() {
		return scheduleType;
	}
	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
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
	public String getScheduleDate() {
		return scheduleDate;
	}
	public void setScheduleDate(String scheduleDate) {
		this.scheduleDate = scheduleDate;
	}
	public String getScheduleTime() {
		return scheduleTime;
	}
	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}
	public String getOfferAlert() {
		return offerAlert;
	}
	public void setOfferAlert(String offerAlert) {
		this.offerAlert = offerAlert;
	}
	public AccountDTO(String customerId, String customerName, String customerEmail, String accountNumber,
			String accountType, String acid, String currency, String scheduleType, String scheduleName,
			String frequency, String scheduleDate, String scheduleTime, String offerAlert) {
		super();
		this.customerId = customerId;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.accountNumber = accountNumber;
		this.accountType = accountType;
		this.acid = acid;
		this.currency = currency;
		this.scheduleType = scheduleType;
		this.scheduleName = scheduleName;
		this.frequency = frequency;
		this.scheduleDate = scheduleDate;
		this.scheduleTime = scheduleTime;
		this.offerAlert = offerAlert;
	}
	public AccountDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	
    
	
   

}
