package com.gbt.cdms.amos.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gbt.cdms.amos.entity.JobConfigurationTable;

@Repository
public interface JobConfigurationTableRepository extends JpaRepository<JobConfigurationTable, String> {

	JobConfigurationTable findByJobKey(String jobkey);

	@Transactional
	@Modifying
	@Query(value = "update cdms.job_configuration_table set manual_run=:manual Run where job_key=:jobkey", nativeQuery = true)
	void updateManualColumn(String manualRun, String jobkey);

}