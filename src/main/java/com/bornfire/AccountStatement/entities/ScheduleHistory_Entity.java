package com.bornfire.AccountStatement.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "SCHEDULE_HISTORY")
public class ScheduleHistory_Entity {

	@Id
	@Column(name = "ID", precision = 20, scale = 0)
	private BigDecimal id;

	@Column(name = "SCHEDULE_ID", precision = 20, scale = 0)
	private BigDecimal scheduleId;

	@Column(name = "DATE_SENT")
	private String dateSent;

	@Column(name = "OUTPUT_FORMAT")
	private String outputFormat;

	@Column(name = "DELIVERY_STATUS")
	private String deliveryStatus;

	@Column(name = "ERROR_REASON")
	private String errorReason;
	
	@Column(name = "TIME_SENT")
	private String timeSent;	

	@Column(name = "SCHEDULE_NAME")
	private String scheduleName;
	
	@Column(name = "IS_RERUN")
    private String isRerun; 

    public String getIsRerun() {
        return isRerun;
    }

    public void setIsRerun(String isRerun) {
        this.isRerun = isRerun;
    }
    

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getTimeSent() {
		return timeSent;
	}

	public void setTimeSent(String timeSent) {
		this.timeSent = timeSent;
	}

	public String getErrorReason() {
		return errorReason;
	}

	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}

	public BigDecimal getId() {
		return id;
	}

	public void setId(BigDecimal id) {
		this.id = id;
	}

	public BigDecimal getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(BigDecimal scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getDateSent() {
		return dateSent;
	}

	public void setDateSent(String dateSent) {
		this.dateSent = dateSent;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}
}
