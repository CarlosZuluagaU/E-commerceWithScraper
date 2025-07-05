package com.portafolio.webscraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableRetry
public class WebScraperApplication {

    private static final Logger logger = LoggerFactory.getLogger(WebScraperApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication.run(WebScraperApplication.class, args);
        } catch (Exception e) {
            logger.error("Error al iniciar la aplicación", e);
            System.exit(1);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncScraper-");
        executor.initialize();
        return executor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        logger.info("╔════════════════════════════════════════════════╗");
        logger.info("║   APLICACIÓN WEB SCRAPER INICIADA CORRECTAMENTE ║");
        logger.info("╠════════════════════════════════════════════════╣");
        logger.info("║ Modo Scheduling: Activado                      ║");
        logger.info("║ Modo Async: Activado                           ║");
        logger.info("║ Modo Retry: Activado                          ║");
        logger.info("╚════════════════════════════════════════════════╝");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkInitialConfiguration() {
        logger.info("Configuración inicial verificada");
        logger.info("Perfil activo: {}", System.getProperty("spring.profiles.active", "default"));
    }
}