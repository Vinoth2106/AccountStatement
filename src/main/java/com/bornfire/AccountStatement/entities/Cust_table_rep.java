package com.bornfire.AccountStatement.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface Cust_table_rep extends JpaRepository<Cust_table_entity, String> {
	
	@Query(value = "select * from CUST_TABLE", nativeQuery = true)
	List<Cust_table_entity> getcustlist();
	
	@Query(value = "SELECT DISTINCT cust_type_code FROM CUST_TABLE WHERE cust_type_code IS NOT NULL",
		       nativeQuery = true)
		List<String> getDistinctAccountTypes();
	@Query(value =
		       "select orgkey from cust_table where cust_type_code=?1",
		       nativeQuery = true)
		List<String> getCustomerIdsByType(String type);
	
	@Query(value = "select * from CUST_TABLE where CUST_TYPE_CODE = 'retail'", nativeQuery = true)
	List<Cust_table_entity> getRetaillist();
	
	@Query(value = "select * from CUST_TABLE where CUST_TYPE_CODE = 'corporate'", nativeQuery = true)
	List<Cust_table_entity> getCorporatelist();

	long count();
	
	long countByCreatedDateBetween(Date startDate, Date endDate);

}
