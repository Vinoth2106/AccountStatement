package com.bornfire.AccountStatement.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "SCHEDULE_STATEMENT")
public class ScheduledStatement_Entity {

	@Id
	@Column(name = "ID", precision = 20, scale = 0)
	private BigDecimal id;

	@Column(name = "SCHEDULE_NAME")
	private String scheduleName;

	@Column(name = "FREQUENCY")
	private String frequency;

	@Column(name = "DAY_DESC")
	private String dayDesc;

	@Column(name = "RUN_TIME")
	private String runTime;

	@Column(name = "START_DATE")
	private String startDate;

	@Column(name = "OUTPUT_FORMAT")
	private String outputFormat;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "RECIPIENTS")
	private String recipients;

	public BigDecimal getId() {
		return id;
	}

	public void setId(BigDecimal id) {
		this.id = id;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getDayDesc() {
		return dayDesc;
	}

	public void setDayDesc(String dayDesc) {
		this.dayDesc = dayDesc;
	}

	public String getRunTime() {
		return runTime;
	}

	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}
}