package com.bornfire.AccountStatement.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "RR_RPT_MAST")
public class RRReport {


	@Id
	@Column(name = "srl_no")
	private int	srl_no;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date	rpt_start_date;
	public String	rpt_code;
	private String	rpt_description;
	private String	rpt_type;
	private String	det_flg;
	private String	rpt_status;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date	start_date;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date	end_date;
	private String	remarks_1;
	private String	remarks_2;
	private String	remarks_3;
	private String	remarks_4;
	private String	remarks_5;
	private String	del_flg;
	private String	entity_flg;
	private String	modify_flg;
	private String	entry_user;
	private String	modify_user;
	private String	verify_suer;
	private Date	entry_date;
	private Date	modify_date;
	private Date	verify_date;
	private String TAXONOMY_VERSION;
	private Date TAXONOMY_DATE;
	
	@Column(name="DOMAIN")
	private String DOMAIN;

	public int getSrl_no() {
		return srl_no;
	}

	public void setSrl_no(int srl_no) {
		this.srl_no = srl_no;
	}

	public Date getRpt_start_date() {
		return rpt_start_date;
	}

	public void setRpt_start_date(Date rpt_start_date) {
		this.rpt_start_date = rpt_start_date;
	}

	public String getRpt_code() {
		return rpt_code;
	}

	public void setRpt_code(String rpt_code) {
		this.rpt_code = rpt_code;
	}

	public String getRpt_description() {
		return rpt_description;
	}

	public void setRpt_description(String rpt_description) {
		this.rpt_description = rpt_description;
	}

	public String getRpt_type() {
		return rpt_type;
	}

	public void setRpt_type(String rpt_type) {
		this.rpt_type = rpt_type;
	}

	public String getDet_flg() {
		return det_flg;
	}

	public void setDet_flg(String det_flg) {
		this.det_flg = det_flg;
	}

	public String getRpt_status() {
		return rpt_status;
	}

	public void setRpt_status(String rpt_status) {
		this.rpt_status = rpt_status;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public String getRemarks_1() {
		return remarks_1;
	}

	public void setRemarks_1(String remarks_1) {
		this.remarks_1 = remarks_1;
	}

	public String getRemarks_2() {
		return remarks_2;
	}

	public void setRemarks_2(String remarks_2) {
		this.remarks_2 = remarks_2;
	}

	public String getRemarks_3() {
		return remarks_3;
	}

	public void setRemarks_3(String remarks_3) {
		this.remarks_3 = remarks_3;
	}

	public String getRemarks_4() {
		return remarks_4;
	}

	public void setRemarks_4(String remarks_4) {
		this.remarks_4 = remarks_4;
	}

	public String getRemarks_5() {
		return remarks_5;
	}

	public void setRemarks_5(String remarks_5) {
		this.remarks_5 = remarks_5;
	}

	public String getDel_flg() {
		return del_flg;
	}

	public void setDel_flg(String del_flg) {
		this.del_flg = del_flg;
	}

	public String getEntity_flg() {
		return entity_flg;
	}

	public void setEntity_flg(String entity_flg) {
		this.entity_flg = entity_flg;
	}

	public String getModify_flg() {
		return modify_flg;
	}

	public void setModify_flg(String modify_flg) {
		this.modify_flg = modify_flg;
	}

	public String getEntry_user() {
		return entry_user;
	}

	public void setEntry_user(String entry_user) {
		this.entry_user = entry_user;
	}

	public String getModify_user() {
		return modify_user;
	}

	public void setModify_user(String modify_user) {
		this.modify_user = modify_user;
	}

	public String getVerify_suer() {
		return verify_suer;
	}

	public void setVerify_suer(String verify_suer) {
		this.verify_suer = verify_suer;
	}

	public Date getEntry_date() {
		return entry_date;
	}

	public void setEntry_date(Date entry_date) {
		this.entry_date = entry_date;
	}

	public Date getModify_date() {
		return modify_date;
	}

	public void setModify_date(Date modify_date) {
		this.modify_date = modify_date;
	}

	public Date getVerify_date() {
		return verify_date;
	}

	public void setVerify_date(Date verify_date) {
		this.verify_date = verify_date;
	}

	public String getTAXONOMY_VERSION() {
		return TAXONOMY_VERSION;
	}

	public void setTAXONOMY_VERSION(String tAXONOMY_VERSION) {
		TAXONOMY_VERSION = tAXONOMY_VERSION;
	}

	public Date getTAXONOMY_DATE() {
		return TAXONOMY_DATE;
	}

	public void setTAXONOMY_DATE(Date tAXONOMY_DATE) {
		TAXONOMY_DATE = tAXONOMY_DATE;
	}

	public String getDOMAIN() {
		return DOMAIN;
	}

	public void setDOMAIN(String dOMAIN) {
		DOMAIN = dOMAIN;
	}

	public RRReport(int srl_no, Date rpt_start_date, String rpt_code, String rpt_description, String rpt_type,
			String det_flg, String rpt_status, Date start_date, Date end_date, String remarks_1, String remarks_2,
			String remarks_3, String remarks_4, String remarks_5, String del_flg, String entity_flg, String modify_flg,
			String entry_user, String modify_user, String verify_suer, Date entry_date, Date modify_date,
			Date verify_date, String tAXONOMY_VERSION, Date tAXONOMY_DATE, String dOMAIN) {
		super();
		this.srl_no = srl_no;
		this.rpt_start_date = rpt_start_date;
		this.rpt_code = rpt_code;
		this.rpt_description = rpt_description;
		this.rpt_type = rpt_type;
		this.det_flg = det_flg;
		this.rpt_status = rpt_status;
		this.start_date = start_date;
		this.end_date = end_date;
		this.remarks_1 = remarks_1;
		this.remarks_2 = remarks_2;
		this.remarks_3 = remarks_3;
		this.remarks_4 = remarks_4;
		this.remarks_5 = remarks_5;
		this.del_flg = del_flg;
		this.entity_flg = entity_flg;
		this.modify_flg = modify_flg;
		this.entry_user = entry_user;
		this.modify_user = modify_user;
		this.verify_suer = verify_suer;
		this.entry_date = entry_date;
		this.modify_date = modify_date;
		this.verify_date = verify_date;
		TAXONOMY_VERSION = tAXONOMY_VERSION;
		TAXONOMY_DATE = tAXONOMY_DATE;
		DOMAIN = dOMAIN;
	}

	public RRReport() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
