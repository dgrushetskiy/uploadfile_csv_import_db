package ru.gothmog.importdbuors.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.FileSystemResource;
import ru.gothmog.importdbuors.mapper.ImportDataPreparedStatementSetter;
import ru.gothmog.importdbuors.model.ImportData;
import ru.gothmog.importdbuors.service.FileStorageService;

import javax.sql.DataSource;

@Configuration
public class ImportJobConfig {
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("dataSource")
    public DataSource dataSource;

    @Bean
    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public FlatFileItemReader<ImportData> importDataReader(@Value("#{jobParameters[fullPathFileName]}") String pathToFile){
        FlatFileItemReader<ImportData> reader = new FlatFileItemReader<>();


        reader.setResource(new FileSystemResource(pathToFile));
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper<ImportData>() {{
            setLineTokenizer(new DelimitedLineTokenizer(){{
                setNames(new String[]{"idLocality","idKeyInformName","valueAll","reportingPeriod"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<ImportData>(){{
                setTargetType(ImportData.class);
            }});
        }});
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<ImportData> writer(){
        String sql = "insert into import_data(id_locality, id_key_inform_name, value_all, reporting_period) VALUES (?,?,?,?)";
        JdbcBatchItemWriter<ImportData> writerImportData = new JdbcBatchItemWriter<>();
        writerImportData.setDataSource(dataSource);
        writerImportData.setSql(sql);
        ItemPreparedStatementSetter<ImportData> statementSetter = new ImportDataPreparedStatementSetter();
        writerImportData.setItemPreparedStatementSetter(statementSetter);
        return writerImportData;
    }
    @Bean(name = "importDataJob")
    @Primary
    public Job importDataJob(ItemReader<ImportData> importDataReader){
        return jobBuilderFactory.get("importDataJob")
                .incrementer(new RunIdIncrementer())
                .flow(stepImportData(importDataReader))
                .end()
                .build();
    }
    @Bean
    public Step stepImportData(ItemReader<ImportData> importDataReader){
        return stepBuilderFactory.get("stepImportData").<ImportData,ImportData>chunk(100)
                .reader(importDataReader)
                .writer(writer())
                .build();
    }
}
