package com.microservices.recommendationservice;

import com.microservices.api.core.recommendation.RecommendationService;
import com.microservices.recommendationservice.persistence.RecommendationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com")
@Slf4j
public class RecommendationServiceApplication {

	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(RecommendationServiceApplication.class, args);
		RecommendationRepository repository = context.getBean(RecommendationRepository.class);
		log.info(repository.toString());
		RecommendationService service = context.getBean(RecommendationService.class);
		log.info(service.toString());
	}

}
