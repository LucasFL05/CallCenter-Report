package com.lucas.callcenter_report.application.service;

import com.lucas.callcenter_report.domain.model.Pause;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PauseCSVParserService {

    // Formato para hora (HH:mm:ss)
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Data fixa (apenas para criar LocalDateTime, já que a data real é irrelevante)
    private static final LocalDate DUMMY_DATE = LocalDate.of(1970, 1, 1); // Data fictícia

    public List<Pause> parsePauses(InputStream is) {
        List<Pause> pauses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(';')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .withIgnoreEmptyLines()
                     .withNullString(""))) {

            System.out.println("Cabeçalhos detectados: " + csvParser.getHeaderNames());

            for (CSVRecord record : csvParser) {
                try {
                    Pause pause = new Pause();

                    // Ignora Data_INICIO e DATA_FIM. Lê apenas os horários de início e fim.
                    String inicioPausaStr = record.isSet("INICIO_PAUSA") ? record.get("INICIO_PAUSA") : null;
                    String finalPausaStr = record.isSet("FINAL_PAUSA") ? record.get("FINAL_PAUSA") : null;

                    // Parse dos horários (ignorando a data)
                    LocalDateTime startDateTime = parseTime(inicioPausaStr);
                    LocalDateTime endDateTime = parseTime(finalPausaStr);

                    // Atribui os valores à pausa
                    pause.setStart(startDateTime);
                    pause.setEnd(endDateTime);
                    pause.setOperator(record.isSet("OPERADOR") ? record.get("OPERADOR").trim() : null);
                    pause.setType(record.isSet("PAUSA") ? record.get("PAUSA").trim() : null);

                    // Calcula a duração
                    if (startDateTime != null && endDateTime != null) {
                        long durationSeconds = Duration.between(startDateTime, endDateTime).getSeconds();
                        pause.setDuration((int) durationSeconds);
                    }

                    pauses.add(pause);

                } catch (Exception e) {
                    System.err.println("[ERRO] Linha " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar CSV: " + e.getMessage(), e);
        }

        return pauses;
    }

    private LocalDateTime parseTime(String timeStr) {
        try {
            if (timeStr == null || timeStr.trim().isEmpty()) {
                System.err.println("[ERRO] Horário ausente.");
                return null;
            }

            LocalTime time = LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
            return LocalDateTime.of(DUMMY_DATE, time); // Combina com a data fictícia

        } catch (DateTimeParseException e) {
            System.err.println("[ERRO] Formato inválido: " + timeStr + " (Esperado: HH:mm:ss)");
            return null;
        }
    }
}