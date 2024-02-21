package com.gbt.cdms.amos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gbt.cdms.amos.entity.JobHistoryTable;

@Repository
public interface JobHistoryTableRepository extends JpaRepository<JobHistoryTable, Integer> {

	@Query(value = "SELECT * FROM cdms.job_history_table where job_key=:job_key ORDER BY start_time DESC LIMIT 1", nativeQuery = true)
	JobHistoryTable findByKey(String job_key);

}