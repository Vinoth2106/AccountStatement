package com.bornfire.AccountStatement.entities;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface TransactionInquiryRep extends CrudRepository<TransactionInquiry,String> {
	
	@Query(value = "select * from GENERAL_MASTER_TB where schm_type<>'OAB' order by Acct_number DESC", nativeQuery = true)
	List<TransactionInquiry> findAllCustom();
	
	@Query(value = "select * from HTD where acid =?1 order by tran_date DESC", nativeQuery = true)
	List<TransactionInquiry> findAllCustomind(String account);
	
	@Query(value =
			"SELECT * FROM HTD " +
			"WHERE acid = ?1 " +
			"AND TRUNC(tran_date) BETWEEN " +
			"TO_DATE(?2,'yyyy-MM-dd') " +
			"AND TO_DATE(?3,'yyyy-MM-dd')",
			nativeQuery = true)

			List<TransactionInquiry> findAllCustominddate(
			        String acid,
			        String fromDate,
			        String toDate);
}
