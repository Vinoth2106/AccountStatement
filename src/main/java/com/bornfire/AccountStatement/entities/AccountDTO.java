package com.bornfire.AccountStatement.entities;

public class AccountDTO {
	
	private String customerId;
    private String customerName;
    private String customerEmail;
    private String accountNumber;
    private String accountType;
    private String acid;
    private String currency;
    
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
	
	
	public AccountDTO(String customerId, String customerName, String customerEmail, String accountNumber,
			String accountType, String acid, String currency) {
		super();
		this.customerId = customerId;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.accountNumber = accountNumber;
		this.accountType = accountType;
		this.acid = acid;
		this.currency = currency;
	}
	
	
	public AccountDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
    
    
   

}
