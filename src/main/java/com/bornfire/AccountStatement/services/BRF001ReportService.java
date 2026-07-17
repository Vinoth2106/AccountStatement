package com.bornfire.AccountStatement.services;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import java.util.Objects;

import javax.sql.DataSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.core.env.Environment;

import com.bornfire.AccountStatement.services.BRF001ReportService;

@Service
@Transactional
public class BRF001ReportService {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	   private DataSource srcdataSource;
	
	@Autowired
	Environment env;
	
	private static final Logger logger = LoggerFactory.getLogger(BRF001ReportService.class);
	
	DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

	public ModelAndView getBRF001View(String reportId, String fromdate,
	        String todate, String currency, String dtltype,
	        Pageable pageable) {

	    ModelAndView mv = new ModelAndView();

	    try {

	        Date reportDate = df.parse(todate);

	        String sql = "SELECT * FROM BRF1_SUMMARYTABLE WHERE report_date = ?";

	        List<Map<String, Object>> reportData =
	                jdbcTemplate.queryForList(
	                        sql,
	                        new java.sql.Date(reportDate.getTime()));

	        mv.addObject("reportsummary", reportData);
	        mv.addObject("reportmaster", reportData);

	    } catch (ParseException e) {
	        e.printStackTrace();
	    }

	    mv.setViewName("RR/BRF1");
	    mv.addObject("displaymode", "summary");
	    mv.addObject("reportsflag", "reportsflag");
	    mv.addObject("menu", reportId);

	    return mv;
	}
	
	public String preCheck(String reportid, String fromdate, String todate) {

	    String msg = "";

	    try {
	        Date dt9 = new SimpleDateFormat("dd/MM/yyyy").parse(todate);

	        logger.info("Report precheck : " + reportid);

	        // Count records for report date
	        String sql1 = "SELECT COUNT(*) FROM BRF1_SUMMARYTABLE WHERE report_date = ?";

	        Long dtlcnt = jdbcTemplate.queryForObject(
	                sql1,
	                Long.class,
	                new java.sql.Date(dt9.getTime()));

	        if (dtlcnt != null && dtlcnt > 0) {

	            logger.info("Getting No of records in Mod table :" + reportid);

	            String sql2 = "SELECT COUNT(*) FROM BRF1_SUMMARYTABLE";

	            Long modcnt = jdbcTemplate.queryForObject(sql2, Long.class);

	            if (modcnt != null && modcnt > 0) {
	                msg = "success";
	            }

	        } else {
	            msg = "success";
	        }

	    } catch (Exception e) {
	        logger.error("Error during preCheck", e);
	        msg = "success";
	    }

	    return msg;
	}

