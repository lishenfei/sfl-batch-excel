package com.lsf.batch.excel.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.poi.PoiItemReader;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.item.validator.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by lishenfei on 2017-01-10.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    public static final String BASE_PACKAGE = "com.lsf.batch.excel.domain";

    @Bean
    @StepScope
    public ItemReader reader(@Value("#{jobParameters['filePath']}") String pathToFile,
                             @Value("#{jobParameters['targetType']}") String targetType,
                             @Value("#{jobParameters['linesToSkip']}") Long linesToSkip,
                             @Value("#{jobParameters['rowMapperClassName']}") String rowMapperClassName) throws Exception {
        PoiItemReader itemReader = new PoiItemReader();
        itemReader.setLinesToSkip(linesToSkip.intValue());
        itemReader.setResource(new FileSystemResource(pathToFile));
        itemReader.setRowMapper(StringUtils.isEmpty(rowMapperClassName)
                ? new BatchRowMapper(Class.forName(targetType))
                : (RowMapper) Class.forName(rowMapperClassName).newInstance());
        itemReader.afterPropertiesSet();
        itemReader.open(new ExecutionContext());
        return itemReader;
    }

    @Bean
    @StepScope
    public ItemProcessor processor(@Value("#{jobParameters['processorClassName']}") String processorClassName,
                                   Validator<Object> batchBeanValidator) throws Exception {
        ValidatingItemProcessor processor = (StringUtils.isEmpty(processorClassName))
                ? new ValidatingItemProcessor() : (ValidatingItemProcessor) Class.forName(processorClassName).newInstance();
        processor.setValidator(batchBeanValidator);
        return processor;
    }

    @Bean
    public ItemWriter writer(DataSource dataSource, LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaItemWriter writer = new JpaItemWriter();
        writer.setEntityManagerFactory(entityManagerFactory.getObject());
        return writer;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setPackagesToScan(BASE_PACKAGE);
        lef.setDataSource(dataSource);
        lef.setJpaVendorAdapter(jpaVendorAdapter);
        lef.setJpaProperties(new Properties());
        return lef;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.MYSQL);
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(true);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
        return jpaVendorAdapter;
    }

    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDataSource(dataSource);
        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
        jobRepositoryFactoryBean.setDatabaseType(Database.MYSQL.name());
        return jobRepositoryFactoryBean.getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository(dataSource, transactionManager));
        return jobLauncher;
    }

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory, ItemReader reader, ItemProcessor processor, ItemWriter writer) {
        return stepBuilderFactory
                .get("step")
                .chunk(1000)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importJob(JobBuilderFactory jobs, Step step, BatchJobListener batchJobListener) {
        return jobs.get("importJob")
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .listener(batchJobListener)
                .build();
    }

    @Bean
    public BatchJobListener batchJobListener() {
        return new BatchJobListener();
    }

    @Bean
    public Validator<Object> batchBeanValidator() {
        return new BatchBeanValidator<>();
    }

}
