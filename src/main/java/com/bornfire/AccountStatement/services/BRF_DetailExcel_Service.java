package com.bornfire.AccountStatement.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class BRF_DetailExcel_Service {

    private static final Logger logger =
            LoggerFactory.getLogger(BRF_DetailExcel_Service.class);

    private static final String[] DETAIL_HEADERS = {
        "Cust ID",
        "Acct No",
        "Acct Name",
        "Act Balance Amt LC",
        "Report Name 1",
        "Report Label 1",
        "Report Addl Criteria 1",
        "Report Date"
    };

    public byte[] buildDetailExcel(List<Object[]> rows) {

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Detail");

            // ── Fonts ─────────────────────────────────────────────────────
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 10);
            headerFont.setColor(IndexedColors.BLACK.getIndex());

            Font dataFont = wb.createFont();
            dataFont.setFontName("Arial");
            dataFont.setFontHeightInPoints((short) 10);

            DataFormat fmt = wb.createDataFormat();

            // ── Header style (#C0C0C0) ────────────────────────────────────
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setWrapText(true);
            applyThinBorder(headerStyle);

            // ── Text cell style ───────────────────────────────────────────
            CellStyle textStyle = wb.createCellStyle();
            textStyle.setFont(dataFont);
            textStyle.setAlignment(HorizontalAlignment.LEFT);
            textStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            applyThinBorder(textStyle);

            // ── Number cell style (right-aligned, 2 decimals) ─────────────
            CellStyle numberStyle = wb.createCellStyle();
            numberStyle.setFont(dataFont);
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            numberStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            numberStyle.setDataFormat(fmt.getFormat("#,##0.00"));
            applyThinBorder(numberStyle);

            // ── Header row ────────────────────────────────────────────────
            XSSFRow headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int col = 0; col < DETAIL_HEADERS.length; col++) {
                XSSFCell cell = headerRow.createCell(col);
                cell.setCellValue(DETAIL_HEADERS[col]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data rows ─────────────────────────────────────────────────
            int rowIdx = 1;
            for (Object[] rowData : rows) {
                XSSFRow row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(16);

                for (int col = 0; col < rowData.length && col < DETAIL_HEADERS.length; col++) {
                    Object val = rowData[col];

                    if (val instanceof BigDecimal) {
                        XSSFCell cell = row.createCell(col);
                        cell.setCellValue(((BigDecimal) val).doubleValue());
                        cell.setCellStyle(numberStyle);
                    } else {
                        XSSFCell cell = row.createCell(col);
                        cell.setCellValue(toDisplayString(val));
                        cell.setCellStyle(textStyle);
                    }
                }
            }

            // ── Auto-size + padding, freeze header ────────────────────────
            for (int col = 0; col < DETAIL_HEADERS.length; col++) {
                sheet.autoSizeColumn(col);
                sheet.setColumnWidth(col, Math.min(sheet.getColumnWidth(col) + 768, 15000));
            }
            sheet.createFreezePane(0, 1);

            wb.write(out);
            logger.info("Detail Excel built — rows={} size={} bytes", rows.size(), out.size());
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to build Excel", e);
        }
    }

    // ── Border helper ─────────────────────────────────────────────────────
    private void applyThinBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    // ── Value → display string ────────────────────────────────────────────
    private String toDisplayString(Object val) {
        if (val == null) return "";
        if (val instanceof java.time.LocalDate)
            return ((java.time.LocalDate) val)
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        if (val instanceof java.time.LocalDateTime)
            return ((java.time.LocalDateTime) val)
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        if (val instanceof Date)
            return new SimpleDateFormat("dd-MM-yyyy").format((Date) val);
        return val.toString();
    }

    // ── Generate report from any entity list ──────────────────────────────
    public byte[] generateReport(List<Object[]> List) {

        List<Object[]> rows = new ArrayList<>();

        for (Object rowObj : List) {

            Object[] row = (Object[]) rowObj;

            rows.add(new Object[]{
                row[0], // cust_id
                row[1], // foracid
                row[2], // acct_name
                row[3], // act_balance_amt_lc
                row[4], // report_name_1
                row[5], // report_label_1
                row[6], // report_addl_criteria_1
                row[7]  // report_date
            });
        }

        return buildDetailExcel(rows);
    }

    // ── Reflection helper ─────────────────────────────────────────────────
