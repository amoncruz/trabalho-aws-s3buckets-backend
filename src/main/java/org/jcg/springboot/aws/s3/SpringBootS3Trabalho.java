package org.jcg.springboot.aws.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main implementation class which serves two purpose in a spring boot application: Configuration and bootstrapping.
 * @author yatin-batra
 */
@SpringBootApplication
public class SpringBootS3Trabalho {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootS3Trabalho.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootS3Trabalho.class, args);
		LOGGER.info("SpringBootS3Trabalho application started successfully.");
	}
}
