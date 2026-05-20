package com.bornfire.AccountStatement.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
@Entity
@Table(name="GENERAL_MASTER_TB")
public class GeneralMasterTbEntity {
	
	@Id
	private String	acct_number;
	private String	acct_name;
	private String	cust_id;
	private String	schm_code;
	private String	schm_type;
	private String	acct_opn_date;
	private BigDecimal	int_rate;
	private BigDecimal	acct_balance_amt_ac;
	private String	acct_crncy_code;
	private String	isic_code;
	private String	nature_of_cust;
	private String	nre_flg;
	private String	country;
	private String	bom_group_identifier;
	private String	cust_unique_identifier;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	report_date;
	private String	asset_liab_nr_flg;
	private BigDecimal	interest_amount;
	private String	acid;
	private String	deposit_type;
	private String	gl_sub_head_code;
	private String	cust_sub_grp;
	private BigDecimal	accrued_int_cr;
	private BigDecimal	accrued_int_dr;
	private String	sme_flg;
	private String	cust_type_code;
	private BigDecimal	dep_period_mths;
	private BigDecimal	dep_period_days;
	private String	interest_type;
	private String	acct_cls_flg;
	private String	acct_cls_date;
	private String	sol_id;
	private BigDecimal	act_balance_amt_lc;
	private BigDecimal	arrear_amt;
	private String	unsecured_flg;
	private String	staff_flg;
	private String	npa_flg;
	private String	loan_1yr_maturity;
	private BigDecimal	rep_period_mths;
	private BigDecimal	rep_period_days;
	private BigDecimal	sanct_amt;
	private String	restructured_flg;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	renewal_date;
	private BigDecimal	shdl_num;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	rep_shdl_date;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	payoff_date;
	private String	payoff_flg;
	private BigDecimal	dpd_cntr;
	private String	security_type;
	private BigDecimal	non_fund_based_amt;
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date	maturity_date;
	private BigDecimal	accr_bal_ac;
	private BigDecimal	accr_bal_lc;
	private BigDecimal	sme_turn_over;
	private String	cust_type_code_2;
	public String getAcct_number() {
		return acct_number;
	}
	public void setAcct_number(String acct_number) {
		this.acct_number = acct_number;
	}
	public String getAcct_name() {
		return acct_name;
	}
	public void setAcct_name(String acct_name) {
		this.acct_name = acct_name;
	}
	public String getCust_id() {
		return cust_id;
	}
	public void setCust_id(String cust_id) {
		this.cust_id = cust_id;
	}
	public String getSchm_code() {
		return schm_code;
	}
	public void setSchm_code(String schm_code) {
		this.schm_code = schm_code;
	}
	public String getSchm_type() {
		return schm_type;
	}
	public void setSchm_type(String schm_type) {
		this.schm_type = schm_type;
	}
	public String getAcct_opn_date() {
		return acct_opn_date;
	}
	public void setAcct_opn_date(String acct_opn_date) {
		this.acct_opn_date = acct_opn_date;
	}
	public BigDecimal getInt_rate() {
		return int_rate;
	}
	public void setInt_rate(BigDecimal int_rate) {
		this.int_rate = int_rate;
	}
	public BigDecimal getAcct_balance_amt_ac() {
		return acct_balance_amt_ac;
	}
	public void setAcct_balance_amt_ac(BigDecimal acct_balance_amt_ac) {
		this.acct_balance_amt_ac = acct_balance_amt_ac;
	}
	public String getAcct_crncy_code() {
		return acct_crncy_code;
	}
	public void setAcct_crncy_code(String acct_crncy_code) {
		this.acct_crncy_code = acct_crncy_code;
	}
	public String getIsic_code() {
		return isic_code;
	}
	public void setIsic_code(String isic_code) {
		this.isic_code = isic_code;
	}
	public String getNature_of_cust() {
		return nature_of_cust;
	}
	public void setNature_of_cust(String nature_of_cust) {
		this.nature_of_cust = nature_of_cust;
	}
	public String getNre_flg() {
		return nre_flg;
	}
	public void setNre_flg(String nre_flg) {
		this.nre_flg = nre_flg;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getBom_group_identifier() {
		return bom_group_identifier;
	}
	public void setBom_group_identifier(String bom_group_identifier) {
		this.bom_group_identifier = bom_group_identifier;
	}
	public String getCust_unique_identifier() {
		return cust_unique_identifier;
	}
	public void setCust_unique_identifier(String cust_unique_identifier) {
		this.cust_unique_identifier = cust_unique_identifier;
	}
	public Date getReport_date() {
		return report_date;
	}
	public void setReport_date(Date report_date) {
		this.report_date = report_date;
	}
	public String getAsset_liab_nr_flg() {
		return asset_liab_nr_flg;
	}
	public void setAsset_liab_nr_flg(String asset_liab_nr_flg) {
		this.asset_liab_nr_flg = asset_liab_nr_flg;
	}
	public BigDecimal getInterest_amount() {
		return interest_amount;
	}
	public void setInterest_amount(BigDecimal interest_amount) {
		this.interest_amount = interest_amount;
	}
	public String getAcid() {
		return acid;
	}
	public void setAcid(String acid) {
		this.acid = acid;
	}
	public String getDeposit_type() {
		return deposit_type;
	}
	public void setDeposit_type(String deposit_type) {
		this.deposit_type = deposit_type;
	}
	public String getGl_sub_head_code() {
		return gl_sub_head_code;
	}
	public void setGl_sub_head_code(String gl_sub_head_code) {
		this.gl_sub_head_code = gl_sub_head_code;
	}
	public String getCust_sub_grp() {
		return cust_sub_grp;
	}
	public void setCust_sub_grp(String cust_sub_grp) {
		this.cust_sub_grp = cust_sub_grp;
	}
	public BigDecimal getAccrued_int_cr() {
		return accrued_int_cr;
	}
	public void setAccrued_int_cr(BigDecimal accrued_int_cr) {
		this.accrued_int_cr = accrued_int_cr;
	}
	public BigDecimal getAccrued_int_dr() {
		return accrued_int_dr;
	}
	public void setAccrued_int_dr(BigDecimal accrued_int_dr) {
		this.accrued_int_dr = accrued_int_dr;
	}
	public String getSme_flg() {
		return sme_flg;
	}
	public void setSme_flg(String sme_flg) {
		this.sme_flg = sme_flg;
	}
	public String getCust_type_code() {
		return cust_type_code;
	}
	public void setCust_type_code(String cust_type_code) {
		this.cust_type_code = cust_type_code;
	}
	public BigDecimal getDep_period_mths() {
		return dep_period_mths;
	}
	public void setDep_period_mths(BigDecimal dep_period_mths) {
		this.dep_period_mths = dep_period_mths;
	}
	public BigDecimal getDep_period_days() {
		return dep_period_days;
	}
	public void setDep_period_days(BigDecimal dep_period_days) {
		this.dep_period_days = dep_period_days;
	}
	public String getInterest_type() {
		return interest_type;
	}
	public void setInterest_type(String interest_type) {
		this.interest_type = interest_type;
	}
	public String getAcct_cls_flg() {
		return acct_cls_flg;
	}
	public void setAcct_cls_flg(String acct_cls_flg) {
		this.acct_cls_flg = acct_cls_flg;
	}
	public String getAcct_cls_date() {
		return acct_cls_date;
	}
	public void setAcct_cls_date(String acct_cls_date) {
		this.acct_cls_date = acct_cls_date;
	}
	public String getSol_id() {
		return sol_id;
	}
	public void setSol_id(String sol_id) {
		this.sol_id = sol_id;
	}
	public BigDecimal getAct_balance_amt_lc() {
		return act_balance_amt_lc;
	}
	public void setAct_balance_amt_lc(BigDecimal act_balance_amt_lc) {
		this.act_balance_amt_lc = act_balance_amt_lc;
	}
	public BigDecimal getArrear_amt() {
		return arrear_amt;
	}
	public void setArrear_amt(BigDecimal arrear_amt) {
		this.arrear_amt = arrear_amt;
	}
	public String getUnsecured_flg() {
		return unsecured_flg;
	}
	public void setUnsecured_flg(String unsecured_flg) {
		this.unsecured_flg = unsecured_flg;
	}
	public String getStaff_flg() {
		return staff_flg;
	}
	public void setStaff_flg(String staff_flg) {
		this.staff_flg = staff_flg;
	}
	public String getNpa_flg() {
		return npa_flg;
	}
	public void setNpa_flg(String npa_flg) {
		this.npa_flg = npa_flg;
	}
	public String getLoan_1yr_maturity() {
		return loan_1yr_maturity;
	}
	public void setLoan_1yr_maturity(String loan_1yr_maturity) {
		this.loan_1yr_maturity = loan_1yr_maturity;
	}
	public BigDecimal getRep_period_mths() {
		return rep_period_mths;
	}
	public void setRep_period_mths(BigDecimal rep_period_mths) {
		this.rep_period_mths = rep_period_mths;
	}
	public BigDecimal getRep_period_days() {
		return rep_period_days;
	}
	public void setRep_period_days(BigDecimal rep_period_days) {
		this.rep_period_days = rep_period_days;
	}
	public BigDecimal getSanct_amt() {
		return sanct_amt;
	}
	public void setSanct_amt(BigDecimal sanct_amt) {
		this.sanct_amt = sanct_amt;
	}
	public String getRestructured_flg() {
		return restructured_flg;
	}
	public void setRestructured_flg(String restructured_flg) {
		this.restructured_flg = restructured_flg;
	}
	public Date getRenewal_date() {
		return renewal_date;
	}
	public void setRenewal_date(Date renewal_date) {
		this.renewal_date = renewal_date;
	}
	public BigDecimal getShdl_num() {
		return shdl_num;
	}
	public void setShdl_num(BigDecimal shdl_num) {
		this.shdl_num = shdl_num;
	}
	public Date getRep_shdl_date() {
		return rep_shdl_date;
	}
	public void setRep_shdl_date(Date rep_shdl_date) {
		this.rep_shdl_date = rep_shdl_date;
	}
	public Date getPayoff_date() {
		return payoff_date;
	}
	public void setPayoff_date(Date payoff_date) {
		this.payoff_date = payoff_date;
	}
	public String getPayoff_flg() {
		return payoff_flg;
	}
	public void setPayoff_flg(String payoff_flg) {
		this.payoff_flg = payoff_flg;
	}
	public BigDecimal getDpd_cntr() {
		return dpd_cntr;
	}
	public void setDpd_cntr(BigDecimal dpd_cntr) {
		this.dpd_cntr = dpd_cntr;
	}
	public String getSecurity_type() {
		return security_type;
	}
	public void setSecurity_type(String security_type) {
		this.security_type = security_type;
	}
	public BigDecimal getNon_fund_based_amt() {
		return non_fund_based_amt;
	}
	public void setNon_fund_based_amt(BigDecimal non_fund_based_amt) {
		this.non_fund_based_amt = non_fund_based_amt;
	}
	public Date getMaturity_date() {
		return maturity_date;
	}
	public void setMaturity_date(Date maturity_date) {
		this.maturity_date = maturity_date;
	}
	public BigDecimal getAccr_bal_ac() {
		return accr_bal_ac;
	}
	public void setAccr_bal_ac(BigDecimal accr_bal_ac) {
		this.accr_bal_ac = accr_bal_ac;
	}
	public BigDecimal getAccr_bal_lc() {
		return accr_bal_lc;
	}
	public void setAccr_bal_lc(BigDecimal accr_bal_lc) {
		this.accr_bal_lc = accr_bal_lc;
	}
	public BigDecimal getSme_turn_over() {
		return sme_turn_over;
	}
	public void setSme_turn_over(BigDecimal sme_turn_over) {
		this.sme_turn_over = sme_turn_over;
	}
	public String getCust_type_code_2() {
		return cust_type_code_2;
	}
	public void setCust_type_code_2(String cust_type_code_2) {
		this.cust_type_code_2 = cust_type_code_2;
	}
	public GeneralMasterTbEntity(String acct_number, String acct_name, String cust_id, String schm_code,
			String schm_type, String acct_opn_date, BigDecimal int_rate, BigDecimal acct_balance_amt_ac,
			String acct_crncy_code, String isic_code, String nature_of_cust, String nre_flg, String country,
			String bom_group_identifier, String cust_unique_identifier, Date report_date, String asset_liab_nr_flg,
			BigDecimal interest_amount, String acid, String deposit_type, String gl_sub_head_code, String cust_sub_grp,
			BigDecimal accrued_int_cr, BigDecimal accrued_int_dr, String sme_flg, String cust_type_code,
			BigDecimal dep_period_mths, BigDecimal dep_period_days, String interest_type, String acct_cls_flg,
			String acct_cls_date, String sol_id, BigDecimal act_balance_amt_lc, BigDecimal arrear_amt,
			String unsecured_flg, String staff_flg, String npa_flg, String loan_1yr_maturity,
			BigDecimal rep_period_mths, BigDecimal rep_period_days, BigDecimal sanct_amt, String restructured_flg,
			Date renewal_date, BigDecimal shdl_num, Date rep_shdl_date, Date payoff_date, String payoff_flg,
			BigDecimal dpd_cntr, String security_type, BigDecimal non_fund_based_amt, Date maturity_date,
			BigDecimal accr_bal_ac, BigDecimal accr_bal_lc, BigDecimal sme_turn_over, String cust_type_code_2) {
		super();
		this.acct_number = acct_number;
		this.acct_name = acct_name;
		this.cust_id = cust_id;
		this.schm_code = schm_code;
		this.schm_type = schm_type;
		this.acct_opn_date = acct_opn_date;
		this.int_rate = int_rate;
		this.acct_balance_amt_ac = acct_balance_amt_ac;
		this.acct_crncy_code = acct_crncy_code;
		this.isic_code = isic_code;
		this.nature_of_cust = nature_of_cust;
		this.nre_flg = nre_flg;
		this.country = country;
		this.bom_group_identifier = bom_group_identifier;
		this.cust_unique_identifier = cust_unique_identifier;
		this.report_date = report_date;
		this.asset_liab_nr_flg = asset_liab_nr_flg;
		this.interest_amount = interest_amount;
		this.acid = acid;
		this.deposit_type = deposit_type;
		this.gl_sub_head_code = gl_sub_head_code;
		this.cust_sub_grp = cust_sub_grp;
		this.accrued_int_cr = accrued_int_cr;
		this.accrued_int_dr = accrued_int_dr;
		this.sme_flg = sme_flg;
		this.cust_type_code = cust_type_code;
		this.dep_period_mths = dep_period_mths;
		this.dep_period_days = dep_period_days;
		this.interest_type = interest_type;
		this.acct_cls_flg = acct_cls_flg;
		this.acct_cls_date = acct_cls_date;
		this.sol_id = sol_id;
		this.act_balance_amt_lc = act_balance_amt_lc;
		this.arrear_amt = arrear_amt;
		this.unsecured_flg = unsecured_flg;
		this.staff_flg = staff_flg;
		this.npa_flg = npa_flg;
		this.loan_1yr_maturity = loan_1yr_maturity;
		this.rep_period_mths = rep_period_mths;
		this.rep_period_days = rep_period_days;
		this.sanct_amt = sanct_amt;
		this.restructured_flg = restructured_flg;
		this.renewal_date = renewal_date;
		this.shdl_num = shdl_num;
		this.rep_shdl_date = rep_shdl_date;
		this.payoff_date = payoff_date;
		this.payoff_flg = payoff_flg;
		this.dpd_cntr = dpd_cntr;
		this.security_type = security_type;
		this.non_fund_based_amt = non_fund_based_amt;
		this.maturity_date = maturity_date;
		this.accr_bal_ac = accr_bal_ac;
		this.accr_bal_lc = accr_bal_lc;
		this.sme_turn_over = sme_turn_over;
		this.cust_type_code_2 = cust_type_code_2;
	}
	public GeneralMasterTbEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

}
