package com.bornfire.AccountStatement.entities;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;




public interface GeneralMasterTbRep extends CrudRepository<GeneralMasterTbEntity,String> {

	@Query(value = "select * from GENERAL_MASTER_TB where schm_type<>'OAB' and cust_id=?1 order by Acct_number DESC", nativeQuery = true)
	List<GeneralMasterTbEntity> findAllCustom(String cust_id);
	
	@Query(value = "select * from GENERAL_MASTER_TB where schm_type<>'OAB'  and Acid =?1 order by Acct_number DESC", nativeQuery = true)
	GeneralMasterTbEntity findAllCustomind(String account);
}
