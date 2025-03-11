package com.lucas.callcenter_report.adapters.controller;

import com.lucas.callcenter_report.application.service.DataCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteAllController {

    private final DataCleanupService dataCleanupService;

    @Autowired
    public DeleteAllController(DataCleanupService dataCleanupService) {
        this.dataCleanupService = dataCleanupService;
    }

    @PostMapping("/delete/all")
    public void deleteAllData() {
        dataCleanupService.deleteAllData();
    }
}