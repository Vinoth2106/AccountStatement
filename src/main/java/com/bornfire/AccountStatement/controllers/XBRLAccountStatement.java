package com.bornfire.AccountStatement.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;

import com.itextpdf.text.pdf.BaseFont;
import com.bornfire.AccountStatement.entities.TransactionInquiry;
import com.bornfire.AccountStatement.entities.TransactionInquiryRep;
import com.bornfire.AccountStatement.services.EmailServices;
import com.bornfire.AccountStatement.services.EmailServices;

import com.bornfire.AccountStatement.util.EnglishToArabicNameTransliterator;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Controller
@ConfigurationProperties("default")
@RequestMapping(value = "ReportsAccount")
public class XBRLAccountStatement {

	@Autowired
	DataSource srcdataSource;

	DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	Environment env;

	@Autowired
	TransactionInquiryRep transactionInquiryRep;

	@Autowired
	EmailServices emailservices;

	private static String[] columns_Loan = { "SL", "Tran Date", "Tran ID", "Part Tran Type", "Value Date",
			"Tran Particular", "Debit Amount", "Credit Amount", "Tran Currency" };

	private static final Logger logger = LoggerFactory.getLogger(XBRLAccountStatement.class);

	private static List<TransactionInquiry> dailyList = new ArrayList<TransactionInquiry>();

	private static BaseFont ARABIC_REGULAR;
	private static BaseFont ARABIC_BOLD;
	private static BaseFont LATIN_REGULAR;
	private static BaseFont LATIN_BOLD;

	private static final char[] ARABIC_INDIC_DIGITS = { '\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665',
			'\u0666', '\u0667', '\u0668', '\u0669' };

