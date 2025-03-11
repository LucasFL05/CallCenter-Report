package com.lucas.callcenter_report.application.service;

import com.lucas.callcenter_report.domain.repository.CallRepository;
import com.lucas.callcenter_report.domain.repository.PauseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataCleanupService {

    private final CallRepository callRepository;
    private final PauseRepository pauseRepository;

    @Autowired
    public DataCleanupService(CallRepository callRepository, PauseRepository pauseRepository) {
        this.callRepository = callRepository;
        this.pauseRepository = pauseRepository;
    }

    @Transactional
    public void deleteAllData() {
        callRepository.deleteAll();
        pauseRepository.deleteAll();
    }
}