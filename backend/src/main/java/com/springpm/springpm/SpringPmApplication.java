package com.springpm.springpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.springpm.springpm.repository")
@EnableTransactionManagement
@ComponentScan(basePackages = "com.springpm.springpm")
public class SpringPmApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringPmApplication.class, args);
	}

}
