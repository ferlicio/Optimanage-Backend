package com.AIT.Optimanage.Controllers.Admin;

import com.AIT.Optimanage.Support.ApiExportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class ApiExportController {

    private final ApiExportService exportService;

    public ApiExportController(ApiExportService exportService) { this.exportService = exportService; }

    @GetMapping(value = "/postman-collection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> exportPostman(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        Map<String, Object> collection = exportService.generatePostmanCollection(baseUrl);
        return ResponseEntity.ok(collection);
    }

    @GetMapping(value = "/api-docs")
    public ResponseEntity<Void> openApiRedirect(HttpServletRequest request) {
        String target = request.getContextPath() + "/v3/api-docs";
        return ResponseEntity.status(302).header("Location", target).build();
    }

    // Controller now delegates to ApiExportService
}
