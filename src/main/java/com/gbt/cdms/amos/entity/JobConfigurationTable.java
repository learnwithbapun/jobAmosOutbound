package com.gbt.cdms.amos.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_configuration_table", schema = "cdms")
@NoArgsConstructor
@Setter
@Getter
public class JobConfigurationTable implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "job_key", length = 50, nullable = false, unique = false)
	private String jobkey;
	@Column(name = "manual_run", length = 50)
	private String manualRun;
	@Column(name = "scheduled_run", length = 50)
	private String scheduledRun;
}
