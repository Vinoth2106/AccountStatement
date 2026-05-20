package com.bornfire.AccountStatement.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="CUST_TABLE")
public class Cust_table_entity {
	
	private String	phone_cell;
	@Id
	private String	orgkey;
	private String	cust_last_name;
	private String	salutation;
	private String	gender;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	cust_dob;
	private String	preferredemail;
	private String	uniqueid;
	private String	country;
	private String address1;
	private String address2;
	private String address3;
	private String cust_type_code;
	public String getPhone_cell() {
		return phone_cell;
	}
	public void setPhone_cell(String phone_cell) {
		this.phone_cell = phone_cell;
	}
	public String getOrgkey() {
		return orgkey;
	}
	public void setOrgkey(String orgkey) {
		this.orgkey = orgkey;
	}
	public String getCust_last_name() {
		return cust_last_name;
	}
	public void setCust_last_name(String cust_last_name) {
		this.cust_last_name = cust_last_name;
	}
	public String getSalutation() {
		return salutation;
	}
	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public Date getCust_dob() {
		return cust_dob;
	}
	public void setCust_dob(Date cust_dob) {
		this.cust_dob = cust_dob;
	}
	public String getPreferredemail() {
		return preferredemail;
	}
	public void setPreferredemail(String preferredemail) {
		this.preferredemail = preferredemail;
	}
	public String getUniqueid() {
		return uniqueid;
	}
	public void setUniqueid(String uniqueid) {
		this.uniqueid = uniqueid;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public String getAddress2() {
		return address2;
	}
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	public String getAddress3() {
		return address3;
	}
	public void setAddress3(String address3) {
		this.address3 = address3;
	}
	public String getCust_type_code() {
		return cust_type_code;
	}
	public void setCust_type_code(String cust_type_code) {
		this.cust_type_code = cust_type_code;
	}
	public Cust_table_entity(String phone_cell, String orgkey, String cust_last_name, String salutation, String gender,
			Date cust_dob, String preferredemail, String uniqueid, String country, String address1, String address2,
			String address3, String cust_type_code) {
		super();
		this.phone_cell = phone_cell;
		this.orgkey = orgkey;
		this.cust_last_name = cust_last_name;
		this.salutation = salutation;
		this.gender = gender;
		this.cust_dob = cust_dob;
		this.preferredemail = preferredemail;
		this.uniqueid = uniqueid;
		this.country = country;
		this.address1 = address1;
		this.address2 = address2;
		this.address3 = address3;
		this.cust_type_code = cust_type_code;
	}
	public Cust_table_entity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