	public ModelAndView getBRF001currentDtl(String reportId, String fromdate, String todate, String currency,
        String dtltype, Pageable pageable, String filter, String searchVal) {

    int pageSize = pageable.getPageSize();
    int startItem = (int) pageable.getOffset();

    ModelAndView mv = new ModelAndView();
    mv.setViewName("RR/BRF1::reportcontent");

    Date reportDate;
    try {
        reportDate = df.parse(todate);
    } catch (ParseException e) {
        logger.error("Error parsing todate in getBRF001currentDtl", e);
        mv.addObject("reportdetails", java.util.Collections.emptyList());
        return mv;
    }
    java.sql.Date sqlReportDate = new java.sql.Date(reportDate.getTime());

    // Build WHERE clause + params list together, in order, to avoid SQL injection
    StringBuilder where = new StringBuilder(" WHERE report_date = ? ");
    List<Object> params = new ArrayList<>();
    params.add(sqlReportDate);

    boolean useFilter = (dtltype.equals("report") || dtltype.equals("ARCH")) && filter != null && !filter.equals("null");
    if (useFilter) {
        where.append(" AND report_label_1 = ? ");
        params.add(filter);
    }

    if (searchVal != null && !searchVal.trim().isEmpty()) {
        String likeVal = "%" + searchVal.trim().toUpperCase() + "%";
        where.append(" AND (UPPER(CUST_ID) LIKE ? OR UPPER(FORACID) LIKE ? OR UPPER(ACCT_NAME) LIKE ? "
                + "OR UPPER(TO_CHAR(ACT_BALANCE_AMT_LC)) LIKE ? OR UPPER(REPORT_NAME_1) LIKE ? "
                + "OR UPPER(REPORT_LABEL_1) LIKE ? OR UPPER(REPORT_ADDL_CRITERIA_1) LIKE ? "
                + "OR UPPER(TO_CHAR(REPORT_DATE, 'DD-MON-YYYY')) LIKE ?) ");
        for (int i = 0; i < 8; i++) {
            params.add(likeVal);
        }
    }

    String countSql = "SELECT COUNT(*) FROM BRF1_DETAILTABLE" + where;
    long totalRecords;
    try {
        totalRecords = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
    } catch (Exception e) {
        logger.error("Error counting BRF1_DETAILTABLE records", e);
        totalRecords = 0L;
    }

    logger.info("REQUESTED PAGE SIZE: " + pageSize);
    logger.info("REQUESTED OFFSET: " + startItem);

    String dataSql = "SELECT * FROM BRF1_DETAILTABLE" + where
            + " ORDER BY CUST_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    List<Object> dataParams = new ArrayList<>(params);
    dataParams.add(startItem);
    dataParams.add(pageSize);

    logger.info("Getting Report Detail for : " + reportId + "," + fromdate + "," + todate + "," + currency);

    List<Map<String, Object>> rows;
    try {
        rows = jdbcTemplate.queryForList(dataSql, dataParams.toArray());
    } catch (Exception e) {
        logger.error("Error fetching BRF1_DETAILTABLE records", e);
        rows = java.util.Collections.emptyList();
    }

    // Derive DR/CR remark per row (kept in-memory since it's not stored data)
    List<Map<String, Object>> reportDetails = new ArrayList<>();
    for (Map<String, Object> row : rows) {
        Map<String, Object> enriched = new HashMap<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            enriched.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        Object bal = row.get("ACT_BALANCE_AMT_LC");
        String remarks1;
        if (bal != null) {
            remarks1 = bal.toString().contains("-") ? "DR" : "CR";
        } else {
            remarks1 = "";
        }
        enriched.put("remarks1", remarks1);
        reportDetails.add(enriched);
    }

    Page<Object> reportDetailsPage = new PageImpl<>(new ArrayList<Object>(reportDetails), pageable, totalRecords);

    System.out.println("Size of the list from DB " + reportDetailsPage.getTotalElements());

    mv.addObject("reportdetails", reportDetailsPage.getContent());
    mv.addObject("reportdetailsPage", reportDetailsPage);
    mv.addObject("singledetail", new java.util.HashMap<String, Object>());
    mv.addObject("reportsflag", "reportsflag");
    mv.addObject("menu", reportId);
    mv.addObject("dtltype", dtltype);
    return mv;
} 
	public String detailChanges1( String foracid, String report_addl_criteria_1,
	        BigDecimal act_balance_amt_lc, String report_label_1, String report_name_1, String report_date) {

	    String msg = "";

	    try {
	        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	        LocalDate parsedDate = LocalDate.parse(report_date, inputFormatter);
	        java.sql.Date sqlReportDate = java.sql.Date.valueOf(parsedDate);

	        String selectSql = "SELECT REPORT_LABEL_1, REPORT_NAME_1, ACT_BALANCE_AMT_LC, REPORT_ADDL_CRITERIA_1 "
	                + "FROM BRF1_DETAILTABLE WHERE FORACID = ? AND REPORT_DATE = ?";

	        Map<String, Object> existing;
	        try {
	            existing = jdbcTemplate.queryForMap(selectSql, foracid, sqlReportDate);
	        } catch (EmptyResultDataAccessException e) {
	            existing = null;
	        }

	        if (existing != null) {
	            String currentLabel = (String) existing.get("REPORT_LABEL_1");
	            String currentName = (String) existing.get("REPORT_NAME_1");
	            BigDecimal currentBalance = (BigDecimal) existing.get("ACT_BALANCE_AMT_LC");
	            String currentAddlCriteria = (String) existing.get("REPORT_ADDL_CRITERIA_1");

	            List<String> oldValues = new ArrayList<>();
	            List<String> newValues = new ArrayList<>();
	            List<String> fieldNames = new ArrayList<>();

	            StringBuilder setClause = new StringBuilder();
	            List<Object> updateParams = new ArrayList<>();

	            if (!Objects.equals(currentLabel, report_label_1)) {
	                oldValues.add(currentLabel);
	                newValues.add(report_label_1);
	                fieldNames.add("report_label_1");
	                setClause.append(setClause.length() > 0 ? ", " : "").append("REPORT_LABEL_1 = ?");
	                updateParams.add(report_label_1);
	            }
	            if (!Objects.equals(currentName, report_name_1)) {
	                oldValues.add(currentName);
	                newValues.add(report_name_1);
	                fieldNames.add("report_name_1");
	                setClause.append(setClause.length() > 0 ? ", " : "").append("REPORT_NAME_1 = ?");
	                updateParams.add(report_name_1);
	            }
	            // BigDecimal uses compareTo, not equals: equals() treats 110.0 and 110.00
	            // as different values because it also compares scale, which caused false
	            // "modified" positives in the old entity-based comparison.
	            boolean balanceChanged = (currentBalance == null) != (act_balance_amt_lc == null)
	                    || (currentBalance != null && act_balance_amt_lc != null
	                        && currentBalance.compareTo(act_balance_amt_lc) != 0);
	            if (balanceChanged) {
	                oldValues.add(currentBalance != null ? currentBalance.toString() : "null");
	                newValues.add(act_balance_amt_lc != null ? act_balance_amt_lc.toString() : "null");
	                fieldNames.add("act_balance_amt_lc");
	                setClause.append(setClause.length() > 0 ? ", " : "").append("ACT_BALANCE_AMT_LC = ?");
	                updateParams.add(act_balance_amt_lc);
	            }
	            if (!Objects.equals(currentAddlCriteria, report_addl_criteria_1)) {
	                oldValues.add(currentAddlCriteria);
	                newValues.add(report_addl_criteria_1);
	                fieldNames.add("report_addl_criteria_1");
	                setClause.append(setClause.length() > 0 ? ", " : "").append("REPORT_ADDL_CRITERIA_1 = ?");
	                updateParams.add(report_addl_criteria_1);
	            }

	            if (fieldNames.isEmpty()) {
	                msg = "No modification done";
	            } else {
	                String updateSql = "UPDATE BRF1_DETAILTABLE SET " + setClause
	                        + " WHERE FORACID = ? AND REPORT_DATE = ?";
	                updateParams.add(foracid);
	                updateParams.add(sqlReportDate);

	                jdbcTemplate.update(updateSql, updateParams.toArray());
	                logger.info("Edited Record");

	                // Audit-table insert kept out, mirroring the commented-out block
	                // in the original method.

	                final String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(sqlReportDate);
	                try {
	                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
	                        @Override
	                        public void afterCommit() {
	                            try {
	                                logger.info("Transaction committed — calling BRF1_SUMMARY_PROCEDURE({})", formattedDate);
	                                jdbcTemplate.update("BEGIN BRF1_SUMMARY_PROCEDURE(?); END;", formattedDate);
	                                logger.info("Procedure executed successfully after commit.");
	                            } catch (Exception e) {
	                                logger.error("Error executing procedure after commit", e);
	                            }
	                        }
	                    });
	                } catch (Exception e) {
	                    logger.error("Error preparing procedure call", e);
	                }

	                msg = "Edited Successfully";
	            }
	        } else {
	            msg = "No data Found";
	        }
	    } catch (Exception e) {
	        msg = "error occured. Please contact Administrator";
	        e.printStackTrace();
	    }

	    return msg;
	} 
	
		/**
		 * Safely extracts a BigDecimal from a jdbcTemplate row map. Handles NUMBER
		 * columns coming back as BigDecimal or other Number subtypes, and returns
		 * null for missing/null values (mirrors the old entity getter's null-safety).
		 */
		private BigDecimal getBD(Map<String, Object> row, String column) {
			Object val = row.get(column);
			if (val == null) return null;
			if (val instanceof BigDecimal) return (BigDecimal) val;
			return new BigDecimal(val.toString());
		}

	public File getFile(String reportId, String fromdate, String todate, String currency, String dtltype,
				String filetype, String filter) throws FileNotFoundException, JRException, SQLException {

			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

			String path = this.env.getProperty("output.exportpath");
			String fileName = "";
			String zipFileName = "";
			File outputFile;

			logger.info("Getting Output file :" + reportId);
			fileName = "011-BRF-001-A";

			if (!filetype.equals("xbrl")) {
				if (!filetype.contains("BRF")) {

					try {
						InputStream jasperFile;
						logger.info("Getting Jasper file :" + reportId);
						if (filetype.equals("detailexcel")) {
							if (dtltype.equals("report")) {
								jasperFile = this.getClass().getResourceAsStream("/static/jasper/BRF1_Detail.jrxml");
							} else {
								jasperFile = this.getClass().getResourceAsStream("/static/jasper/BRF1_Detail.jrxml");
							}

						} else {
							if (dtltype.equals("report")) {
								logger.info("Inside report");
								jasperFile = this.getClass().getResourceAsStream("/static/jasper/BRF1.jrxml");
							} else {
								jasperFile = this.getClass().getResourceAsStream("/static/jasper/BRF1.jrxml");
							}
						}

						JasperReport jr = JasperCompileManager.compileReport(jasperFile);
						HashMap<String, Object> map = new HashMap<String, Object>();

						logger.info("Assigning Parameters for Jasper");
						map.put("REPORT_DATE", todate);
						map.put("CELL_MAPPING", filter);

						if (filetype.equals("pdf")) {
							fileName = fileName + ".pdf";
							path += fileName;
							JasperPrint jp = JasperFillManager.fillReport(jr, map, srcdataSource.getConnection());
							JasperExportManager.exportReportToPdfFile(jp, path);
							logger.info("PDF File exported");
						} else {
							fileName = fileName + ".xlsx";
							path += fileName;
							JasperPrint jp = JasperFillManager.fillReport(jr, map, srcdataSource.getConnection());
							JRXlsxExporter exporter = new JRXlsxExporter();
							exporter.setExporterInput(new SimpleExporterInput(jp));
							exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(path));
							exporter.exportReport();
							logger.info("Excel File exported");
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					outputFile = new File(path);
					return outputFile;
				} else {
					List<Map<String, Object>> T1Master;
					try {
						Date d1 = df.parse(todate);

						String reportSql = "SELECT * FROM BRF1_SUMMARYTABLE WHERE REPORT_DATE = ?";
						T1Master = jdbcTemplate.queryForList(reportSql, new java.sql.Date(d1.getTime()));

						if (T1Master.size() == 1) {

							for (Map<String, Object> BRF001row : T1Master) {

								File Responsecamt = new File(
										env.getProperty("output.exportpathtemp") + "011-BRF-001-AT.xls");

								Workbook workbook = WorkbookFactory.create(Responsecamt);

								Sheet sheet = workbook.getSheetAt(0);

								///// srl_no -12////////

								Row row = sheet.getRow(11);
								Cell cell = row.getCell(5);
								if (cell != null) {
									cell.setCellValue(getBD(BRF001row, "R2_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R2_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell cell1 = row.getCell(7);
								if (cell1 != null) {
									cell1.setCellValue(getBD(BRF001row, "R2_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R2_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell cell2 = row.getCell(9);
								if (cell2 != null) {
									cell2.setCellValue(getBD(BRF001row, "R2_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R2_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell cell3 = row.getCell(11);
								if (cell3 != null) {
									cell3.setCellValue(getBD(BRF001row, "R2_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R2_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	///////srl_no -13/////

								Row row3 = sheet.getRow(12);
								Cell R3cell = row3.getCell(5);
								if (R3cell != null) {
									R3cell.setCellValue(getBD(BRF001row, "R3_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R3_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R3cell1 = row3.getCell(7);
								if (R3cell1 != null) {
									R3cell1.setCellValue(getBD(BRF001row, "R3_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R3_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R3cell2 = row3.getCell(9);
								if (R3cell2 != null) {
									R3cell2.setCellValue(getBD(BRF001row, "R3_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R3_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R3cell3 = row3.getCell(11);
								if (R3cell3 != null) {
									R3cell3.setCellValue(getBD(BRF001row, "R3_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R3_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -14/////////

								Row row4 = sheet.getRow(13);
								Cell R4cell = row4.getCell(5);
								if (R4cell != null) {
									R4cell.setCellValue(getBD(BRF001row, "R4_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R4_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R4cell1 = row4.getCell(7);
								if (R4cell1 != null) {
									R4cell1.setCellValue(getBD(BRF001row, "R4_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R4_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R4cell2 = row4.getCell(9);
								if (R4cell2 != null) {
									R4cell2.setCellValue(getBD(BRF001row, "R4_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R4_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R4cell3 = row4.getCell(11);
								if (R4cell3 != null) {
									R4cell3.setCellValue(getBD(BRF001row, "R4_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R4_AMOUNT_FCY_NON_RESIDENT").intValue());
								}
	/////srl_no -15/////////

								Row row5 = sheet.getRow(14);
								Cell R5cell = row5.getCell(5);
								if (R5cell != null) {
									R5cell.setCellValue(getBD(BRF001row, "R5_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R5_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R5cell1 = row5.getCell(7);
								if (R5cell1 != null) {
									R5cell1.setCellValue(getBD(BRF001row, "R5_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R5_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R5cell2 = row5.getCell(9);
								if (R5cell2 != null) {
									R5cell2.setCellValue(getBD(BRF001row, "R5_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R5_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R5cell3 = row5.getCell(11);
								if (R5cell3 != null) {
									R5cell3.setCellValue(getBD(BRF001row, "R5_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R5_AMOUNT_FCY_NON_RESIDENT").intValue());
								}
								///// srl_no -17/////////

								Row row7 = sheet.getRow(16);
								Cell R7cell = row7.getCell(5);
								if (R7cell != null) {
									R7cell.setCellValue(getBD(BRF001row, "R7_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R7_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R7cell1 = row7.getCell(7);
								if (R7cell1 != null) {
									R7cell1.setCellValue(getBD(BRF001row, "R7_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R7_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R7cell2 = row7.getCell(9);
								if (R7cell2 != null) {
									R7cell2.setCellValue(getBD(BRF001row, "R7_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R7_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R7cell3 = row7.getCell(11);
								if (R7cell3 != null) {
									R7cell3.setCellValue(getBD(BRF001row, "R7_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R7_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

								///// srl_no -18/////////

								Row row8 = sheet.getRow(17);
								Cell R8cell = row8.getCell(5);
								if (R8cell != null) {
									R8cell.setCellValue(getBD(BRF001row, "R8_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R8_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R8cell1 = row8.getCell(7);
								if (R8cell1 != null) {
									R8cell1.setCellValue(getBD(BRF001row, "R8_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R8_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R8cell2 = row8.getCell(9);
								if (R8cell2 != null) {
									R8cell2.setCellValue(getBD(BRF001row, "R8_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R8_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R8cell3 = row8.getCell(11);
								if (R8cell3 != null) {
									R8cell3.setCellValue(getBD(BRF001row, "R8_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R8_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

								///// srl_no -20/////////

								Row row10 = sheet.getRow(19);
								Cell R10cell = row10.getCell(5);
								if (R10cell != null) {
									R10cell.setCellValue(getBD(BRF001row, "R10_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R10_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R10cell1 = row10.getCell(7);
								if (R10cell1 != null) {
									R10cell1.setCellValue(getBD(BRF001row, "R10_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R10_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R10cell2 = row10.getCell(9);
								if (R10cell2 != null) {
									R10cell2.setCellValue(getBD(BRF001row, "R10_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R10_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R10cell3 = row10.getCell(11);
								if (R10cell3 != null) {
									R10cell3.setCellValue(getBD(BRF001row, "R10_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R10_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

								///// srl_no -21/////////

								Row row11 = sheet.getRow(20);
								Cell R11cell = row11.getCell(5);
								if (R11cell != null) {
									R11cell.setCellValue(getBD(BRF001row, "R11_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R11_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R11cell1 = row11.getCell(7);
								if (R11cell1 != null) {
									R11cell1.setCellValue(getBD(BRF001row, "R11_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R11_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R11cell2 = row11.getCell(9);
								if (R11cell2 != null) {
									R11cell2.setCellValue(getBD(BRF001row, "R11_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R11_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R11cell3 = row11.getCell(11);
								if (R11cell3 != null) {
									R11cell3.setCellValue(getBD(BRF001row, "R11_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R11_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -22/////////

								Row row12 = sheet.getRow(21);
								Cell R12cell = row12.getCell(5);
								if (R12cell != null) {
									R12cell.setCellValue(getBD(BRF001row, "R12_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R12_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R12cell1 = row12.getCell(7);
								if (R12cell1 != null) {
									R12cell1.setCellValue(getBD(BRF001row, "R12_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R12_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R12cell2 = row12.getCell(9);
								if (R12cell2 != null) {
									R12cell2.setCellValue(getBD(BRF001row, "R12_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R12_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R12cell3 = row12.getCell(11);
								if (R12cell3 != null) {
									R12cell3.setCellValue(getBD(BRF001row, "R12_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R12_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -24/////////

								Row row14 = sheet.getRow(23);
								Cell R14cell = row14.getCell(5);
								if (R14cell != null) {
									R14cell.setCellValue(getBD(BRF001row, "R14_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R14_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R14cell1 = row14.getCell(7);
								if (R14cell1 != null) {
									R14cell1.setCellValue(getBD(BRF001row, "R14_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R14_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R14cell2 = row14.getCell(9);
								if (R14cell2 != null) {
									R14cell2.setCellValue(getBD(BRF001row, "R14_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R14_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R14cell3 = row14.getCell(11);
								if (R14cell3 != null) {
									R14cell3.setCellValue(getBD(BRF001row, "R14_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R14_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -25/////////

								Row row15 = sheet.getRow(24);
								Cell R15cell = row15.getCell(5);
								if (R15cell != null) {
									R15cell.setCellValue(getBD(BRF001row, "R15_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R15_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R15cell1 = row15.getCell(7);
								if (R15cell1 != null) {
									R15cell1.setCellValue(getBD(BRF001row, "R15_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R15_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R15cell2 = row15.getCell(9);
								if (R15cell2 != null) {
									R15cell2.setCellValue(getBD(BRF001row, "R15_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R15_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R15cell3 = row15.getCell(11);
								if (R15cell3 != null) {
									R15cell3.setCellValue(getBD(BRF001row, "R15_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R15_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

								///// srl_no -26/////////

								Row row16 = sheet.getRow(25);
								Cell R16cell = row16.getCell(5);
								if (R16cell != null) {
									R16cell.setCellValue(getBD(BRF001row, "R16_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R16_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R16cell1 = row16.getCell(7);
								if (R16cell1 != null) {
									R16cell1.setCellValue(getBD(BRF001row, "R16_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R16_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R16cell2 = row16.getCell(9);
								if (R16cell2 != null) {
									R16cell2.setCellValue(getBD(BRF001row, "R16_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R16_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R16cell3 = row16.getCell(11);
								if (R16cell3 != null) {
									R16cell3.setCellValue(getBD(BRF001row, "R16_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R16_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -27/////////

								Row row17 = sheet.getRow(26);
								Cell R17cell = row17.getCell(5);
								if (R17cell != null) {
									R17cell.setCellValue(getBD(BRF001row, "R17_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R17_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R17cell1 = row17.getCell(7);
								if (R17cell1 != null) {
									R17cell1.setCellValue(getBD(BRF001row, "R17_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R17_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R17cell2 = row17.getCell(9);
								if (R17cell2 != null) {
									R17cell2.setCellValue(getBD(BRF001row, "R17_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R17_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R17cell3 = row17.getCell(11);
								if (R17cell3 != null) {
									R17cell3.setCellValue(getBD(BRF001row, "R17_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R17_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -28/////////

								Row row18 = sheet.getRow(27);
								Cell R18cell = row18.getCell(5);
								if (R18cell != null) {
									R18cell.setCellValue(getBD(BRF001row, "R18_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R18_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R18cell1 = row18.getCell(7);
								if (R18cell1 != null) {
									R18cell1.setCellValue(getBD(BRF001row, "R18_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R18_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R18cell2 = row18.getCell(9);
								if (R18cell2 != null) {
									R18cell2.setCellValue(getBD(BRF001row, "R18_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R18_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R18cell3 = row18.getCell(11);
								if (R18cell3 != null) {
									R18cell3.setCellValue(getBD(BRF001row, "R18_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R18_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -29/////////

								Row row19 = sheet.getRow(28);
								Cell R19cell = row19.getCell(5);
								if (R19cell != null) {
									R19cell.setCellValue(getBD(BRF001row, "R19_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R19_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R19cell1 = row19.getCell(7);
								if (R19cell1 != null) {
									R19cell1.setCellValue(getBD(BRF001row, "R19_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R19_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R19cell2 = row19.getCell(9);
								if (R19cell2 != null) {
									R19cell2.setCellValue(getBD(BRF001row, "R19_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R19_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R19cell3 = row19.getCell(11);
								if (R19cell3 != null) {
									R19cell3.setCellValue(getBD(BRF001row, "R19_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R19_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -31/////////

								Row row21 = sheet.getRow(30);
								Cell R21cell = row21.getCell(5);
								if (R21cell != null) {
									R21cell.setCellValue(getBD(BRF001row, "R21_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R21_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R21cell1 = row21.getCell(7);
								if (R21cell1 != null) {
									R21cell1.setCellValue(getBD(BRF001row, "R21_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R21_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R21cell2 = row21.getCell(9);
								if (R21cell2 != null) {
									R21cell2.setCellValue(getBD(BRF001row, "R21_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R21_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R21cell3 = row21.getCell(11);
								if (R21cell3 != null) {
									R21cell3.setCellValue(getBD(BRF001row, "R21_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R21_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -32/////////

								Row row22 = sheet.getRow(31);
								Cell R22cell = row22.getCell(5);
								if (R22cell != null) {
									R22cell.setCellValue(getBD(BRF001row, "R22_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R22_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R22cell1 = row22.getCell(7);
								if (R22cell1 != null) {
									R22cell1.setCellValue(getBD(BRF001row, "R22_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R22_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R22cell2 = row22.getCell(9);
								if (R22cell2 != null) {
									R22cell2.setCellValue(getBD(BRF001row, "R22_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R22_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R22cell3 = row22.getCell(11);
								if (R22cell3 != null) {
									R22cell3.setCellValue(getBD(BRF001row, "R22_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R22_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -33/////////

								Row row23 = sheet.getRow(32);
								Cell R23cell = row23.getCell(5);
								if (R23cell != null) {
									R23cell.setCellValue(getBD(BRF001row, "R23_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R23_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R23cell1 = row23.getCell(7);
								if (R23cell1 != null) {
									R23cell1.setCellValue(getBD(BRF001row, "R23_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R23_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R23cell2 = row23.getCell(9);
								if (R23cell2 != null) {
									R23cell2.setCellValue(getBD(BRF001row, "R23_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R23_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R23cell3 = row23.getCell(11);
								if (R23cell3 != null) {
									R23cell3.setCellValue(getBD(BRF001row, "R23_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R23_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -36/////////

								Row row26 = sheet.getRow(35);
								Cell R26cell = row26.getCell(5);
								if (R26cell != null) {
									R26cell.setCellValue(getBD(BRF001row, "R26_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R26_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R26cell1 = row26.getCell(7);
								if (R26cell1 != null) {
									R26cell1.setCellValue(getBD(BRF001row, "R26_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R26_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R26cell2 = row26.getCell(9);
								if (R26cell2 != null) {
									R26cell2.setCellValue(getBD(BRF001row, "R26_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R26_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R26cell3 = row26.getCell(11);
								if (R26cell3 != null) {
									R26cell3.setCellValue(getBD(BRF001row, "R26_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R26_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -37/////////

								Row row27 = sheet.getRow(36);
								Cell R27cell = row27.getCell(5);
								if (R27cell != null) {
									R27cell.setCellValue(getBD(BRF001row, "R27_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R27_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R27cell1 = row27.getCell(7);
								if (R27cell1 != null) {
									R27cell1.setCellValue(getBD(BRF001row, "R27_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R27_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R27cell2 = row27.getCell(9);
								if (R27cell2 != null) {
									R27cell2.setCellValue(getBD(BRF001row, "R27_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R27_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R27cell3 = row27.getCell(11);
								if (R27cell3 != null) {
									R27cell3.setCellValue(getBD(BRF001row, "R27_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R27_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -38/////////

								Row row28 = sheet.getRow(37);
								Cell R28cell = row28.getCell(5);
								if (R28cell != null) {
									R28cell.setCellValue(getBD(BRF001row, "R28_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R28_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R28cell1 = row28.getCell(7);
								if (R28cell1 != null) {
									R28cell1.setCellValue(getBD(BRF001row, "R28_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R28_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R28cell2 = row28.getCell(9);
								if (R28cell2 != null) {
									R28cell2.setCellValue(getBD(BRF001row, "R28_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R28_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R28cell3 = row28.getCell(11);
								if (R28cell3 != null) {
									R28cell3.setCellValue(getBD(BRF001row, "R28_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R28_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -41/////////

								Row row31 = sheet.getRow(40);
								Cell R31cell = row31.getCell(5);
								if (R31cell != null) {
									R31cell.setCellValue(getBD(BRF001row, "R31_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R31_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R31cell1 = row31.getCell(7);
								if (R31cell1 != null) {
									R31cell1.setCellValue(getBD(BRF001row, "R31_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R31_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R31cell2 = row31.getCell(9);
								if (R31cell2 != null) {
									R31cell2.setCellValue(getBD(BRF001row, "R31_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R31_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R31cell3 = row31.getCell(11);
								if (R31cell3 != null) {
									R31cell3.setCellValue(getBD(BRF001row, "R31_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R31_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -42/////////

								Row row32 = sheet.getRow(41);
								Cell R32cell = row32.getCell(5);
								if (R32cell != null) {
									R32cell.setCellValue(getBD(BRF001row, "R32_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R32_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R32cell1 = row32.getCell(7);
								if (R32cell1 != null) {
									R32cell1.setCellValue(getBD(BRF001row, "R32_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R32_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R32cell2 = row32.getCell(9);
								if (R32cell2 != null) {
									R32cell2.setCellValue(getBD(BRF001row, "R32_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R32_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R32cell3 = row32.getCell(11);
								if (R32cell3 != null) {
									R32cell3.setCellValue(getBD(BRF001row, "R32_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R32_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -43/////////

								Row row33 = sheet.getRow(42);
								Cell R33cell = row33.getCell(5);
								if (R33cell != null) {
									R33cell.setCellValue(getBD(BRF001row, "R33_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R33_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R33cell1 = row33.getCell(7);
								if (R33cell1 != null) {
									R33cell1.setCellValue(getBD(BRF001row, "R33_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R33_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R33cell2 = row33.getCell(9);
								if (R33cell2 != null) {
									R33cell2.setCellValue(getBD(BRF001row, "R33_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R33_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R33cell3 = row33.getCell(11);
								if (R33cell3 != null) {
									R33cell3.setCellValue(getBD(BRF001row, "R33_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R33_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -45/////////

								Row row35 = sheet.getRow(44);
								Cell R35cell = row35.getCell(5);
								if (R35cell != null) {
									R35cell.setCellValue(getBD(BRF001row, "R35_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R35_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R35cell1 = row35.getCell(7);
								if (R35cell1 != null) {
									R35cell1.setCellValue(getBD(BRF001row, "R35_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R35_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R35cell2 = row35.getCell(9);
								if (R35cell2 != null) {
									R35cell2.setCellValue(getBD(BRF001row, "R35_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R35_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R35cell3 = row35.getCell(11);
								if (R35cell3 != null) {
									R35cell3.setCellValue(getBD(BRF001row, "R35_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R35_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -46/////////

								Row row36 = sheet.getRow(45);
								Cell R36cell = row36.getCell(5);
								if (R36cell != null) {
									R36cell.setCellValue(getBD(BRF001row, "R36_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R36_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R36cell1 = row36.getCell(7);
								if (R36cell1 != null) {
									R36cell1.setCellValue(getBD(BRF001row, "R36_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R36_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R36cell2 = row36.getCell(9);
								if (R36cell2 != null) {
									R36cell2.setCellValue(getBD(BRF001row, "R36_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R36_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R36cell3 = row36.getCell(11);
								if (R36cell3 != null) {
									R36cell3.setCellValue(getBD(BRF001row, "R36_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R36_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -49/////////

								Row row39 = sheet.getRow(48);
								Cell R39cell = row39.getCell(4);
								if (R39cell != null) {
									R39cell.setCellValue(getBD(BRF001row, "R39_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R39_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R39cell1 = row39.getCell(5);
								if (R39cell1 != null) {
									R39cell1.setCellValue(getBD(BRF001row, "R39_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R39_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R39cell2 = row39.getCell(6);
								if (R39cell2 != null) {
									R39cell2.setCellValue(getBD(BRF001row, "R39_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R39_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R39cell3 = row39.getCell(7);
								if (R39cell3 != null) {
									R39cell3.setCellValue(getBD(BRF001row, "R39_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R39_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R39cell4 = row39.getCell(8);
								if (R39cell4 != null) {
									R39cell4.setCellValue(getBD(BRF001row, "R39_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R39_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R39cell5 = row39.getCell(9);
								if (R39cell5 != null) {
									R39cell5.setCellValue(getBD(BRF001row, "R39_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R39_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R39cell6 = row39.getCell(10);
								if (R39cell6 != null) {
									R39cell6.setCellValue(getBD(BRF001row, "R39_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R39_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R39cell7 = row39.getCell(11);
								if (R39cell7 != null) {
									R39cell7.setCellValue(getBD(BRF001row, "R39_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R39_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -50/////////

								Row row40 = sheet.getRow(49);
								Cell R40cell = row40.getCell(4);
								if (R40cell != null) {
									R40cell.setCellValue(getBD(BRF001row, "R40_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R40_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R40cell1 = row40.getCell(5);
								if (R40cell1 != null) {
									R40cell1.setCellValue(getBD(BRF001row, "R40_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R40_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R40cell2 = row40.getCell(6);
								if (R40cell2 != null) {
									R40cell2.setCellValue(getBD(BRF001row, "R40_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R40_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R40cell3 = row40.getCell(7);
								if (R40cell3 != null) {
									R40cell3.setCellValue(getBD(BRF001row, "R40_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R40_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R40cell4 = row40.getCell(8);
								if (R40cell4 != null) {
									R40cell4.setCellValue(getBD(BRF001row, "R40_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R40_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R40cell5 = row40.getCell(9);
								if (R40cell5 != null) {
									R40cell5.setCellValue(getBD(BRF001row, "R40_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R40_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R40cell6 = row40.getCell(10);
								if (R40cell6 != null) {
									R40cell6.setCellValue(getBD(BRF001row, "R40_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R40_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R40cell7 = row40.getCell(11);
								if (R40cell7 != null) {
									R40cell7.setCellValue(getBD(BRF001row, "R40_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R40_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -52/////////

								Row row42 = sheet.getRow(51);
								Cell R42cell = row42.getCell(4);
								if (R42cell != null) {
									R42cell.setCellValue(getBD(BRF001row, "R42_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R42_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R42cell1 = row42.getCell(5);
								if (R42cell1 != null) {
									R42cell1.setCellValue(getBD(BRF001row, "R42_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R42_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R42cell2 = row42.getCell(6);
								if (R42cell2 != null) {
									R42cell2.setCellValue(getBD(BRF001row, "R42_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R42_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R42cell3 = row42.getCell(7);
								if (R42cell3 != null) {
									R42cell3.setCellValue(getBD(BRF001row, "R42_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R42_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R42cell4 = row42.getCell(8);
								if (R42cell4 != null) {
									R42cell4.setCellValue(getBD(BRF001row, "R42_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R42_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R42cell5 = row42.getCell(9);
								if (R42cell5 != null) {
									R42cell5.setCellValue(getBD(BRF001row, "R42_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R42_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R42cell6 = row42.getCell(10);
								if (R42cell6 != null) {
									R42cell6.setCellValue(getBD(BRF001row, "R42_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R42_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R42cell7 = row42.getCell(11);
								if (R42cell7 != null) {
									R42cell7.setCellValue(getBD(BRF001row, "R42_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R42_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -53/////////

								Row row43 = sheet.getRow(52);
								Cell R43cell = row43.getCell(4);
								if (R43cell != null) {
									R43cell.setCellValue(getBD(BRF001row, "R43_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R43_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R43cell1 = row43.getCell(5);
								if (R43cell1 != null) {
									R43cell1.setCellValue(getBD(BRF001row, "R43_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R43_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R43cell2 = row43.getCell(6);
								if (R43cell2 != null) {
									R43cell2.setCellValue(getBD(BRF001row, "R43_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R43_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R43cell3 = row43.getCell(7);
								if (R43cell3 != null) {
									R43cell3.setCellValue(getBD(BRF001row, "R43_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R43_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R43cell4 = row43.getCell(8);
								if (R43cell4 != null) {
									R43cell4.setCellValue(getBD(BRF001row, "R43_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R43_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R43cell5 = row43.getCell(9);
								if (R43cell5 != null) {
									R43cell5.setCellValue(getBD(BRF001row, "R43_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R43_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R43cell6 = row43.getCell(10);
								if (R43cell6 != null) {
									R43cell6.setCellValue(getBD(BRF001row, "R43_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R43_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R43cell7 = row43.getCell(11);
								if (R43cell7 != null) {
									R43cell7.setCellValue(getBD(BRF001row, "R43_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R43_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -56/////////

								Row row46 = sheet.getRow(55);
								Cell R46cell = row46.getCell(4);
								if (R46cell != null) {
									R46cell.setCellValue(getBD(BRF001row, "R46_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R46_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R46cell1 = row46.getCell(5);
								if (R46cell1 != null) {
									R46cell1.setCellValue(getBD(BRF001row, "R46_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R46_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R46cell2 = row46.getCell(6);
								if (R46cell2 != null) {
									R46cell2.setCellValue(getBD(BRF001row, "R46_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R46_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R46cell3 = row46.getCell(7);
								if (R46cell3 != null) {
									R46cell3.setCellValue(getBD(BRF001row, "R46_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R46_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R46cell4 = row46.getCell(8);
								if (R46cell4 != null) {
									R46cell4.setCellValue(getBD(BRF001row, "R46_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R46_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R46cell5 = row46.getCell(9);
								if (R46cell5 != null) {
									R46cell5.setCellValue(getBD(BRF001row, "R46_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R46_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R46cell6 = row46.getCell(10);
								if (R46cell6 != null) {
									R46cell6.setCellValue(getBD(BRF001row, "R46_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R46_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R46cell7 = row46.getCell(11);
								if (R46cell7 != null) {
									R46cell7.setCellValue(getBD(BRF001row, "R46_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R46_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -57/////////

								Row row47 = sheet.getRow(56);
								Cell R47cell = row47.getCell(4);
								if (R47cell != null) {
									R47cell.setCellValue(getBD(BRF001row, "R47_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R47_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R47cell1 = row47.getCell(5);
								if (R47cell1 != null) {
									R47cell1.setCellValue(getBD(BRF001row, "R47_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R47_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R47cell2 = row47.getCell(6);
								if (R47cell2 != null) {
									R47cell2.setCellValue(getBD(BRF001row, "R47_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R47_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R47cell3 = row47.getCell(7);
								if (R47cell3 != null) {
									R47cell3.setCellValue(getBD(BRF001row, "R47_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R47_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R47cell4 = row47.getCell(8);
								if (R47cell4 != null) {
									R47cell4.setCellValue(getBD(BRF001row, "R47_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R47_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R47cell5 = row47.getCell(9);
								if (R47cell5 != null) {
									R47cell5.setCellValue(getBD(BRF001row, "R47_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R47_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R47cell6 = row47.getCell(10);
								if (R47cell6 != null) {
									R47cell6.setCellValue(getBD(BRF001row, "R47_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R47_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R47cell7 = row47.getCell(11);
								if (R47cell7 != null) {
									R47cell7.setCellValue(getBD(BRF001row, "R47_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R47_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -58/////////

								Row row48 = sheet.getRow(57);
								Cell R48cell = row48.getCell(4);
								if (R48cell != null) {
									R48cell.setCellValue(getBD(BRF001row, "R48_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R48_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R48cell1 = row48.getCell(5);
								if (R48cell1 != null) {
									R48cell1.setCellValue(getBD(BRF001row, "R48_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R48_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R48cell2 = row48.getCell(6);
								if (R48cell2 != null) {
									R48cell2.setCellValue(getBD(BRF001row, "R48_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R48_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R48cell3 = row48.getCell(7);
								if (R48cell3 != null) {
									R48cell3.setCellValue(getBD(BRF001row, "R48_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R48_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R48cell4 = row48.getCell(8);
								if (R48cell4 != null) {
									R48cell4.setCellValue(getBD(BRF001row, "R48_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R48_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R48cell5 = row48.getCell(9);
								if (R48cell5 != null) {
									R48cell5.setCellValue(getBD(BRF001row, "R48_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R48_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R48cell6 = row48.getCell(10);
								if (R48cell6 != null) {
									R48cell6.setCellValue(getBD(BRF001row, "R48_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R48_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R48cell7 = row48.getCell(11);
								if (R48cell7 != null) {
									R48cell7.setCellValue(getBD(BRF001row, "R48_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R48_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -59/////////

								Row row49 = sheet.getRow(58);
								Cell R49cell = row49.getCell(4);
								if (R49cell != null) {
									R49cell.setCellValue(getBD(BRF001row, "R49_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R49_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R49cell1 = row49.getCell(5);
								if (R49cell1 != null) {
									R49cell1.setCellValue(getBD(BRF001row, "R49_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R49_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R49cell2 = row49.getCell(6);
								if (R49cell2 != null) {
									R49cell2.setCellValue(getBD(BRF001row, "R49_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R49_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R49cell3 = row49.getCell(7);
								if (R49cell3 != null) {
									R49cell3.setCellValue(getBD(BRF001row, "R49_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R49_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R49cell4 = row49.getCell(8);
								if (R49cell4 != null) {
									R49cell4.setCellValue(getBD(BRF001row, "R49_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R49_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R49cell5 = row49.getCell(9);
								if (R49cell5 != null) {
									R49cell5.setCellValue(getBD(BRF001row, "R49_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R49_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R49cell6 = row49.getCell(10);
								if (R49cell6 != null) {
									R49cell6.setCellValue(getBD(BRF001row, "R49_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R49_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R49cell7 = row49.getCell(11);
								if (R49cell7 != null) {
									R49cell7.setCellValue(getBD(BRF001row, "R49_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R49_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -60/////////

								Row row50 = sheet.getRow(59);
								Cell R50cell = row50.getCell(4);
								if (R50cell != null) {
									R50cell.setCellValue(getBD(BRF001row, "R50_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R50_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R50cell1 = row50.getCell(5);
								if (R50cell1 != null) {
									R50cell1.setCellValue(getBD(BRF001row, "R50_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R50_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R50cell2 = row50.getCell(6);
								if (R50cell2 != null) {
									R50cell2.setCellValue(getBD(BRF001row, "R50_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R50_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R50cell3 = row50.getCell(7);
								if (R50cell3 != null) {
									R50cell3.setCellValue(getBD(BRF001row, "R50_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R50_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R50cell4 = row50.getCell(8);
								if (R50cell4 != null) {
									R50cell4.setCellValue(getBD(BRF001row, "R50_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R50_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R50cell5 = row50.getCell(9);
								if (R50cell5 != null) {
									R50cell5.setCellValue(getBD(BRF001row, "R50_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R50_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R50cell6 = row50.getCell(10);
								if (R50cell6 != null) {
									R50cell6.setCellValue(getBD(BRF001row, "R50_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R50_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R50cell7 = row50.getCell(11);
								if (R50cell7 != null) {
									R50cell7.setCellValue(getBD(BRF001row, "R50_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R50_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -62/////////

								Row row52 = sheet.getRow(61);
								Cell R52cell = row52.getCell(4);
								if (R52cell != null) {
									R52cell.setCellValue(getBD(BRF001row, "R52_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R52_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R52cell1 = row52.getCell(5);
								if (R52cell1 != null) {
									R52cell1.setCellValue(getBD(BRF001row, "R52_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R52_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R52cell2 = row52.getCell(6);
								if (R52cell2 != null) {
									R52cell2.setCellValue(getBD(BRF001row, "R52_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R52_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R52cell3 = row52.getCell(7);
								if (R52cell3 != null) {
									R52cell3.setCellValue(getBD(BRF001row, "R52_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R52_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R52cell4 = row52.getCell(8);
								if (R52cell4 != null) {
									R52cell4.setCellValue(getBD(BRF001row, "R52_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R52_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R52cell5 = row52.getCell(9);
								if (R52cell5 != null) {
									R52cell5.setCellValue(getBD(BRF001row, "R52_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R52_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R52cell6 = row52.getCell(10);
								if (R52cell6 != null) {
									R52cell6.setCellValue(getBD(BRF001row, "R52_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R52_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R52cell7 = row52.getCell(11);
								if (R52cell7 != null) {
									R52cell7.setCellValue(getBD(BRF001row, "R52_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R52_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -63/////////

								Row row53 = sheet.getRow(62);
								Cell R53cell = row53.getCell(4);
								if (R53cell != null) {
									R53cell.setCellValue(getBD(BRF001row, "R53_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R53_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R53cell1 = row53.getCell(5);
								if (R53cell1 != null) {
									R53cell1.setCellValue(getBD(BRF001row, "R53_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R53_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R53cell2 = row53.getCell(6);
								if (R53cell2 != null) {
									R53cell2.setCellValue(getBD(BRF001row, "R53_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R53_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R53cell3 = row53.getCell(7);
								if (R53cell3 != null) {
									R53cell3.setCellValue(getBD(BRF001row, "R53_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R53_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R53cell4 = row53.getCell(8);
								if (R53cell4 != null) {
									R53cell4.setCellValue(getBD(BRF001row, "R53_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R53_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R53cell5 = row53.getCell(9);
								if (R53cell5 != null) {
									R53cell5.setCellValue(getBD(BRF001row, "R53_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R53_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R53cell6 = row53.getCell(10);
								if (R53cell6 != null) {
									R53cell6.setCellValue(getBD(BRF001row, "R53_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R53_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R53cell7 = row53.getCell(11);
								if (R53cell7 != null) {
									R53cell7.setCellValue(getBD(BRF001row, "R53_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R53_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -64/////////

								Row row54 = sheet.getRow(63);
								Cell R54cell = row54.getCell(4);
								if (R54cell != null) {
									R54cell.setCellValue(getBD(BRF001row, "R54_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R54_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R54cell1 = row54.getCell(5);
								if (R54cell1 != null) {
									R54cell1.setCellValue(getBD(BRF001row, "R54_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R54_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R54cell2 = row54.getCell(6);
								if (R54cell2 != null) {
									R54cell2.setCellValue(getBD(BRF001row, "R54_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R54_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R54cell3 = row54.getCell(7);
								if (R54cell3 != null) {
									R54cell3.setCellValue(getBD(BRF001row, "R54_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R54_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R54cell4 = row54.getCell(8);
								if (R54cell4 != null) {
									R54cell4.setCellValue(getBD(BRF001row, "R54_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R54_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R54cell5 = row54.getCell(9);
								if (R54cell5 != null) {
									R54cell5.setCellValue(getBD(BRF001row, "R54_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R54_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R54cell6 = row54.getCell(10);
								if (R54cell6 != null) {
									R54cell6.setCellValue(getBD(BRF001row, "R54_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R54_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R54cell7 = row54.getCell(11);
								if (R54cell7 != null) {
									R54cell7.setCellValue(getBD(BRF001row, "R54_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R54_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -65/////////

								Row row55 = sheet.getRow(64);
								Cell R55cell = row55.getCell(4);
								if (R55cell != null) {
									R55cell.setCellValue(getBD(BRF001row, "R55_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R55_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R55cell1 = row55.getCell(5);
								if (R55cell1 != null) {
									R55cell1.setCellValue(getBD(BRF001row, "R55_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R55_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R55cell2 = row55.getCell(6);
								if (R55cell2 != null) {
									R55cell2.setCellValue(getBD(BRF001row, "R55_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R55_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R55cell3 = row55.getCell(7);
								if (R55cell3 != null) {
									R55cell3.setCellValue(getBD(BRF001row, "R55_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R55_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R55cell4 = row55.getCell(8);
								if (R55cell4 != null) {
									R55cell4.setCellValue(getBD(BRF001row, "R55_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R55_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R55cell5 = row55.getCell(9);
								if (R55cell5 != null) {
									R55cell5.setCellValue(getBD(BRF001row, "R55_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R55_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R55cell6 = row55.getCell(10);
								if (R55cell6 != null) {
									R55cell6.setCellValue(getBD(BRF001row, "R55_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R55_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R55cell7 = row55.getCell(11);
								if (R55cell7 != null) {
									R55cell7.setCellValue(getBD(BRF001row, "R55_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R55_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -66/////////

								Row row56 = sheet.getRow(65);
								Cell R56cell = row56.getCell(4);
								if (R56cell != null) {
									R56cell.setCellValue(getBD(BRF001row, "R56_NO_ACCT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R56_NO_ACCT_AED_RESIDENT").intValue());
								}
								Cell R56cell1 = row56.getCell(5);
								if (R56cell1 != null) {
									R56cell1.setCellValue(getBD(BRF001row, "R56_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R56_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R56cell2 = row56.getCell(6);
								if (R56cell2 != null) {
									R56cell2.setCellValue(getBD(BRF001row, "R56_NO_ACCT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R56_NO_ACCT_FCY_RESIDENT").intValue());
								}
								Cell R56cell3 = row56.getCell(7);
								if (R56cell3 != null) {
									R56cell3.setCellValue(getBD(BRF001row, "R56_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R56_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R56cell4 = row56.getCell(8);
								if (R56cell4 != null) {
									R56cell4.setCellValue(getBD(BRF001row, "R56_NO_ACCT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R56_NO_ACCT_AED_NON_RESIDENT").intValue());
								}
								Cell R56cell5 = row56.getCell(9);
								if (R56cell5 != null) {
									R56cell5.setCellValue(getBD(BRF001row, "R56_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R56_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R56cell6 = row56.getCell(10);
								if (R56cell6 != null) {
									R56cell6.setCellValue(getBD(BRF001row, "R56_NO_ACCT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R56_NO_ACCT_FCY_NON_RESIDENT").intValue());
								}
								Cell R56cell7 = row56.getCell(11);
								if (R56cell7 != null) {
									R56cell7.setCellValue(getBD(BRF001row, "R56_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R56_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -68/////////

								Row row58 = sheet.getRow(67);
								Cell R58cell = row58.getCell(5);
								if (R58cell != null) {
									R58cell.setCellValue(getBD(BRF001row, "R58_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R58_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R58cell1 = row58.getCell(7);
								if (R58cell1 != null) {
									R58cell1.setCellValue(getBD(BRF001row, "R58_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R58_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R58cell2 = row58.getCell(9);
								if (R58cell2 != null) {
									R58cell2.setCellValue(getBD(BRF001row, "R58_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R58_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R58cell3 = row58.getCell(11);
								if (R58cell3 != null) {
									R58cell3.setCellValue(getBD(BRF001row, "R58_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R58_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -69/////////

								Row row59 = sheet.getRow(68);
								Cell R59cell = row59.getCell(5);
								if (R59cell != null) {
									R59cell.setCellValue(getBD(BRF001row, "R59_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R59_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R59cell1 = row59.getCell(7);
								if (R59cell1 != null) {
									R59cell1.setCellValue(getBD(BRF001row, "R59_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R59_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R59cell2 = row59.getCell(9);
								if (R59cell2 != null) {
									R59cell2.setCellValue(getBD(BRF001row, "R59_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R59_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R59cell3 = row59.getCell(11);
								if (R59cell3 != null) {
									R59cell3.setCellValue(getBD(BRF001row, "R59_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R59_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -70/////////

								Row row60 = sheet.getRow(69);
								Cell R60cell = row60.getCell(5);
								if (R60cell != null) {
									R60cell.setCellValue(getBD(BRF001row, "R60_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R60_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R60cell1 = row60.getCell(7);
								if (R60cell1 != null) {
									R60cell1.setCellValue(getBD(BRF001row, "R60_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R60_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R60cell2 = row60.getCell(9);
								if (R60cell2 != null) {
									R60cell2.setCellValue(getBD(BRF001row, "R60_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R60_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R60cell3 = row60.getCell(11);
								if (R60cell3 != null) {
									R60cell3.setCellValue(getBD(BRF001row, "R60_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R60_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -71/////////

								Row row61 = sheet.getRow(70);
								Cell R61cell = row61.getCell(5);
								if (R61cell != null) {
									R61cell.setCellValue(getBD(BRF001row, "R61_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R61_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R61cell1 = row61.getCell(7);
								if (R61cell1 != null) {
									R61cell1.setCellValue(getBD(BRF001row, "R61_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R61_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R61cell2 = row61.getCell(9);
								if (R61cell2 != null) {
									R61cell2.setCellValue(getBD(BRF001row, "R61_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R61_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R61cell3 = row61.getCell(11);
								if (R61cell3 != null) {
									R61cell3.setCellValue(getBD(BRF001row, "R61_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R61_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -72/////////

								Row row62 = sheet.getRow(71);
								Cell R62cell = row62.getCell(5);
								if (R62cell != null) {
									R62cell.setCellValue(getBD(BRF001row, "R62_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R62_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R62cell1 = row62.getCell(7);
								if (R62cell1 != null) {
									R62cell1.setCellValue(getBD(BRF001row, "R62_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R62_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R62cell2 = row62.getCell(9);
								if (R62cell2 != null) {
									R62cell2.setCellValue(getBD(BRF001row, "R62_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R62_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R62cell3 = row62.getCell(11);
								if (R62cell3 != null) {
									R62cell3.setCellValue(getBD(BRF001row, "R62_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R62_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -73/////////

								Row row63 = sheet.getRow(72);
								Cell R63cell = row63.getCell(5);
								if (R63cell != null) {
									R63cell.setCellValue(getBD(BRF001row, "R63_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R63_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R63cell1 = row63.getCell(7);
								if (R63cell1 != null) {
									R63cell1.setCellValue(getBD(BRF001row, "R63_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R63_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R63cell2 = row63.getCell(9);
								if (R63cell2 != null) {
									R63cell2.setCellValue(getBD(BRF001row, "R63_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R63_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R63cell3 = row63.getCell(11);
								if (R63cell3 != null) {
									R63cell3.setCellValue(getBD(BRF001row, "R63_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R63_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -75/////////

								Row row65 = sheet.getRow(74);
								Cell R65cell = row65.getCell(5);
								if (R65cell != null) {
									R65cell.setCellValue(getBD(BRF001row, "R65_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R65_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R65cell1 = row65.getCell(7);
								if (R65cell1 != null) {
									R65cell1.setCellValue(getBD(BRF001row, "R65_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R65_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R65cell2 = row65.getCell(9);
								if (R65cell2 != null) {
									R65cell2.setCellValue(getBD(BRF001row, "R65_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R65_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R65cell3 = row65.getCell(11);
								if (R65cell3 != null) {
									R65cell3.setCellValue(getBD(BRF001row, "R65_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R65_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -76/////////

								Row row66 = sheet.getRow(75);
								Cell R66cell = row66.getCell(5);
								if (R66cell != null) {
									R66cell.setCellValue(getBD(BRF001row, "R66_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R66_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R66cell1 = row66.getCell(7);
								if (R66cell1 != null) {
									R66cell1.setCellValue(getBD(BRF001row, "R66_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R66_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R66cell2 = row66.getCell(9);
								if (R66cell2 != null) {
									R66cell2.setCellValue(getBD(BRF001row, "R66_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R66_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R66cell3 = row66.getCell(11);
								if (R66cell3 != null) {
									R66cell3.setCellValue(getBD(BRF001row, "R66_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R66_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -77/////////

								Row row67 = sheet.getRow(76);
								Cell R67cell = row67.getCell(5);
								if (R67cell != null) {
									R67cell.setCellValue(getBD(BRF001row, "R67_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R67_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R67cell1 = row67.getCell(7);
								if (R67cell1 != null) {
									R67cell1.setCellValue(getBD(BRF001row, "R67_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R67_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R67cell2 = row67.getCell(9);
								if (R67cell2 != null) {
									R67cell2.setCellValue(getBD(BRF001row, "R67_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R67_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R67cell3 = row67.getCell(11);
								if (R67cell3 != null) {
									R67cell3.setCellValue(getBD(BRF001row, "R67_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R67_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -78/////////

								Row row68 = sheet.getRow(77);
								Cell R68cell = row68.getCell(5);
								if (R68cell != null) {
									R68cell.setCellValue(getBD(BRF001row, "R68_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R68_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R68cell1 = row68.getCell(7);
								if (R68cell1 != null) {
									R68cell1.setCellValue(getBD(BRF001row, "R68_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R68_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R68cell2 = row68.getCell(9);
								if (R68cell2 != null) {
									R68cell2.setCellValue(getBD(BRF001row, "R68_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R68_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R68cell3 = row68.getCell(11);
								if (R68cell3 != null) {
									R68cell3.setCellValue(getBD(BRF001row, "R68_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R68_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -79/////////

								Row row69 = sheet.getRow(78);
								Cell R69cell = row69.getCell(5);
								if (R69cell != null) {
									R69cell.setCellValue(getBD(BRF001row, "R69_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R69_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R69cell1 = row69.getCell(7);
								if (R69cell1 != null) {
									R69cell1.setCellValue(getBD(BRF001row, "R69_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R69_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R69cell2 = row69.getCell(9);
								if (R69cell2 != null) {
									R69cell2.setCellValue(getBD(BRF001row, "R69_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R69_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R69cell3 = row69.getCell(11);
								if (R69cell3 != null) {
									R69cell3.setCellValue(getBD(BRF001row, "R69_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R69_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -80/////////

								Row row70 = sheet.getRow(79);
								Cell R70cell = row70.getCell(5);
								if (R70cell != null) {
									R70cell.setCellValue(getBD(BRF001row, "R70_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R70_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R70cell1 = row70.getCell(7);
								if (R70cell1 != null) {
									R70cell1.setCellValue(getBD(BRF001row, "R70_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R70_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R70cell2 = row70.getCell(9);
								if (R70cell2 != null) {
									R70cell2.setCellValue(getBD(BRF001row, "R70_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R70_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R70cell3 = row70.getCell(11);
								if (R70cell3 != null) {
									R70cell3.setCellValue(getBD(BRF001row, "R70_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R70_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -81/////////

								Row row71 = sheet.getRow(80);
								Cell R71cell = row71.getCell(5);
								if (R71cell != null) {
									R71cell.setCellValue(getBD(BRF001row, "R71_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R71_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R71cell1 = row71.getCell(7);
								if (R71cell1 != null) {
									R71cell1.setCellValue(getBD(BRF001row, "R71_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R71_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R71cell2 = row71.getCell(9);
								if (R71cell2 != null) {
									R71cell2.setCellValue(getBD(BRF001row, "R71_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R71_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R71cell3 = row71.getCell(11);
								if (R71cell3 != null) {
									R71cell3.setCellValue(getBD(BRF001row, "R71_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R71_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -82/////////

								Row row72 = sheet.getRow(81);
								Cell R72cell = row72.getCell(5);
								if (R72cell != null) {
									R72cell.setCellValue(getBD(BRF001row, "R72_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R72_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R72cell1 = row72.getCell(7);
								if (R72cell1 != null) {
									R72cell1.setCellValue(getBD(BRF001row, "R72_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R72_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R72cell2 = row72.getCell(9);
								if (R72cell2 != null) {
									R72cell2.setCellValue(getBD(BRF001row, "R72_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R72_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R72cell3 = row72.getCell(11);
								if (R72cell3 != null) {
									R72cell3.setCellValue(getBD(BRF001row, "R72_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R72_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -84/////////

								Row row74 = sheet.getRow(83);
								Cell R74cell = row74.getCell(5);
								if (R74cell != null) {
									R74cell.setCellValue(getBD(BRF001row, "R74_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R74_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R74cell1 = row74.getCell(7);
								if (R74cell1 != null) {
									R74cell1.setCellValue(getBD(BRF001row, "R74_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R74_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R74cell2 = row74.getCell(9);
								if (R74cell2 != null) {
									R74cell2.setCellValue(getBD(BRF001row, "R74_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R74_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R74cell3 = row74.getCell(11);
								if (R74cell3 != null) {
									R74cell3.setCellValue(getBD(BRF001row, "R74_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R74_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -85/////////

								Row row75 = sheet.getRow(84);
								Cell R75cell = row75.getCell(5);
								if (R75cell != null) {
									R75cell.setCellValue(getBD(BRF001row, "R75_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R75_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R75cell1 = row75.getCell(7);
								if (R75cell1 != null) {
									R75cell1.setCellValue(getBD(BRF001row, "R75_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R75_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R75cell2 = row75.getCell(9);
								if (R75cell2 != null) {
									R75cell2.setCellValue(getBD(BRF001row, "R75_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R75_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R75cell3 = row75.getCell(11);
								if (R75cell3 != null) {
									R75cell3.setCellValue(getBD(BRF001row, "R75_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R75_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -86/////////

								Row row76 = sheet.getRow(85);
								Cell R76cell = row76.getCell(5);
								if (R76cell != null) {
									R76cell.setCellValue(getBD(BRF001row, "R76_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R76_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R76cell1 = row76.getCell(7);
								if (R76cell1 != null) {
									R76cell1.setCellValue(getBD(BRF001row, "R76_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R76_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R76cell2 = row76.getCell(9);
								if (R76cell2 != null) {
									R76cell2.setCellValue(getBD(BRF001row, "R76_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R76_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R76cell3 = row76.getCell(11);
								if (R76cell3 != null) {
									R76cell3.setCellValue(getBD(BRF001row, "R76_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R76_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -87/////////

								Row row77 = sheet.getRow(86);
								Cell R77cell = row77.getCell(5);
								if (R77cell != null) {
									R77cell.setCellValue(getBD(BRF001row, "R77_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R77_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R77cell1 = row77.getCell(7);
								if (R77cell1 != null) {
									R77cell1.setCellValue(getBD(BRF001row, "R77_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R77_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R77cell2 = row77.getCell(9);
								if (R77cell2 != null) {
									R77cell2.setCellValue(getBD(BRF001row, "R77_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R77_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R77cell3 = row77.getCell(11);
								if (R77cell3 != null) {
									R77cell3.setCellValue(getBD(BRF001row, "R77_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R77_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -88/////////

								Row row78 = sheet.getRow(87);
								Cell R78cell = row78.getCell(5);
								if (R78cell != null) {
									R78cell.setCellValue(getBD(BRF001row, "R78_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R78_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R78cell1 = row78.getCell(7);
								if (R78cell1 != null) {
									R78cell1.setCellValue(getBD(BRF001row, "R78_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R78_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R78cell2 = row78.getCell(9);
								if (R78cell2 != null) {
									R78cell2.setCellValue(getBD(BRF001row, "R78_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R78_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R78cell3 = row78.getCell(11);
								if (R78cell3 != null) {
									R78cell3.setCellValue(getBD(BRF001row, "R78_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R78_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -89/////////

								Row row79 = sheet.getRow(88);
								Cell R79cell = row79.getCell(5);
								if (R79cell != null) {
									R79cell.setCellValue(getBD(BRF001row, "R79_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R79_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R79cell1 = row79.getCell(7);
								if (R79cell1 != null) {
									R79cell1.setCellValue(getBD(BRF001row, "R79_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R79_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R79cell2 = row79.getCell(9);
								if (R79cell2 != null) {
									R79cell2.setCellValue(getBD(BRF001row, "R79_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R79_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R79cell3 = row79.getCell(11);
								if (R79cell3 != null) {
									R79cell3.setCellValue(getBD(BRF001row, "R79_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R79_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

	/////srl_no -90/////////

								Row row80 = sheet.getRow(89);
								Cell R80cell = row80.getCell(5);
								if (R80cell != null) {
									R80cell.setCellValue(getBD(BRF001row, "R80_AMOUNT_AED_RESIDENT") == null ? 0
											: getBD(BRF001row, "R80_AMOUNT_AED_RESIDENT").intValue());
								}
								Cell R80cell1 = row80.getCell(7);
								if (R80cell1 != null) {
									R80cell1.setCellValue(getBD(BRF001row, "R80_AMOUNT_FCY_RESIDENT") == null ? 0
											: getBD(BRF001row, "R80_AMOUNT_FCY_RESIDENT").intValue());
								}
								Cell R80cell2 = row80.getCell(9);
								if (R80cell2 != null) {
									R80cell2.setCellValue(getBD(BRF001row, "R80_AMOUNT_AED_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R80_AMOUNT_AED_NON_RESIDENT").intValue());
								}
								Cell R80cell3 = row80.getCell(11);
								if (R80cell3 != null) {
									R80cell3.setCellValue(getBD(BRF001row, "R80_AMOUNT_FCY_NON_RESIDENT") == null ? 0
											: getBD(BRF001row, "R80_AMOUNT_FCY_NON_RESIDENT").intValue());
								}

								// Save the changes
								workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
								FileOutputStream fileOut = new FileOutputStream(
										env.getProperty("output.exportpathfinal") + "011-BRF-001-A.xls");
								workbook.write(fileOut);
								fileOut.close();
								path = fileOut.toString();
								workbook.close();
							}

						}

					} catch (ParseException e) {
						e.printStackTrace();
					} catch (EncryptedDocumentException e) {
						e.printStackTrace();
					} catch (InvalidFormatException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

			}
			outputFile = new File(env.getProperty("output.exportpathfinal") + "011-BRF-001-A.xls");
			return outputFile;

		}
	
	public List<Object[]> getExcel(String todate) {

	    List<Object[]> rows = new ArrayList<>();

	    try {
	        Date reportDate = df.parse(todate);
	        java.sql.Date sqlReportDate = new java.sql.Date(reportDate.getTime());

	        String sql = "SELECT cust_id, foracid, acct_name, act_balance_amt_lc, "
	                + "report_name_1, report_label_1, report_addl_criteria_1, report_date "
	                + "FROM BRF1_DETAILTABLE "
	                + "WHERE report_date = ? "
	                + "ORDER BY report_label_1";

	        rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] {
	                rs.getString("cust_id"),
	                rs.getString("foracid"),
	                rs.getString("acct_name"),
	                rs.getBigDecimal("act_balance_amt_lc"),
	                rs.getString("report_name_1"),
	                rs.getString("report_label_1"),
	                rs.getString("report_addl_criteria_1"),
	                rs.getDate("report_date")
	        }, sqlReportDate);

	    } catch (ParseException e) {
	        logger.error("Error parsing todate in getBRF001DetailDownloadRows", e);
	    }

	    return rows;
	}
	
}
