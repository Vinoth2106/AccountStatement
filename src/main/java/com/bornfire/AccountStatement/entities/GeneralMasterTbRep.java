package com.bornfire.AccountStatement.entities;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;




public interface GeneralMasterTbRep extends CrudRepository<GeneralMasterTbEntity,String> {

	@Query(value = "select * from GENERAL_MASTER_TB where schm_type<>'OAB' and cust_id=?1 order by Acct_number DESC", nativeQuery = true)
	List<GeneralMasterTbEntity> findAllCustom(String cust_id);
	
	@Query(value = "select * from GENERAL_MASTER_TB where Acct_number=?1 order by Acct_number DESC", nativeQuery = true)
	List<GeneralMasterTbEntity> findbyAccountnum(String Acct_number);
	
	@Query(value = "select DISTINCT schm_type from GENERAL_MASTER_TB order by schm_type ", nativeQuery = true)
	List<String> getschmtype();
	
	@Query(value = "select a.CUST_ID,a.ACCT_NAME,b.PREFERREDEMAIL,a.ACCT_NUMBER,a.schm_type,a.acid,a.ACCT_CRNCY_CODE,a.ACCT_BALANCE_AMT_AC from GENERAL_MASTER_TB a left join CUST_TABLE b on a.CUST_ID=b.ORGKEY where (b.CUST_TYPE_CODE=?1 or b.ORGKEY=?1 or a.schm_type=?1) ", nativeQuery = true)
	List<Object> findAllCustombytype(String filterValue);
	
	
	@Query(value = "select * from GENERAL_MASTER_TB where schm_type<>'OAB'  and Acid =?1 order by Acct_number DESC", nativeQuery = true)
	GeneralMasterTbEntity findAllCustomind(String account);
	
	@Query(value =
		       "select * from general_master_tb where cust_id in (:custIds)",
		       nativeQuery = true)
		List<GeneralMasterTbEntity> findByCustIds(
		        @Param("custIds") List<String> custIds);
	
	@Query(value = "SELECT nvl(SUM(ACCT_BALANCE_AMT_AC),0) FROM GENERAL_MASTER_TB WHERE Acct_number = ?1 AND report_date BETWEEN ?2 AND ?3",
		       nativeQuery = true)
		BigDecimal getSumBalanceBetweenDates(String Accountnum, String fd, String td);
	
	
	@Query(value = "select * from GENERAL_MASTER_TB where ACCT_NUMBER=?1",
	       nativeQuery = true)
	GeneralMasterTbEntity findByAcctNumber(String acct_number);
	
	@Query(value = "SELECT * FROM GENERAL_MASTER_TB WHERE ACID = ?1 AND ROWNUM = 1",
		       nativeQuery = true)
		GeneralMasterTbEntity findByAcctid(String acid);
	
	@Query(value =
		    "select schm_type from GENERAL_MASTER_TB where acid = ?1",
		    nativeQuery = true)
		String getSchemeTypeByAcid(String acid);
	

}
