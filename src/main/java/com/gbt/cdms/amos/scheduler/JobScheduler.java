package com.gbt.cdms.amos.scheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gbt.cdms.amos.constant.AppConstant;
import com.gbt.cdms.amos.entity.JobConfigurationTable;
import com.gbt.cdms.amos.entity.JobHistoryTable;
import com.gbt.cdms.amos.integration.MFTIntegration;
import com.gbt.cdms.amos.repository.JobConfigurationTableRepository;
import com.gbt.cdms.amos.repository.JobHistoryTableRepository;
import com.gbt.cdms.amos.util.AppUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JobScheduler {
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

	@Autowired
	private JobConfigurationTableRepository jobConfigurationTableRepository;

	@Autowired
	private JobHistoryTableRepository jobHistoryTableRepository;

	@Autowired
	private MFTIntegration mftIntegration;

	@Value("${sftp.isMFTEnabled}")
	private boolean is_MFT_Enabled;

	@Scheduled(cron = "0 0/5 * * * *")
	public void scheduler() {
		String requestId = String.valueOf(UUID.randomUUID());
		MDC.put("requestId", requestId);
		MDC.put("version", "v1.0");
		MDC.put("userId", "system");

		log.info("JobScheduler : Fetching job configuration: {}", AppConstant.job_key);
		JobConfigurationTable jobConfigurationTable = jobConfigurationTableRepository.findByJobKey(AppConstant.job_key);
		log.info("JobScheduler: Found job configuration: {}", AppUtils.toJson(jobConfigurationTable));
		String manual_run = jobConfigurationTable.getManualRun();
		log.info("JobScheduler: Fetching job history: {}", AppConstant.job_key);
		JobHistoryTable existJobHistoryTable = jobHistoryTableRepository.findByKey(AppConstant.job_key);
		log.info("JobScheduler : Found job history: {}", AppUtils.toJson(existJobHistoryTable));
		JobParameters jobParameters = new JobParametersBuilder().addLong("START_AT", System.currentTimeMillis())
				.toJobParameters();

		JobExecution jobExecution = null;
		LocalTime scheduled_run = AppUtils.convertCronToScheduleTime(jobConfigurationTable.getScheduledRun());

		// Start date
		LocalTime startTime = LocalTime.of(0, 0);
		if (existJobHistoryTable != null) {
			startTime = AppUtils.formattedTime(existJobHistoryTable.getStartTime());
		}

		// Current date
		LocalTime currentTime = AppUtils.formattedTime(Date.from(Instant.now()));
		boolean isManual = false;
		if (manual_run == null || manual_run.isEmpty()) {
			log.info("ScheduledRun: Starting job with startTime: {}, scheduled_run: {}, and currentTime: {}", startTime,
					scheduled_run, currentTime);
			if (startTime.isBefore(scheduled_run) && currentTime.isAfter(scheduled_run)) {
				try {
					JobParametersBuilder builder = new JobParametersBuilder();
					builder.addDate("date", new Date());
					jobExecution = jobLauncher.run(job, builder.toJobParameters());
					log.info("ScheduledRun: Batch job completed with status:: {}", jobExecution.getStatus());
					saveJobHistoryTable(jobExecution, isManual);
					checkMFTEnabled(jobExecution);
				} catch (Exception e) {
					MDC.put("code", "SCHEDULE_RUN_FAILURE");
					MDC.put("text", e.getMessage());
					MDC.put("severity", "HIGH");
					log.error("ScheduledRun error: {}", e.getMessage());
				}
			} else {
				log.info("Scheduled run condition failed");
			}
		} else {
			isManual = true;
			LocalTime updated_manual_run = AppUtils.convertCronToTime(manual_run);
			log.info("Manual Run: Starting job with startTime: {}, manual_run: {}, and currentTime: {}", startTime,
					updated_manual_run, currentTime);
			if (startTime.isBefore(updated_manual_run) && currentTime.isAfter(updated_manual_run)) {
				try {
					JobParametersBuilder builder = new JobParametersBuilder();
					builder.addDate("date", new Date());
					jobExecution = jobLauncher.run(job, builder.toJobParameters());
					log.info("Batch job completed with status:: {}", jobExecution.getStatus());
					jobConfigurationTable.setManualRun("");
					log.info("Updating manual_run columns of jobConfigurationTable with data - {}",
							AppUtils.toJson(jobConfigurationTable));
					jobConfigurationTableRepository.updateManualColumn(jobConfigurationTable.getManualRun(),
							AppConstant.job_key);
					log.info("Updated manual_run columns of jobConfigurationTable");
					saveJobHistoryTable(jobExecution, isManual);
					checkMFTEnabled(jobExecution);
				} catch (Exception e) {
					MDC.put("code", "MANUAL_RUN_FAILURE");
					MDC.put("text", e.getMessage());
					MDC.put("severity", "HIGH");
					log.error("Manual Run error: {}", e.getMessage());
				}
			} else {
				log.info("Manual run condition failed");
			}
		}
	}

	private void uploadFiles(JobExecution jobExecution) {
		try {
			if ("COMPLETED".equals(jobExecution.getStatus().toString())) {
				modifyFile(AppConstant.GISCXMSTP_DEFAULT_FILE_PATH);
				log.info("Uploading file GISCXMSTP from: {} to sftp server", AppConstant.GISCXMSTP_DEFAULT_FILE_PATH);
				mftIntegration.uploadFileToSftp(AppConstant.GISCXMSTP_DEFAULT_FILE_PATH);
				log.info("Uploaded file GISCXMSTP to sftp server.");

				modifyFile(AppConstant.GISDRMSTPT_DEFAULT_FILE_PATH);
				log.info("Uploading file GISDRMSTPT from: {} to sftp server", AppConstant.GISDRMSTPT_DEFAULT_FILE_PATH);
				mftIntegration.uploadFileToSftp(AppConstant.GISDRMSTPT_DEFAULT_FILE_PATH);
				log.info("Uploaded file GISDRMSTPT to sftp server.");
				log.info("Execution status is {}", jobExecution.getStatus().toString());
			}
		} catch (Exception e) {
			log.error("Failed to upload file due to : {}", e.getMessage());
		}
	}

	private void saveJobHistoryTable(JobExecution jobExecution, boolean isManual) {
		JobHistoryTable jobHistoryTable = new JobHistoryTable();
		jobHistoryTable.setJobKey(AppConstant.job_key);
		jobHistoryTable.setStartTime(new Timestamp(jobExecution.getCreateTime().getTime()));
		jobHistoryTable.setJobStatus(jobExecution.getStatus().toString());
		jobHistoryTable.setEndTime(new Timestamp(jobExecution.getEndTime().getTime()));
		jobHistoryTable.setManual(isManual);
		log.info("Saving job history with data {}", AppUtils.toJson(jobHistoryTable));
		jobHistoryTableRepository.save(jobHistoryTable);
		log.info("Saved job history");
	}

	private void modifyFile(String fileName) {
		log.info("Replacing contents with special character ? for file: {}", fileName);
		File fileToBeModified = new File(fileName);
		File tempFile = new File(fileName + ".tmp");
		BufferedReader reader = null;
		BufferedWriter writer = null;

		try {
			reader = new BufferedReader(new FileReader(fileToBeModified));
			writer = new BufferedWriter(new FileWriter(tempFile));
			String line = reader.readLine();
			while (line != null) {
				String replacedLine = line.replaceAll("@", "?");
				writer.write(replacedLine + System.lineSeparator());
				line = reader.readLine();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		if (!fileToBeModified.delete()) {
			log.error("Could not delete original file");
		}
		if (!tempFile.renameTo(fileToBeModified)) {
			log.error("Could not rename temporary file");
		}
		log.info("Replaced contents with special character? for file: {}", fileName);
	}

	private void checkMFTEnabled(JobExecution jobExecution) {
		log.info("Checking isMFTEnabled as {}", is_MFT_Enabled);
		boolean isMFTEnabled = is_MFT_Enabled;
		if (isMFTEnabled) {
			uploadFiles(jobExecution);
		} else {
			log.info("MFT transfer is skipped");
		}
	}

	//
}
