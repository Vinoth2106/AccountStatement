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
