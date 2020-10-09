package victor.training.performance.batch.sync;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;

import static victor.training.performance.ConcurrencyUtil.measureCall;


@SpringBootApplication
@EnableBatchProcessing
public class BatchBasicApp {

    @Value("${chunk.size}")
    private int chunkSize;

    public static void main(String[] args) throws IOException {
        DataFileGenerator.generateFile(10_000);
        int dt = measureCall(() -> SpringApplication.run(BatchBasicApp.class, new String[]{"param1=xx"}).close());
        System.out.println("Batch took " + dt + " ms");
    }

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    public Job basicJob() {
        return jobBuilderFactory.get("basicJob")
            .incrementer(new RunIdIncrementer())
            .start(basicChunkStep())
            .listener(jobListener())
            .build();
    }

    @Bean
    @JobScope
    public MyJobListener jobListener() {
        return new MyJobListener();
    }

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    public Step basicChunkStep() {
        return stepBuilderFactory.get("basicChunkStep")
                .<MyEntityFileRecord, MyEntity>chunk(chunkSize)
                .reader(xmlReader())
                .processor(processor())
                .writer(jpaWriter())
                .listener(new MyChunkListener())
                .listener(new MyStepExecutionListener())
                .build();
    }

    private ItemReader<MyEntityFileRecord> xmlReader() {
        StaxEventItemReader<MyEntityFileRecord> reader = new StaxEventItemReader<>();
        reader.setResource(new FileSystemResource("data.xml")); // TODO parameterize
        reader.setFragmentRootElementName("data");
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setClassesToBeBound(MyEntityFileRecord.class);;
        reader.setUnmarshaller(unmarshaller);
        return reader;
    }

    @Bean
    @StepScope
    public MyEntityProcessor processor() {
        return new MyEntityProcessor();
    }

    @Autowired
    private EntityManagerFactory emf;

    private JpaItemWriter<MyEntity> jpaWriter() {
        JpaItemWriter<MyEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }
}

