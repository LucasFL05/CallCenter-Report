package com.lucas.callcenter_report.adapters.controller;

import com.lucas.callcenter_report.application.service.CallReportService;
import com.lucas.callcenter_report.application.service.PauseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private CallReportService callReportService;

    @Autowired
    private PauseReportService pauseReportService;

    @PostMapping("/calls")
    public ResponseEntity<Map<String, String>> uploadCallReport(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("error", "Arquivo vazio");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            callReportService.processFile(file);
            response.put("message", "Relatório de chamadas processado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Erro: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping(value = "/pauses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPauseReport(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("error", "Arquivo vazio");
                return ResponseEntity.badRequest().body(response);
            }

            pauseReportService.processFile(file);
            response.put("message", "Relatório de pausas processado com sucesso");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Falha no upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
