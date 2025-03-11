package com.lucas.callcenter_report.application.service;

import com.lucas.callcenter_report.domain.model.Call;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

@Service
public class CallCSVParserService {

    // Formatter flexível para todos os formatos reportados
    private static final DateTimeFormatter FLEXIBLE_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)    // Dia (1 ou 2 dígitos)
            .appendLiteral('/')
            .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)   // Mês (1 ou 2 dígitos)
            .appendLiteral('/')
            .appendValue(ChronoField.YEAR, 4)                                  // Ano com 4 dígitos
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NORMAL)     // Hora (1 ou 2 dígitos)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 1, 2, SignStyle.NORMAL)  // Minuto (1 ou 2 dígitos)
            .optionalStart()                                                   // Segundos opcionais
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 1, 2, SignStyle.NORMAL)// Segundo (1 ou 2 dígitos)
            .optionalEnd()
            .toFormatter();

    public List<Call> parseCalls(InputStream is) {
        List<Call> calls = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(';')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .withAllowMissingColumnNames()
                     .withIgnoreEmptyLines()
                     .withNullString(""))) {

            for (CSVRecord record : csvParser) {
                try {
                    Call call = new Call();

                    // Extração dos campos
                    String horaStr = record.get("Hora");
                    String campanha = record.get("Campanha");
                    String cpf = record.get("CPF");
                    String telefone = record.get("Telefone");
                    String esperaStr = record.get("Espera");
                    String atendente = record.get("Atendente");
                    String tempoStr = record.get("Tempo");
                    String status = record.get("Status");
                    String quemDesligou = record.get("Quem Desligou");

                    // Parse dos campos especiais
                    LocalDateTime hora = parseDateTime(horaStr);
                    Integer espera = parseSafeInteger(esperaStr);
                    Integer tempo = parseSafeInteger(tempoStr);

                    // Atribuição ao objeto Call
                    call.setHora(hora);
                    call.setCampanha(campanha);
                    call.setCpf(cpf);
                    call.setTelefone(telefone);
                    call.setEspera(espera);
                    call.setAtendente(atendente);
                    call.setTempo(tempo);
                    call.setStatus(status);
                    call.setQuemDesligou(quemDesligou);

                    calls.add(call);

                } catch (Exception e) {
                    System.err.println("[ERRO] Linha " + record.getRecordNumber() + ": " + e.getMessage());
                    System.err.println("Registro problemático: " + record);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro fatal ao processar CSV: " + e.getMessage(), e);
        }

        return calls;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            System.err.println("[AVISO] Campo 'Hora' vazio");
            return null;
        }

        try {
            // Padroniza a string (remove espaços múltiplos e formatação inconsistente)
            String cleaned = dateTimeStr.trim()
                    .replaceAll("\\s+", " ")   // Espaços múltiplos -> único espaço
                    .replaceAll("(?<=\\d):(?=\\d)", ":") // Remove espaços entre os ":"
                    .replaceAll("[/](?=\\d)", "/");      // Remove espaços após "/"

            return LocalDateTime.parse(cleaned, FLEXIBLE_DATE_TIME_FORMATTER);

        } catch (DateTimeParseException e) {
            System.err.println("[ERRO CRÍTICO] Formato inválido para 'Hora': " + dateTimeStr);
            System.err.println("Causa: " + e.getMessage());
            return null;
        }
    }

    private Integer parseSafeInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("[AVISO] Valor não numérico ignorado: '" + value + "'");
            return null;
        }
    }
}