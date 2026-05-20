package com.bornfire.AccountStatement.entities;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;




public interface GeneralMasterTbRep extends CrudRepository<GeneralMasterTbEntity,String> {

	@Query(value = "select * from GENERAL_MASTER_TB where schm_type<>'OAB' and cust_id=?1 order by Acct_number DESC", nativeQuery = true)
	List<GeneralMasterTbEntity> findAllCustom(String cust_id);
	
	@Query(value = "select a.CUST_ID,a.ACCT_NAME,a.ACCT_NUMBER,b.CUST_TYPE_CODE,a.ACCT_CRNCY_CODE from GENERAL_MASTER_TB a left join CUST_TABLE b on a.CUST_ID=b.ORGKEY where b.CUST_TYPE_CODE=?1", nativeQuery = true)
	List<Object> findAllCustombytype(String CUST_TYPE_CODE);
	
	
	@Query(value = "select * from GENERAL_MASTER_TB where schm_type<>'OAB'  and Acid =?1 order by Acct_number DESC", nativeQuery = true)
	GeneralMasterTbEntity findAllCustomind(String account);
	
	@Query(value =
		       "select * from general_master_tb where cust_id in (:custIds)",
		       nativeQuery = true)
		List<GeneralMasterTbEntity> findByCustIds(
		        @Param("custIds") List<String> custIds);
}
