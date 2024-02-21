package com.gbt.cdms.amos.tasklet;

import java.util.UUID;

import javax.sql.DataSource;

import org.jboss.logging.MDC;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.gbt.cdms.amos.constant.DatabaseQueries;

import lombok.extern.slf4j.Slf4j;


@Component
public class ExecutableQueries implements Tasklet {

	@Autowired
	private DataSource dataSource;

	@Override
	public RepeatStatus execute(StepContribution arge, ChunkContext arg1) throws Exception {

		String requestId = String.valueOf(UUID.randomUUID());
		MDC.put("requestId", requestId);
		MDC.put("version", "v1.0");
		MDC.put("userId", "system");
		
		new JdbcTemplate (dataSource).execute(DatabaseQueries.DELETE_GISCXMSTP);
//		log.info(" Deleting/Clearing GISDRMSTP");
		new JdbcTemplate (dataSource).execute(DatabaseQueries.DELETE_GISDRMSTP);
//		log.info(" Inserting into GISCXMSTP table");
		new JdbcTemplate
		(dataSource).execute(DatabaseQueries.SAVE_TO_GISCXMSTP);
//		log.info(" Inserting into GISDRMSTP table");
		new JdbcTemplate (dataSource).execute(DatabaseQueries. SAVE_TO_GISDRMSTP);
//		log.info(" Updating CXMODE in CUSTXREF");
		new JdbcTemplate (dataSource).execute(DatabaseQueries. UPDATE_CXMODE_TO_CUSTXREF);
		return RepeatStatus.FINISHED;
	}
}


