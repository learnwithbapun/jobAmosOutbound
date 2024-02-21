package com.gbt.cdms.amos.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.gbt.cdms.amos.constant.AppConstant;
import com.gbt.cdms.amos.model.Giscxmstp;
import com.gbt.cdms.amos.model.Gisdrmstp;
import com.gbt.cdms.amos.reader.FetchGiscxmstpReader;
import com.gbt.cdms.amos.reader.FetchGisdrmstpReader;
import com.gbt.cdms.amos.tasklet.ExecutableQueries;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private ExecutableQueries executableQueries;
	@Autowired
	private FetchGisdrmstpReader fetchGisdrmstpReader;
	@Autowired
	private FetchGiscxmstpReader fetchGiscxmstpReader;
	
	@Bean
	public FlatFileItemWriter<Gisdrmstp> exportGisdrmstpWriter() {
	    FlatFileItemWriter<Gisdrmstp> itemWriter = new FlatFileItemWriter<>();
	    itemWriter.setResource(new FileSystemResource(AppConstant.GISDRMSTPT_DEFAULT_FILE_PATH));
	    FormatterLineAggregator<Gisdrmstp> aggregator = new FormatterLineAggregator<>();
	    BeanWrapperFieldExtractor<Gisdrmstp> extractor = new BeanWrapperFieldExtractor<>();
	    aggregator.setFormat("%-11s % -19s % -13s % -16s % -14s % -18s % -18s");
	    extractor.setNames(AppConstant.GISDRMSTP_COLUMN_NAMES);
	    aggregator.setFieldExtractor(extractor);
	    itemWriter.setLineAggregator(aggregator);
	    return itemWriter;
	}
	
	@Bean
	public FlatFileItemWriter<Giscxmstp> exportGiscxmstpWriter() {
	    FlatFileItemWriter<Giscxmstp> itemWriter = new FlatFileItemWriter<>();
	    itemWriter.setResource(new FileSystemResource(AppConstant.GISCXMSTP_DEFAULT_FILE_PATH));
	    FormatterLineAggregator<Giscxmstp> aggregator = new FormatterLineAggregator<>();
	    BeanWrapperFieldExtractor<Giscxmstp> extractor = new BeanWrapperFieldExtractor<>();
	    aggregator.setFormat("%-19s%-13s % -11s % -16s % -16s % -58s % -58s % -33s % -33s % -18s % -33s % -11s % -10s % -26s % -33s % -26s%-13s % -33s % -26s%-13s%-9s%-");
	    extractor.setNames(AppConstant.GISCXMSTP_COLUMN_NAMES);
	    aggregator.setFieldExtractor(extractor);
	    itemWriter.setLineAggregator(aggregator);
	    return itemWriter;
	}
	@Bean
	public Step exportGisdrmstpTable() {
	    return stepBuilderFactory.get("Step 4 import to text table gisdrmstp ").<Gisdrmstp, Gisdrmstp>chunk(1000)
	            .reader(fetchGisdrmstpReader)
	            .writer(exportGisdrmstpWriter())
	            .taskExecutor(taskExecutor())
	            .build();
	}

	@Bean
	public Step exportGiscxmstpTable() {
	    return stepBuilderFactory.get("Step 5 -Giscxmstp_Table").<Giscxmstp, Giscxmstp>chunk(1000)
	            .reader(fetchGiscxmstpReader)
	            .writer(exportGiscxmstpWriter())
	            .taskExecutor(taskExecutor())
	            .build();
	}

	@Bean
	public Job job() {
	    return jobBuilderFactory.get("amosFeed").incrementer(new RunIdIncrementer())
	            .flow(executingQueriesStep())
	            .next(exportGiscxmstpTable())
	            .next(exportGisdrmstpTable())
	            .end()
	            .build();
	}

	@Bean
	public TaskExecutor taskExecutor() {
	    SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
	    taskExecutor.setConcurrencyLimit(10);
	    return taskExecutor;
	}

	public Step executingQueriesStep() {
		return stepBuilderFactory.get("ExecutableQueries").tasklet(executableQueries).build();
	}

}
