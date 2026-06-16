package com.bornfire.AccountStatement.entities;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RRReportRepo extends JpaRepository<RRReport, Integer> {

	@Query(value = "SELECT * FROM RR_RPT_MAST t " + "WHERE t.REMARKS_5 = :remarks5 " + " AND t.END_DATE = ( "
			+ " SELECT MAX(t2.END_DATE) " + " FROM RR_RPT_MAST t2 " + " WHERE t2.RPT_CODE = t.RPT_CODE "
			+ " AND t2.REMARKS_5 = :remarks5 " + ") " + " ORDER BY t.RPT_CODE", nativeQuery = true)
	List<RRReport> findReportsByRemarks(@Param("remarks5") String remarks5);
	
	@Query(value = "SELECT * FROM ( " + " SELECT * FROM RR_RPT_MAST t "
			+ " WHERE t.end_date = :end_date AND t.REMARKS_5 = :remarks5 " + " UNION ALL "
			+ " SELECT * FROM RR_RPT_MAST t " + " WHERE t.REMARKS_5 = :remarks5 " + " AND NOT EXISTS ( " + " SELECT 1 "
			+ " FROM RR_RPT_MAST x " + " WHERE x.RPT_CODE = t.RPT_CODE " + " AND x.end_date = :end_date "
			+ " AND x.REMARKS_5 = :remarks5 " + " ) " + " AND t.end_date = ( " + " SELECT MAX(t2.end_date) "
			+ " FROM RR_RPT_MAST t2 " + " WHERE t2.RPT_CODE = t.RPT_CODE AND t2.REMARKS_5 = :remarks5 " + " ) "
			+ ") combined_results " + "ORDER BY RPT_CODE", nativeQuery = true)
	List<RRReport> findDataByDate(@Param("end_date") Date end_date, @Param("remarks5") String remarks5);
	
	@Query(value = "select end_date from RR_RPT_MAST WHERE RPT_CODE =?1", nativeQuery = true)
	List<Date> getdatelist(String rptcode);
}