//    private Object getField(Class<?> clazz, Object entity, String fieldName) {
//        try {
//            Field field = clazz.getDeclaredField(fieldName);
//            field.setAccessible(true);
//            return field.get(entity);
//        } catch (Exception e) {
//            logger.warn("Field not found: {}", fieldName);
//            return "";
//        }
//    }
    public byte[] convertExcelBytesToPdf(byte[] excelBytes) {
	    try (
	        InputStream inputStream = new ByteArrayInputStream(excelBytes);
	        Workbook workbook = WorkbookFactory.create(inputStream);
	        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream()
	    ) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
	        PdfWriter.getInstance(document, pdfOut);
	        document.open();
	        Row headerRow = null;
	        for (Row r : sheet) {
	            if (r != null && r.getLastCellNum() > 0) {
	                headerRow = r;
	                break;
	            }
	        }
	        if (headerRow == null) throw new IllegalArgumentException("Excel sheet is empty.");

	        int firstCol = headerRow.getFirstCellNum();
	        int lastCol = headerRow.getLastCellNum();

	        PdfPTable table = new PdfPTable(lastCol - firstCol);
	        table.setWidthPercentage(100);
	        table.setSpacingBefore(10f);

	        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
	        DataFormatter formatter = new DataFormatter();
	        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
	        Set<String> mergedCells = new HashSet<>();

	        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	            Row row = sheet.getRow(rowIndex);
	            if (row == null) continue;

	            for (int colIndex = firstCol; colIndex < lastCol; colIndex++) {
	                String cellKey = rowIndex + "-" + colIndex;
	                if (mergedCells.contains(cellKey)) continue;

	                Cell cell = row.getCell(colIndex);
	                String cellValue = "";
	                if (cell != null) {
	                    if (cell.getCellTypeEnum() == CellType.FORMULA) {
	                        CellValue eval = evaluator.evaluate(cell);
	                        if (eval != null) {
	                            switch (eval.getCellTypeEnum()) {
	                                case STRING:  cellValue = eval.getStringValue(); break;
	                                case NUMERIC: cellValue = String.valueOf(eval.getNumberValue()); break;
	                                case BOOLEAN: cellValue = String.valueOf(eval.getBooleanValue()); break;
	                                default: cellValue = "";
	                            }
	                        }
	                    } else {
	                        cellValue = formatter.formatCellValue(cell);
	                    }
	                }

	                PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue));

	                // Handle merged regions
	                for (CellRangeAddress range : mergedRegions) {
	                    if (range.isInRange(rowIndex, colIndex)) {
	                        int rowspan = range.getLastRow() - range.getFirstRow() + 1;
	                        int colspan = range.getLastColumn() - range.getFirstColumn() + 1;
	                        if (rowIndex == range.getFirstRow() && colIndex == range.getFirstColumn()) {
	                            if (rowspan > 1) pdfCell.setRowspan(rowspan);
	                            if (colspan > 1) pdfCell.setColspan(colspan);
	                        } else {
	                            mergedCells.add(cellKey);
	                            pdfCell = null;
	                        }
	                        break;
	                    }
	                }

	                if (pdfCell == null) continue;

	                // Background color (supports .xlsx and .xls)
	                BaseColor bgColor = null;
	                if (cell != null && cell.getCellStyle() != null) {
	                    org.apache.poi.ss.usermodel.Color poiColor = cell.getCellStyle().getFillForegroundColorColor();

	                    if (poiColor instanceof XSSFColor) { // For .xlsx
	                        XSSFColor xssfColor = (XSSFColor) poiColor;
	                        if (xssfColor != null && xssfColor.getRGB() != null) {
	                            byte[] rgb = xssfColor.getRGB();
	                            if (!(rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0))
	                                bgColor = new BaseColor(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
	                        }
	                    } else if (workbook instanceof HSSFWorkbook) { // For .xls
	                        HSSFWorkbook hwb = (HSSFWorkbook) workbook;
	                        short idx = cell.getCellStyle().getFillForegroundColor();
	                        if (idx > 0) {
	                            HSSFPalette palette = hwb.getCustomPalette();
	                            HSSFColor color = palette.getColor(idx);
	                            if (color != null && color.getTriplet() != null) {
	                                short[] rgb = color.getTriplet();
	                                if (!(rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0))
	                                    bgColor = new BaseColor(rgb[0], rgb[1], rgb[2]);
	                            }
	                        }
	                    }
	                }

	                if (bgColor != null) pdfCell.setBackgroundColor(bgColor);
	                pdfCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                pdfCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	                pdfCell.setPadding(4f);
	                table.addCell(pdfCell);
	            }
	        }

	        document.add(table);
	        document.close();
	        return pdfOut.toByteArray();
	    } catch (Exception e) {
            throw new RuntimeException("Failed to build Pdf", e);
        }
	}
}