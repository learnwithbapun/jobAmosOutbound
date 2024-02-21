package com.gbt.cdms.amos.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_history_table", schema = "cdms")
@NoArgsConstructor
@Setter
@Getter
public class JobHistoryTable implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Integer id;

	@Column(name = "job_key", length = 50)
	private String jobKey;

	@Column(name = "start_time")
	private Timestamp startTime;

	@Column(name = "end_time")
	private Timestamp endTime;

	@Column(name = "is_manual")
	private boolean isManual;

	@Column(name = "job_status", length = 100)
	private String jobStatus;

	@Column(name = "error_desc", length = 100)
	private String errorDesc;
}