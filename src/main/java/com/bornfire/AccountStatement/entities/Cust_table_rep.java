package com.bornfire.AccountStatement.entities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface Cust_table_rep extends JpaRepository<Cust_table_entity, String> {
	
	@Query(value = "select * from CUST_TABLE", nativeQuery = true)
	List<Cust_table_entity> getcustlist();
	
	

}
