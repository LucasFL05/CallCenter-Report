package com.lucas.callcenter_report.adapters.controller;

import com.lucas.callcenter_report.application.service.DataCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteAllController {

    private final DataCleanupService dataCleanupService;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    public DeleteAllController(DataCleanupService dataCleanupService) {
        this.dataCleanupService = dataCleanupService;
    }

    @PostMapping("/delete/all")
    public void deleteAllData() {
        logger.info("Todos os dados foram excluídos com sucesso.");
        dataCleanupService.deleteAllData();
    }
}