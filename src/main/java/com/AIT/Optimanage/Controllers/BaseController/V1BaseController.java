package com.AIT.Optimanage.Controllers.BaseController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public abstract class V1BaseController {

    protected <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }

    protected <T> ResponseEntity<T> created(T body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    protected ResponseEntity<Map<String, String>> badRequest(String mensagem) {
        return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem));
    }
}