	static {
		try {
			ARABIC_REGULAR = BaseFont.createFont("fonts/NotoSansArabic-Regular.ttf", BaseFont.IDENTITY_H,
					BaseFont.EMBEDDED);
			ARABIC_BOLD = BaseFont.createFont("fonts/NotoSansArabic-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			LATIN_REGULAR = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			LATIN_BOLD = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		} catch (Exception e) {
			logger.error("Failed to load fonts for account statement PDF", e);
		}
	}

	private static final float ARABIC_DATA_FONT_SIZE = 9f;
	private static final float ARABIC_HEADER_FONT_SIZE = 10f;

	@GetMapping("/ReportDownloadXLSX")
	public ResponseEntity<InputStreamResource> AMLDownloadExcel(HttpServletResponse response,
			@RequestParam(value = "reportId", required = false) String reportId,
			@RequestParam(value = "acid", required = false) String acid,
			@RequestParam(value = "fromdate", required = false) String fromdate,
			@RequestParam(value = "todate", required = false) String todate,
			@RequestParam(value = "userid", required = false) String userid,
			@RequestParam(value = "filetype", required = false) String filetype,
			@RequestParam(value = "dtltype", required = false) String dtltype,
			@RequestParam(value = "category", required = false) String category)
			throws IOException, SQLException, JRException, ParseException {
		response.setContentType("application/octet-stream");
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");

		String toDAte = null;

		if (fromdate != null && !fromdate.isEmpty()) {

			if (todate.equals("undefined")) {
				toDAte = "";
			} else {
				Date ConToDate = dateFormat1.parse(todate);
				String strDate1 = formatter1.format(ConToDate);
				toDAte = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate1));
			}

		} else {
			toDAte = "";
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Account Statement" + toDAte + ".xlsx");
		String filename = "Account Statement" + toDAte + ".xlsx";
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(
				getDownloadFileExcel(null, acid, fromdate, todate, dtltype, filetype, category, filename)));

	}

	@GetMapping("/ReportDownloadXLSXE")
	public ResponseEntity<InputStreamResource> AMLDownloadExcelE(HttpServletResponse response,
			@RequestParam(value = "reportId", required = false) String reportId,
			@RequestParam(value = "acid", required = false) String acid,
			@RequestParam(value = "fromdate", required = false) String fromdate,
			@RequestParam(value = "todate", required = false) String todate,
			@RequestParam(value = "userid", required = false) String userid,
			@RequestParam(value = "filetype", required = false) String filetype,
			@RequestParam(value = "dtltype", required = false) String dtltype,
			@RequestParam(value = "category", required = false) String category)
			throws IOException, SQLException, JRException, ParseException {
		response.setContentType("application/octet-stream");
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");

		String toDAte = null;

		if (fromdate != null && !fromdate.isEmpty()) {

			if (todate.equals("undefined")) {
				toDAte = "";
			} else {
				Date ConToDate = dateFormat1.parse(todate);
				String strDate1 = formatter1.format(ConToDate);
				toDAte = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate1));
			}

		} else {
			toDAte = "";
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Account Statement" + toDAte + ".xlsx");
		String filename = "Account Statement" + toDAte + ".xlsx";
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(
				getDownloadFileExcelE(null, acid, fromdate, todate, dtltype, filetype, category, filename)));

	}

	@RequestMapping(value = "/ReportDownloadPDF", method = RequestMethod.GET)
	@ResponseBody
	public InputStreamResource AMLReportDownloadFormattedDate(HttpServletResponse response,
			@RequestParam(value = "reportId", required = false) String reportId,
			@RequestParam(value = "acid", required = false) String acid,
			@RequestParam(value = "fromdate", required = false) String fromdate,
			@RequestParam(value = "todate", required = false) String todate,
			@RequestParam(value = "userid", required = false) String userid,
			@RequestParam(value = "filetype", required = false) String filetype,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "accountnumber", required = false) String accountnumber,
			@RequestParam(value = "Acctname", required = false) String Acctname)
			throws IOException, SQLException, ParseException {
		response.setContentType("application/octet-stream");

		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");

		Date ConDateFromdate = dateFormat1.parse(fromdate);
		System.out.println(ConDateFromdate);

		String strDate2 = formatter1.format(ConDateFromdate);
		fromdate = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate2));

		Date ConToDate = dateFormat1.parse(todate);
		System.out.println(ConToDate);

		String strDate1 = formatter1.format(ConToDate);
		todate = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate1));

		InputStreamResource resource = null;
		try {
			logger.info("Getting download File :" + reportId + ", FileType :" + filetype);
			// System.out.println(asondate);getDownloadFile
			File repfile = getDownloadFileScr(acid, userid, reportId, fromdate, todate, null, null, filetype, category,
					accountnumber, Acctname);

			response.setHeader("Content-Disposition", "attachment; filename=" + repfile.getName());
			resource = new InputStreamResource(new FileInputStream(repfile));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return resource;
	}

	@RequestMapping(value = "/ReportDownloadPDFE", method = RequestMethod.GET)
	@ResponseBody
	public InputStreamResource AMLReportDownloadFormattedDateE(HttpServletResponse response,
			@RequestParam(value = "reportId", required = false) String reportId,
			@RequestParam(value = "acid", required = false) String acid,
			@RequestParam(value = "fromdate", required = false) String fromdate,
			@RequestParam(value = "todate", required = false) String todate,
			@RequestParam(value = "userid", required = false) String userid,
			@RequestParam(value = "filetype", required = false) String filetype,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "accountnumber", required = false) String accountnumber,
			@RequestParam(value = "Acctname", required = false) String Acctname)
			throws IOException, SQLException, ParseException {
		response.setContentType("application/octet-stream");

		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");

		Date ConDateFromdate = dateFormat1.parse(fromdate);
		System.out.println(ConDateFromdate);

		String strDate2 = formatter1.format(ConDateFromdate);
		fromdate = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate2));

		Date ConToDate = dateFormat1.parse(todate);
		System.out.println(ConToDate);

		String strDate1 = formatter1.format(ConToDate);
		todate = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate1));

		InputStreamResource resource = null;
		try {
			logger.info("Getting download File :" + reportId + ", FileType :" + filetype);
			// System.out.println(asondate);getDownloadFile
			File repfile = getDownloadFileScr(acid, userid, reportId, fromdate, todate, null, null, filetype, category,
					accountnumber, Acctname);

			response.setHeader("Content-Disposition", "attachment; filename=" + repfile.getName());
			// emailservices.sendEmail(repfile.getName());

			FileInputStream fis = new FileInputStream(repfile);
			byte[] fileBytes = convertInputStreamToBytes(fis);
			emailservices.sendEmail(repfile.getName(), fileBytes, "application/pdf");
			fis.close();

			resource = new InputStreamResource(new FileInputStream(repfile));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return resource;
	}

	@RequestMapping(value = "/ReportDownloadPDFAr", method = RequestMethod.GET)
	@ResponseBody
	public InputStreamResource AMLReportDownloadFormattedDateAr(HttpServletResponse response,
			@RequestParam(value = "reportId", required = false) String reportId,
			@RequestParam(value = "acid", required = false) String acid,
			@RequestParam(value = "fromdate", required = false) String fromdate,
			@RequestParam(value = "todate", required = false) String todate,
			@RequestParam(value = "userid", required = false) String userid,
			@RequestParam(value = "filetype", required = false) String filetype,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "accountnumber", required = false) String accountnumber,
			@RequestParam(value = "Acctname", required = false) String Acctname)
			throws IOException, SQLException, ParseException {
		response.setContentType("application/octet-stream");

		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");

		Date ConDateFromdate = dateFormat1.parse(fromdate);
		String strDate2 = formatter1.format(ConDateFromdate);
		fromdate = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate2));

		Date ConToDate = dateFormat1.parse(todate);
		String strDate1 = formatter1.format(ConToDate);
		todate = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate1));

		InputStreamResource resource = null;
		try {
			logger.info("Getting Arabic download File :" + reportId + ", FileType :" + filetype);
			File repfile = getDownloadFileScr(acid, userid, reportId, fromdate, todate, null, null, filetype, category,
					accountnumber, Acctname, true);

			response.setHeader("Content-Disposition", "attachment; filename=" + repfile.getName());
			resource = new InputStreamResource(new FileInputStream(repfile));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return resource;
	}

	@RequestMapping(value = "/ReportDownloadPDFEAr", method = RequestMethod.GET)
	@ResponseBody
	public InputStreamResource AMLReportDownloadFormattedDateEAr(HttpServletResponse response,
			@RequestParam(value = "reportId", required = false) String reportId,
			@RequestParam(value = "acid", required = false) String acid,
			@RequestParam(value = "fromdate", required = false) String fromdate,
			@RequestParam(value = "todate", required = false) String todate,
			@RequestParam(value = "userid", required = false) String userid,
			@RequestParam(value = "filetype", required = false) String filetype,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "accountnumber", required = false) String accountnumber,
			@RequestParam(value = "Acctname", required = false) String Acctname)
			throws IOException, SQLException, ParseException {
		response.setContentType("application/octet-stream");

		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");

		Date ConDateFromdate = dateFormat1.parse(fromdate);
		String strDate2 = formatter1.format(ConDateFromdate);
		fromdate = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate2));

		Date ConToDate = dateFormat1.parse(todate);
		String strDate1 = formatter1.format(ConToDate);
		todate = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate1));

		InputStreamResource resource = null;
		try {
			logger.info("Getting Arabic email File :" + reportId + ", FileType :" + filetype);
			File repfile = getDownloadFileScr(acid, userid, reportId, fromdate, todate, null, null, filetype, category,
					accountnumber, Acctname, true);

			response.setHeader("Content-Disposition", "attachment; filename=" + repfile.getName());

			FileInputStream fis = new FileInputStream(repfile);
			byte[] fileBytes = convertInputStreamToBytes(fis);
			emailservices.sendEmail(repfile.getName(), fileBytes, "application/pdf");
			fis.close();

			resource = new InputStreamResource(new FileInputStream(repfile));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return resource;
	}

	public byte[] convertInputStreamToBytes(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int nRead;

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		return buffer.toByteArray();
	}

	public File getDownloadFileScr(String acid, String userid, String reportId, String fromdate, String todate,
			String currency, String dtltype, String filetype, String catgeory, String accountnumber, String Acctname)
			throws FileNotFoundException, JRException, SQLException, ParseException {
		return getDownloadFileScr(acid, userid, reportId, fromdate, todate, currency, dtltype, filetype, catgeory,
				accountnumber, Acctname, false);
	}

	public File getDownloadFileScr(String acid, String userid, String reportId, String fromdate, String todate,
			String currency, String dtltype, String filetype, String catgeory, String accountnumber, String Acctname,
			boolean arabic) throws FileNotFoundException, JRException, SQLException, ParseException {

		logger.info("Getting Report File for : " + reportId + " in pdf format, arabic=" + arabic);

		return getFile(acid, reportId, fromdate, todate, currency, dtltype, filetype, catgeory, accountnumber, Acctname,
				arabic);
	}

	public File getFile(String acid, String reportId, String fromdate, String todate, String currency, String dtltype,
			String filetype, String category, String accountnumber, String Acctname) {
		return getFile(acid, reportId, fromdate, todate, currency, dtltype, filetype, category, accountnumber, Acctname,
				false);
	}

	public File getFile(String acid, String reportId, String fromdate, String todate, String currency, String dtltype,
			String filetype, String category, String accountnumber, String Acctname, boolean arabic) {

		File outputFile = null;

		try {
			List<TransactionInquiry> tranList = transactionInquiryRep.findAllCustominddate(acid, fromdate, todate);
			String accountName = Acctname;
			String accountNumber = accountnumber;
			String filePrefix = arabic ? "Account_Statement_Ar_" : "Account_Statement_";
			String fileName = filePrefix + System.currentTimeMillis() + ".pdf";

			Document document = new Document(PageSize.A4.rotate());
			PdfWriter.getInstance(document, new FileOutputStream(fileName));
			document.open();

			com.itextpdf.text.Font titleFont;
			com.itextpdf.text.Font headerFont;
			com.itextpdf.text.Font normalFont;
			int textAlign;
			int numberAlign;
			boolean rtl;

			if (arabic) {
				if (ARABIC_REGULAR == null || ARABIC_BOLD == null) {
					throw new IllegalStateException("Arabic fonts are not available");
				}
				titleFont = new com.itextpdf.text.Font(ARABIC_BOLD, 14, com.itextpdf.text.Font.BOLD);
				headerFont = new com.itextpdf.text.Font(ARABIC_BOLD, 8, com.itextpdf.text.Font.BOLD);
				normalFont = new com.itextpdf.text.Font(ARABIC_REGULAR, 9, com.itextpdf.text.Font.NORMAL);
				textAlign = Element.ALIGN_RIGHT;
				numberAlign = Element.ALIGN_LEFT;
				rtl = true;
			} else {
				titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14,
						com.itextpdf.text.Font.BOLD);
				headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
						com.itextpdf.text.Font.BOLD);
				normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9,
						com.itextpdf.text.Font.NORMAL);
				textAlign = Element.ALIGN_LEFT;
				numberAlign = Element.ALIGN_RIGHT;
				rtl = false;
			}

			Image logo = Image.getInstance(getClass().getClassLoader().getResource("static/images/icici.png"));
			logo.scaleToFit(120, 60);
			document.add(logo);

			String titleText = arabic ? "\u0643\u0634\u0641 \u062d\u0633\u0627\u0628" : "ACCOUNT STATEMENT";
			Paragraph title = new Paragraph(titleText, titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);
			document.add(new Paragraph(" "));

			PdfPTable detailTable = new PdfPTable(4);
			detailTable.setWidthPercentage(100);
			if (rtl) {
				detailTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
			}

			String lblAccountName = arabic ? "\u0627\u0633\u0645 \u0627\u0644\u062d\u0633\u0627\u0628" : "Account Name";
			String lblAccountNumber = arabic ? "\u0631\u0642\u0645 \u0627\u0644\u062d\u0633\u0627\u0628" : "Account Number";
			String lblFromDate = arabic ? "\u0645\u0646 \u062a\u0627\u0631\u064a\u062e" : "From Date";
			String lblToDate = arabic ? "\u0625\u0644\u0649 \u062a\u0627\u0631\u064a\u062e" : "To Date";

			addDetailCell(detailTable, lblAccountName, headerFont, textAlign, true, rtl);
			addDetailCell(detailTable, accountName, normalFont, textAlign, false, rtl, arabic, true);
			addDetailCell(detailTable, lblAccountNumber, headerFont, textAlign, true, rtl);
			addDetailCell(detailTable, accountNumber, normalFont, textAlign, false, rtl, arabic, false);
			addDetailCell(detailTable, lblFromDate, headerFont, textAlign, true, rtl);
			addDetailCell(detailTable, fromdate, normalFont, textAlign, false, rtl, arabic, false);
			addDetailCell(detailTable, lblToDate, headerFont, textAlign, true, rtl);
			addDetailCell(detailTable, todate, normalFont, textAlign, false, rtl, arabic, false);
			document.add(detailTable);
			document.add(new Paragraph(" "));

			PdfPTable table = new PdfPTable(8);
			table.setWidthPercentage(100);
			float[] widths = { 5f, 12f, 12f, 12f, 30f, 12f, 12f, 12f };
			table.setWidths(widths);
			if (rtl) {
				table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
			}

			String lblSno = arabic ? "\u0645" : "S.No";
			String lblValueDate = arabic ? "\u062a\u0627\u0631\u064a\u062e \u0627\u0644\u0642\u064a\u0645\u0629" : "Value Date";
			String lblTranDate = arabic ? "\u062a\u0627\u0631\u064a\u062e \u0627\u0644\u0645\u0639\u0627\u0645\u0644\u0629" : "Tran Date";
			String lblTranId = arabic ? "\u0631\u0642\u0645 \u0627\u0644\u0645\u0639\u0627\u0645\u0644\u0629" : "Tran ID";
			String lblParticular = arabic ? "\u062a\u0641\u0627\u0635\u064a\u0644 \u0627\u0644\u0645\u0639\u0627\u0645\u0644\u0629" : "Tran Particular";
			String lblDebit = arabic ? "\u0645\u0628\u0644\u063a \u0645\u062f\u064a\u0646" : "Debit Amount";
			String lblCredit = arabic ? "\u0645\u0628\u0644\u063a \u062f\u0627\u0626\u0646" : "Credit Amount";
			String lblClosing = arabic ? "\u0627\u0644\u0631\u0635\u064a\u062f \u0627\u0644\u062e\u062a\u0627\u0645\u064a" : "Closing Balance";
			String lblFinalClosing = arabic ? "\u0627\u0644\u0631\u0635\u064a\u062f \u0627\u0644\u062e\u062a\u0627\u0645\u064a \u0627\u0644\u0646\u0647\u0627\u0626\u064a"
					: "Final Closing Balance";

			addHeader(table, lblSno, headerFont, Element.ALIGN_CENTER, rtl);
			addHeader(table, lblValueDate, headerFont, textAlign, rtl);
			addHeader(table, lblTranDate, headerFont, textAlign, rtl);
			addHeader(table, lblTranId, headerFont, textAlign, rtl);
			addHeader(table, lblParticular, headerFont, textAlign, rtl);
			addHeader(table, lblDebit, headerFont, numberAlign, rtl);
			addHeader(table, lblCredit, headerFont, numberAlign, rtl);
			addHeader(table, lblClosing, headerFont, numberAlign, rtl);

			int sno = 1;
			BigDecimal closingBalance = BigDecimal.ZERO;
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

			for (TransactionInquiry t : tranList) {
				addBodyCell(table, String.valueOf(sno++), normalFont, Element.ALIGN_CENTER, rtl, arabic, false);
				addBodyCell(table, t.getValue_date() != null ? sdf.format(t.getValue_date()) : "-", normalFont,
						textAlign, rtl, arabic, false);
				addBodyCell(table, t.getTran_date() != null ? sdf.format(t.getTran_date()) : "-", normalFont,
						textAlign, rtl, arabic, false);
				addBodyCell(table, t.getTran_id(), normalFont, textAlign, rtl, arabic, true);
				addBodyCell(table, t.getTran_particular(), normalFont, textAlign, rtl, arabic, true);

				if ("D".equalsIgnoreCase(t.getPart_tran_type())) {
					addBodyCell(table, t.getTran_amt().toString(), normalFont, numberAlign, rtl, arabic, false);
					addBodyCell(table, "0.00", normalFont, numberAlign, rtl, arabic, false);
					closingBalance = closingBalance.subtract(t.getTran_amt());
				} else {
					addBodyCell(table, "0.00", normalFont, numberAlign, rtl, arabic, false);
					addBodyCell(table, t.getTran_amt().toString(), normalFont, numberAlign, rtl, arabic, false);
					closingBalance = closingBalance.add(t.getTran_amt());
				}

				addBodyCell(table, closingBalance.toString(), normalFont, numberAlign, rtl, arabic, false);
			}

			PdfPCell closingCell = new PdfPCell(new Phrase(lblFinalClosing, headerFont));
			closingCell.setColspan(7);
			closingCell.setHorizontalAlignment(numberAlign);
			closingCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
			if (rtl) {
				closingCell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
			}
			table.addCell(closingCell);

			String closingAmountText = arabic ? formatArabicPdfValue(closingBalance.toString(), false)
					: closingBalance.toString();
			PdfPCell amountCell = new PdfPCell(
					arabic ? createBilingualPhrase(closingAmountText, ARABIC_HEADER_FONT_SIZE, true)
							: new Phrase(closingAmountText, headerFont));
			amountCell.setHorizontalAlignment(numberAlign);
			table.addCell(amountCell);

			document.add(table);
			document.close();
			outputFile = new File(fileName);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return outputFile;
	}

	private void addHeader(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment) {
		addHeader(table, text, font, alignment, false);
	}

	private void addHeader(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment, boolean rtl) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
		cell.setHorizontalAlignment(alignment);
		if (rtl) {
			cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
		}
		table.addCell(cell);
	}

	private void addBodyCell(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment) {
		addBodyCell(table, text, font, alignment, false, false);
	}

	private void addBodyCell(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment, boolean rtl) {
		addBodyCell(table, text, font, alignment, rtl, false);
	}

	private void addBodyCell(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment, boolean rtl,
			boolean bilingual) {
		addBodyCell(table, text, font, alignment, rtl, bilingual, false);
	}

	private void addBodyCell(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment, boolean rtl,
			boolean bilingual, boolean transliterateLatin) {
		Phrase phrase = resolvePhrase(text, font, bilingual, false, transliterateLatin);
		PdfPCell cell = new PdfPCell(phrase);
		cell.setHorizontalAlignment(alignment);
		if (rtl) {
			cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
		}
		table.addCell(cell);
	}

	private void addDetailCell(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment,
			boolean header) {
		addDetailCell(table, text, font, alignment, header, false);
	}

	private void addDetailCell(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment,
			boolean header, boolean rtl) {
		addDetailCell(table, text, font, alignment, header, rtl, false);
	}

	private void addDetailCell(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment,
			boolean header, boolean rtl, boolean bilingual) {
		addDetailCell(table, text, font, alignment, header, rtl, bilingual, false);
	}

	private void addDetailCell(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment,
			boolean header, boolean rtl, boolean bilingual, boolean transliterateLatin) {
		Phrase phrase = resolvePhrase(text, font, bilingual, header, transliterateLatin);
		PdfPCell cell = new PdfPCell(phrase);
		cell.setHorizontalAlignment(alignment);
		if (header) {
			cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
		}
		if (rtl) {
			cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
		}
		table.addCell(cell);
	}

	/** Converts Western digits and decimal separators to Eastern Arabic forms (٠–٩). */
	private String toArabicIndicDigits(String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c >= '0' && c <= '9') {
				sb.append(ARABIC_INDIC_DIGITS[c - '0']);
			} else if (c == '.') {
				sb.append('\u066B');
			} else if (c == ',') {
				sb.append('\u066C');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private boolean containsLatinLetter(String value) {
		if (value == null) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
				return true;
			}
		}
		return false;
	}

	private boolean containsArabicLetter(String value) {
		if (value == null) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
			if (block == Character.UnicodeBlock.ARABIC || block == Character.UnicodeBlock.ARABIC_SUPPLEMENT
					|| block == Character.UnicodeBlock.ARABIC_EXTENDED_A) {
				return true;
			}
		}
		return false;
	}

	/** Phonetic Latin → Arabic script for names (no harakat / vowel marks). */
	private String transliterateLatinToArabic(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		if (!containsLatinLetter(value) || containsArabicLetter(value)) {
			return value;
		}
		return EnglishToArabicNameTransliterator.transliterate(value);
	}

	/**
	 * Arabic PDF formatting: optional Latin transliteration, then Eastern Arabic
	 * digits for all numbers.
	 */
	private String formatArabicPdfValue(String value, boolean transliterateLatin) {
		if (value == null) {
			return "";
		}
		String result = transliterateLatin ? transliterateLatinToArabic(value) : value;
		return toArabicIndicDigits(result);
	}

	/**
	 * Builds a phrase that renders Arabic script with Noto Sans Arabic and Latin /
	 * numeric text with Helvetica, so dynamic values (account names, particulars,
	 * etc.) are not blank in Arabic statements.
	 */
	private Phrase createBilingualPhrase(String text, float size, boolean bold) {
		if (ARABIC_REGULAR == null || LATIN_REGULAR == null) {
			return new Phrase(text != null ? text : "");
		}
		FontSelector selector = new FontSelector();
		if (bold) {
			selector.addFont(new com.itextpdf.text.Font(ARABIC_BOLD, size));
			selector.addFont(new com.itextpdf.text.Font(LATIN_BOLD, size));
		} else {
			selector.addFont(new com.itextpdf.text.Font(ARABIC_REGULAR, size));
			selector.addFont(new com.itextpdf.text.Font(LATIN_REGULAR, size));
		}
		return selector.process(text != null ? text : "");
	}

	private Phrase resolvePhrase(String text, com.itextpdf.text.Font font, boolean bilingual, boolean bold,
			boolean transliterateLatin) {
		if (bilingual) {
			float size = font != null ? font.getSize() : ARABIC_DATA_FONT_SIZE;
			String formatted = formatArabicPdfValue(text, transliterateLatin);
			return createBilingualPhrase(formatted, size, bold);
		}
		return new Phrase(text != null ? text : "", font);
	}

	public ByteArrayInputStream getDownloadFileExcel(String userid, String reportId, String fromdate, String todate,
			String dtltype, String filetype, String catgeory, String fileName)
			throws FileNotFoundException, JRException, SQLException, ParseException {

		ByteArrayInputStream repfile = null;

		repfile = getFileLoanExcel(userid, reportId, fromdate, todate, dtltype, filetype, fileName);

		return repfile;

	}

	public ByteArrayInputStream getDownloadFileExcelE(String userid, String reportId, String fromdate, String todate,
			String dtltype, String filetype, String catgeory, String fileName)
			throws JRException, SQLException, ParseException, IOException {

		ByteArrayInputStream repfile = null;

		repfile = getFileLoanExcel(userid, reportId, fromdate, todate, dtltype, filetype, fileName);
		// emailservices.sendEmail(fileName);

		byte[] fileBytes = convertInputStreamToBytes(repfile);

		emailservices.sendEmail(fileName, fileBytes,
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		return repfile;

	}

	public ByteArrayInputStream getFileLoanExcel(String userid, String acid, String fromdate, String todate,
			String dtltype, String filetype, String filename)
			throws FileNotFoundException, JRException, SQLException, ParseException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		Session hs = sessionFactory.getCurrentSession();
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");

		String fromDAte = null;
		String toDAte = null;

		if (fromdate != null && !fromdate.isEmpty()) {
			Date ConDateFromdate = dateFormat1.parse(fromdate);
			String strDate2 = formatter1.format(ConDateFromdate);
			fromDAte = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate2));

			Date ConToDate = dateFormat1.parse(todate);
			String strDate1 = formatter1.format(ConToDate);
			toDAte = formatter1.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate1));
		}

		String path = env.getProperty("output.exportpath");

		logger.info("Getting Output file :" + acid);

		try {
			InputStream fileStream = null;

			Workbook workbook = new XSSFWorkbook();
			CreationHelper createHelper = workbook.getCreationHelper();
			Sheet sheet = workbook.createSheet("Account Statement");

			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.BLACK.getIndex());

			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			headerCellStyle.setBorderTop(BorderStyle.MEDIUM);
			headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);
			headerCellStyle.setBorderLeft(BorderStyle.MEDIUM);
			headerCellStyle.setBorderRight(BorderStyle.MEDIUM);

			Row TitleRow = sheet.createRow(0);
			Cell cellTitle = TitleRow.createCell((short) 0);
			cellTitle.setCellValue("BORNFIRE BRF & ACCOUNT STATEMENT GENERATION SYSTEM");
			sheet.addMergedRegion(CellRangeAddress.valueOf("A1:I2"));
			headerCellStyle.setAlignment(HorizontalAlignment.CENTER_SELECTION);
