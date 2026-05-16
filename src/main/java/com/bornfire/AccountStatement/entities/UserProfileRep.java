package com.bornfire.AccountStatement.entities;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRep extends CrudRepository<UserProfile,String> {
	
public Optional<UserProfile> findByusername(String userName);
	
	@Query(value = "select * from XBRL_USER_PROFILE_TABLE where user_id=?1 ", nativeQuery = true)
	List<UserProfile> delete(String userid);
	
	@Query(value = "select * from XBRL_USER_PROFILE_TABLE", nativeQuery = true)
	List<UserProfile> getalluser();
	
	@Query(value = "select * from XBRL_USER_PROFILE_TABLE where USER_ID=?1", nativeQuery = true)
	UserProfile getRole(String userId);
	
	@Query(value = "select * from XBRL_USER_PROFILE_TABLE where role_id in ('DCD_ADMIN','DCD_BRANCH')", nativeQuery = true)
	Iterable<UserProfile> getUsersListByecdd();
	
	@Query(value = "select * from XBRL_USER_PROFILE_TABLE where role_id in ('RBR_ADMIN','RBR_BRANCH')", nativeQuery = true)
	Iterable<UserProfile> getUsersListByrbr();
	
	@Query(value = "select * from XBRL_USER_PROFILE_TABLE where role_id in ('BRC')", nativeQuery = true)
	Iterable<UserProfile> getUsersListBybrc();

}
