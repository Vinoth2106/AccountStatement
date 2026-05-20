package com.bornfire.AccountStatement.entities;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface TransactionInquiryRep extends CrudRepository<TransactionInquiry,String> {
	
	@Query(value = "select * from GENERAL_MASTER_TB where schm_type<>'OAB' order by Acct_number DESC", nativeQuery = true)
	List<TransactionInquiry> findAllCustom();
	
	@Query(value = "select * from HTD where acid =?1 order by tran_date DESC", nativeQuery = true)
	List<TransactionInquiry> findAllCustomind(String account);
	
	@Query(value = "select * from HTD where acid =?1  and tran_date between ?2 and ?3 order by tran_date DESC", nativeQuery = true)
	List<TransactionInquiry> findAllCustominddate(String account,String fd,String td);

}