//			headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellTitle.setCellStyle(headerCellStyle);
			RegionUtil.setBorderTop(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A1:I2"), sheet);
			RegionUtil.setBorderRight(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A1:I2"), sheet);
			RegionUtil.setBorderLeft(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A1:I2"), sheet);
			RegionUtil.setBorderBottom(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A1:I2"), sheet);
//			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

			Row ReportNameRow = sheet.createRow(2);
			Cell cellReporName = ReportNameRow.createCell((short) 0);
			cellReporName.setCellValue("Account Statement");
			sheet.addMergedRegion(CellRangeAddress.valueOf("A3:I3"));
			headerCellStyle.setAlignment(HorizontalAlignment.CENTER_SELECTION);
//			headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellReporName.setCellStyle(headerCellStyle);
			RegionUtil.setBorderTop(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A3:I3"), sheet);
			RegionUtil.setBorderRight(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A3:I3"), sheet);
			RegionUtil.setBorderLeft(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A3:I3"), sheet);
			RegionUtil.setBorderBottom(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A3:I3"), sheet);

			CellStyle reportDateCellStyle = workbook.createCellStyle();
			reportDateCellStyle.setFont(headerFont);
			reportDateCellStyle.setBorderTop(BorderStyle.MEDIUM);
			reportDateCellStyle.setBorderBottom(BorderStyle.MEDIUM);
			reportDateCellStyle.setBorderLeft(BorderStyle.MEDIUM);
			reportDateCellStyle.setBorderRight(BorderStyle.MEDIUM);

			Row Report_Date_Row = sheet.createRow(3);
			Cell cellReporDate = Report_Date_Row.createCell((short) 0);
			cellReporDate.setCellValue("Start Date- " + fromDAte);
			sheet.addMergedRegion(CellRangeAddress.valueOf("A4:C4"));
			reportDateCellStyle.setAlignment(HorizontalAlignment.LEFT);
//			reportDateCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellReporDate.setCellStyle(reportDateCellStyle);
			RegionUtil.setBorderTop(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A4:C4"), sheet);
			RegionUtil.setBorderRight(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A4:C4"), sheet);
			RegionUtil.setBorderLeft(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A4:C4"), sheet);
			RegionUtil.setBorderBottom(BorderStyle.MEDIUM, CellRangeAddress.valueOf("A4:C4"), sheet);

			Row Report_Date_Row2 = sheet.createRow(4);
			Cell cellReporDate2 = Report_Date_Row2.createCell((short) 0);
			cellReporDate2.setCellValue("End Date- " + toDAte);
			sheet.addMergedRegion(CellRangeAddress.valueOf("D4:E4"));
			reportDateCellStyle.setAlignment(HorizontalAlignment.LEFT);
//			reportDateCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellReporDate2.setCellStyle(reportDateCellStyle);
			RegionUtil.setBorderTop(BorderStyle.MEDIUM, CellRangeAddress.valueOf("D4:E4"), sheet);
			RegionUtil.setBorderRight(BorderStyle.MEDIUM, CellRangeAddress.valueOf("D4:E4"), sheet);
			RegionUtil.setBorderLeft(BorderStyle.MEDIUM, CellRangeAddress.valueOf("D4:E4"), sheet);
			RegionUtil.setBorderBottom(BorderStyle.MEDIUM, CellRangeAddress.valueOf("D4:E4"), sheet);

			Row headerRow = sheet.createRow(6);
			for (int i = 0; i < columns_Loan.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns_Loan[i]);
				cell.setCellStyle(headerCellStyle);
			}

			CellStyle dateCellStyle = workbook.createCellStyle();
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

			DataFormat fmt = workbook.createDataFormat();
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setDataFormat(fmt.getFormat("@"));

			DataFormat fmt1 = workbook.createDataFormat();
			CellStyle numStyle = workbook.createCellStyle();
			numStyle.setDataFormat(fmt1.getFormat("#,##0.000"));

			int rowNum = 6;
			int sn = 1;

			dailyList = transactionInquiryRep.findAllCustominddate(acid, fromDAte, toDAte);
			for (TransactionInquiry daily_List : dailyList) {
				Row row = sheet.createRow(++rowNum);
				writeBook_Loan(daily_List, row, dateCellStyle, sn, cellStyle, numStyle);
				sn++;
			}

			for (int i = 0; i < columns_Loan.length; i++) {
				sheet.autoSizeColumn(i);
			}

			workbook.write(out);

			out.close();

			workbook.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ByteArrayInputStream(out.toByteArray());

	}

	private void writeBook_Loan(TransactionInquiry aBook, Row row, CellStyle dateCellStyle, int sn, CellStyle dateCell,
			CellStyle numStyle) {

		Cell cell = row.createCell(0);
		cell.setCellValue(sn);
		cell.setCellStyle(dateCell);

		Cell cif = row.createCell(1);
		cif.setCellValue(aBook.getTran_date());
		cif.setCellStyle(dateCellStyle);

		Cell lastname = row.createCell(2);
		lastname.setCellValue(aBook.getTran_id());
		lastname.setCellStyle(dateCell);

		Cell firstname = row.createCell(3);
		if (aBook.getPart_tran_type().equals("D")) {
			firstname.setCellValue("Debit");
		} else {
			firstname.setCellValue("Credit");
		}

		firstname.setCellStyle(dateCell);

		Cell nid = row.createCell(4);
		nid.setCellValue(aBook.getValue_date());
		nid.setCellStyle(dateCellStyle);

		Cell risk_cat = row.createCell(5);
		risk_cat.setCellValue(aBook.getTran_particular());
		risk_cat.setCellStyle(dateCell);

		Cell pep_desc = row.createCell(6);
		if (aBook.getPart_tran_type().equals("D")) {
			pep_desc.setCellValue("-" + String.format("%,.2f", aBook.getTran_amt()));
		} else {
			pep_desc.setCellValue("-");
		}

		pep_desc.setCellStyle(numStyle);

		Cell cust_pos = row.createCell(7);
		if (aBook.getPart_tran_type().equals("C")) {
			cust_pos.setCellValue(String.format("%,.2f", aBook.getTran_amt()));
		} else {
			cust_pos.setCellValue("-");
		}

		cust_pos.setCellStyle(numStyle);

		Cell membershipDate = row.createCell(8);
		membershipDate.setCellValue(aBook.getTran_crncy_code());
		membershipDate.setCellStyle(dateCellStyle);

	}

}
