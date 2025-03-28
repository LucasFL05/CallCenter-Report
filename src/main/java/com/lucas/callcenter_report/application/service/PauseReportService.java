package com.lucas.callcenter_report.application.service;

import com.lucas.callcenter_report.domain.model.Pause;
import com.lucas.callcenter_report.domain.repository.PauseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PauseReportService {

    @Autowired
    private PauseRepository pauseRepository;

    @Autowired
    private PauseCSVParserService pauseCSVParserService;

    public void processFile(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            List<Pause> pauses = pauseCSVParserService.parsePauses(is);
            pauseRepository.saveAll(pauses);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao processar arquivo de pausas: " + e.getMessage(), e);
        }
    }



    public Map<String, Map<String, Object>> generatePauseReport() {
        List<Pause> pauses = pauseRepository.findAll();

        return pauses.stream()
                .filter(p -> p.getOperator() != null)
                .collect(Collectors.groupingBy(
                        Pause::getOperator,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateMetrics
                        )
                ));
    }

    public Map<String, Map<String, Map<String, Object>>> generateHourlyPauseReport() {
        List<Pause> pauses = pauseRepository.findAll();

        return pauses.stream()
                .filter(p -> p.getEnd() != null && p.getOperator() != null)
                .collect(Collectors.groupingBy(
                        Pause::getOperator,
                        Collectors.groupingBy(
                                p -> p.getEnd().getHour() + ":00",
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        this::calculateMetrics
                                )
                        )
                ));
    }

    private Map<String, Object> calculateMetrics(List<Pause> pauses) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("totalPauses", pauses.size());

        int totalDuration = pauses.stream()
                .filter(p -> p.getDuration() != null)
                .mapToInt(Pause::getDuration)
                .sum();
        metrics.put("totalDuration", totalDuration);

        Map<String, Integer> durationByType = pauses.stream()
                .filter(p -> p.getType() != null && p.getDuration() != null)
                .collect(Collectors.groupingBy(
                        Pause::getType,
                        Collectors.summingInt(Pause::getDuration)
                ));
        metrics.put("durationByType", durationByType);

        return metrics;
    }
}