package com.bornfire.AccountStatement.services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.bornfire.AccountStatement.services.RegulatoryReportServices;

import net.sf.jasperreports.engine.JRException;

@Service
public class RegulatoryReportServices {
	
	@Autowired
	BRF001ReportService brf001ReportService;
	
	@Autowired
	BRF_DetailExcel_Service brf_DetailExcel_Service;
	
	private static final Logger logger = LoggerFactory.getLogger(RegulatoryReportServices.class);
	
	public ModelAndView getReportView(String reportId, String reportDate, String fromdate, String todate,
			String currency, String dtltype, String subreportid, String secid, String reportingTime, Pageable pageable,
			BigDecimal srl_no, String req) {

		ModelAndView repsummary = new ModelAndView();
		logger.info("Getting Summary for the Report :" + reportId);
		
		switch (reportId) { 
			
		case "BRF001":
			repsummary = brf001ReportService.getBRF001View(reportId, fromdate, todate, currency, dtltype, pageable);
			break;
		
		}
		
		return repsummary;
	}
	
	public String preCheckReportRBS(String reportid, String fromdate, String todate) {

		String msg = "";

		logger.info("Report precheck : " + reportid);

		switch (reportid) { 
		case "BRF001":
			msg = brf001ReportService.preCheck(reportid, fromdate, todate);
			break;
			
		default:
			logger.info("default -> preCheck()");
			msg = "Master - need to process";
		}

	return msg;
	}
	
	public ModelAndView getReportDetails(String reportId, String instanceCode, String asondate, String fromdate,
			String todate, String currency, String reportingTime, String dtltype, String subreportid, String secid,
			Pageable pageable, String Filter, String searchVal) {

		ModelAndView repdetail = new ModelAndView();
		logger.info("Getting Details for the Report :" + reportId); 
	
		switch (reportId) { 
		
		case "BRF001":
			repdetail = brf001ReportService.getBRF001currentDtl(reportId, fromdate, todate, currency, dtltype, pageable,
					Filter,searchVal);
			break;
		
		}
		return repdetail;
	
	}
	
	public File getDownloadFile(String reportId, String asondate, String fromdate, String todate, String currency,
			String subreportid, String secid, String dtltype, String reportingTime, String filetype,
			String instancecode, String filter) throws JRException, SQLException, IOException {

		File repfile = null;

		logger.info("Getting Report File for : " + reportId + " in " + filetype + " format");

		switch (reportId) {
		
		case "BRF001":
			repfile = brf001ReportService.getFile(reportId, fromdate, todate, currency, dtltype, filetype, filter);
			break;
			
		} 
		return repfile;
		
	}
	
	public byte[] detailexceldownload(String reportid, String todate) {

	    byte[] excelBytes;

	    switch (reportid) {

	        case "BRF001": {
	            List<Object[]> rows = brf001ReportService.getExcel(todate);
	            excelBytes = brf_DetailExcel_Service.buildDetailExcel(rows);
	            break;
	        }

	        default:
	            throw new RuntimeException("Unknown reportid: " + reportid);
	    }

	    return excelBytes;
	} 
}
