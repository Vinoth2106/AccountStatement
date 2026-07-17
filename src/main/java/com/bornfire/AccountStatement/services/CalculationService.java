package com.bornfire.AccountStatement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MapAccessor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;

@Service
public class CalculationService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public BigDecimal calculate(String formula, String reportdate) {
		Map<String, Map<String, Double>> contextVariables = new HashMap<>();
		Map<String, Set<String>> tablesAndColumns = new HashMap<>();

		// Extract table and column
		Matcher matcher = Pattern.compile("([a-zA-Z0-9_]+)\\.([a-zA-Z0-9_]+)").matcher(formula);
		while (matcher.find()) {
			tablesAndColumns.computeIfAbsent(matcher.group(1), k -> new HashSet<>()).add(matcher.group(2));
		}

		// Fetch from DB
		for (Map.Entry<String, Set<String>> entry : tablesAndColumns.entrySet()) {
			String tableName = entry.getKey();
			Set<String> columns = entry.getValue();

			Map<String, Double> tableData = new HashMap<>();
			contextVariables.put(tableName, tableData);

			String colsToSelect = String.join(", ", columns);
			//System.out.println("Table name : " + tableName + " Columns to slect : " + colsToSelect + " of Report date "	+ reportdate);
			String sql = String.format("SELECT %s FROM %s WHERE REPORT_DATE = ?", colsToSelect, tableName);

			try {
				Map<String, Object> row = jdbcTemplate.queryForMap(sql, reportdate);
				for (String col : columns) {
					Number val = (Number) row.get(col);
					tableData.put(col, val != null ? val.doubleValue() : 0.0);
				}
			} catch (EmptyResultDataAccessException e) {
				for (String col : columns) {
					tableData.put(col, 0.0);
				}
			}
		}

		// Run the equation formula
		StandardEvaluationContext context = new StandardEvaluationContext(contextVariables);
		context.addPropertyAccessor(new MapAccessor());

		return new SpelExpressionParser().parseExpression(formula).getValue(context, BigDecimal.class);
	}
	
	public BigDecimal fetchSingleValue(String tableColumnPair, String reportDate) {
        // Split the string at the dot
        String[] parts = tableColumnPair.split("\\.");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid format. Expected 'tablename.columnname', got: " + tableColumnPair);
        }

        String tableName = parts[0];
        String columnName = parts[1];

        String sql = String.format("SELECT %s FROM %s WHERE REPORT_DATE = ?", columnName, tableName);

        try {
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, reportDate);
            
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
	

	Pattern formula_pattern = Pattern.compile("([a-zA-Z0-9_]+)\\.([a-zA-Z0-9_]+)");

	// Returns table names
	public List<String> extractTableNames(String formula) {
		List<String> tableNames = new ArrayList<>();
		Matcher matcher = formula_pattern.matcher(formula);
		while (matcher.find()) {
			tableNames.add(matcher.group(1));
		}
		return tableNames;
	}

	// Returns column names
	public List<String> extractColumnNames(String formula) {
		List<String> columnNames = new ArrayList<>();
		Matcher matcher = formula_pattern.matcher(formula);
		while (matcher.find()) {
			columnNames.add(matcher.group(2));
		}
		return columnNames;
	}

	// returns combinations table.column
	public List<String> extractCombinations(String formula) {
		List<String> combinations = new ArrayList<>();
		Matcher matcher = formula_pattern.matcher(formula);
		while (matcher.find()) {
			combinations.add(matcher.group(0));
		}
		return combinations;
	}
	
	public List<String> extractlefttext(String formula) {
		List<String> leftList = new ArrayList<>();
		String regex = "\"([^\"]*)\"\\.\"([^\"]*)\"";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(formula);
		while (matcher.find()) {
			leftList.add(matcher.group(1));
		}
		/*
		System.out.println("Left List:");
		for (String left : leftList) {
			System.out.println(left);
		}*/
		return leftList;
	}
	public List<String> extractrighttext(String formula) {
		List<String> rightList = new ArrayList<>();
		String regex = "\"([^\"]*)\"\\.\"([^\"]*)\"";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(formula);
		while (matcher.find()) {
			rightList.add(matcher.group(2));
		}
		/*
		System.out.println("Right List:");
		for (String right : rightList) {
			System.out.println(right);
		}*/
		return rightList;
	}
}