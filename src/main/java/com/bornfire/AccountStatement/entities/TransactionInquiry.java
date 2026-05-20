package com.bornfire.AccountStatement.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name="HTD")
public class TransactionInquiry {
	

	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	tran_date;
	private String	tran_id;
	@Id
	private String	part_tran_srl_num;
	private String	del_flg;
	private String	tran_type;
	private String	tran_sub_type;
	private String	part_tran_type;
	private String	gl_sub_head_code;
	private String	acid;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	value_date;
	private BigDecimal	tran_amt;
	private String	tran_particular;
	private String	tran_rmks;
	private String	tran_crncy_code;
	public Date getTran_date() {
		return tran_date;
	}
	public void setTran_date(Date tran_date) {
		this.tran_date = tran_date;
	}
	public String getTran_id() {
		return tran_id;
	}
	public void setTran_id(String tran_id) {
		this.tran_id = tran_id;
	}
	public String getPart_tran_srl_num() {
		return part_tran_srl_num;
	}
	public void setPart_tran_srl_num(String part_tran_srl_num) {
		this.part_tran_srl_num = part_tran_srl_num;
	}
	public String getDel_flg() {
		return del_flg;
	}
	public void setDel_flg(String del_flg) {
		this.del_flg = del_flg;
	}
	public String getTran_type() {
		return tran_type;
	}
	public void setTran_type(String tran_type) {
		this.tran_type = tran_type;
	}
	public String getTran_sub_type() {
		return tran_sub_type;
	}
	public void setTran_sub_type(String tran_sub_type) {
		this.tran_sub_type = tran_sub_type;
	}
	public String getPart_tran_type() {
		return part_tran_type;
	}
	public void setPart_tran_type(String part_tran_type) {
		this.part_tran_type = part_tran_type;
	}
	public String getGl_sub_head_code() {
		return gl_sub_head_code;
	}
	public void setGl_sub_head_code(String gl_sub_head_code) {
		this.gl_sub_head_code = gl_sub_head_code;
	}
	public String getAcid() {
		return acid;
	}
	public void setAcid(String acid) {
		this.acid = acid;
	}
	public Date getValue_date() {
		return value_date;
	}
	public void setValue_date(Date value_date) {
		this.value_date = value_date;
	}
	public BigDecimal getTran_amt() {
		return tran_amt;
	}
	public void setTran_amt(BigDecimal tran_amt) {
		this.tran_amt = tran_amt;
	}
	public String getTran_particular() {
		return tran_particular;
	}
	public void setTran_particular(String tran_particular) {
		this.tran_particular = tran_particular;
	}
	public String getTran_rmks() {
		return tran_rmks;
	}
	public void setTran_rmks(String tran_rmks) {
		this.tran_rmks = tran_rmks;
	}
	public String getTran_crncy_code() {
		return tran_crncy_code;
	}
	public void setTran_crncy_code(String tran_crncy_code) {
		this.tran_crncy_code = tran_crncy_code;
	}
	public TransactionInquiry(Date tran_date, String tran_id, String part_tran_srl_num, String del_flg,
			String tran_type, String tran_sub_type, String part_tran_type, String gl_sub_head_code, String acid,
			Date value_date, BigDecimal tran_amt, String tran_particular, String tran_rmks, String tran_crncy_code) {
		super();
		this.tran_date = tran_date;
		this.tran_id = tran_id;
		this.part_tran_srl_num = part_tran_srl_num;
		this.del_flg = del_flg;
		this.tran_type = tran_type;
		this.tran_sub_type = tran_sub_type;
		this.part_tran_type = part_tran_type;
		this.gl_sub_head_code = gl_sub_head_code;
		this.acid = acid;
		this.value_date = value_date;
		this.tran_amt = tran_amt;
		this.tran_particular = tran_particular;
		this.tran_rmks = tran_rmks;
		this.tran_crncy_code = tran_crncy_code;
	}
	public TransactionInquiry() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
