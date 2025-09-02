package com.AIT.Optimanage.Support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
@Profile("dev")
public class PostmanOnStartupRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(PostmanOnStartupRunner.class);

    private final ApiExportService apiExportService;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public PostmanOnStartupRunner(ApiExportService apiExportService) {
        this.apiExportService = apiExportService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Build base URL using localhost + configured port + context path
        String baseUrl = "http://localhost:" + serverPort + (contextPath != null ? contextPath : "");
        try {
            Map<String, Object> collection = apiExportService.generatePostmanCollection(baseUrl);
            Path out = Path.of("src", "main", "resources", "postman.json");
            Files.createDirectories(out.getParent());
            mapper.writeValue(out.toFile(), collection);
            log.info("Postman collection written to {}", out.toAbsolutePath());
        } catch (IOException ex) {
            log.warn("Failed to write Postman collection: {}", ex.getMessage());
        } catch (Exception e) {
            log.warn("Unexpected error generating Postman collection: {}", e.getMessage());
        }
    }
}

