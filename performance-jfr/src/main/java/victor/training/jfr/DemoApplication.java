package victor.training.jfr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@EnableScheduling
@SpringBootApplication
@EnableMBeanExport
public class DemoApplication {
	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}

@Component
class FileProcessor {
	private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);
	@Value("${minWaitThresholdForLogging}")
	private long minWaitThresholdForLogging;

	public void processFile(int fileId, long sumbmitTime) {
		long startTime = System.currentTimeMillis();
		if (startTime - sumbmitTime > minWaitThresholdForLogging) {
			log.info("Waited in queue {} seconds" , (startTime-sumbmitTime)/1000);
		}
		log.info("Start processing file {}", fileId);
		ThreadUtils.sleepq(5_000); //network
		Tasks.cpu(5_000);
		log.info("End {}", fileId);
	}
}

@RestController
class R1 {
	private static final Logger log = LoggerFactory.getLogger(R1.class);

	@GetMapping("jfr")
	public String hello() throws InterruptedException {
		CheckStockEvent event = new CheckStockEvent();
		event.begin();

		int[] ints = new int[1000];
		System.out.println(ints.length);

		Thread.sleep(1000);
		if (event.isEnabled()) {
			event.commit();
		}
		return "Hello";
	}
}
