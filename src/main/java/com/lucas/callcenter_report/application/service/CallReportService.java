package com.lucas.callcenter_report.application.service;

import com.lucas.callcenter_report.domain.model.Call;
import com.lucas.callcenter_report.domain.repository.CallRepository;
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
public class CallReportService {

    @Autowired
    private CallRepository callRepository;

    @Autowired
    private CallCSVParserService callCSVParserService;

    public void processFile(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            List<Call> calls = callCSVParserService.parseCalls(is);
            callRepository.saveAll(calls);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao processar arquivo de chamadas: " + e.getMessage(), e);
        }
    }

    public Map<String, Map<String, Object>> gerarRelatorioChamadas() {
        List<Call> chamadas = callRepository.findAll();

        return chamadas.stream()
                .filter(c -> c.getAtendente() != null)
                .collect(Collectors.groupingBy(
                        Call::getAtendente,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calcularMetricas
                        )
                ));
    }

    public List<Call> listarChamadasDerrubadas() {
        return callRepository.findAll().stream()
                .filter(c -> c.getQuemDesligou() != null &&
                        ("Operador".equalsIgnoreCase(c.getQuemDesligou()) ||
                                "Cliente".equalsIgnoreCase(c.getQuemDesligou())))
                .filter(c -> c.getTempo() != null)
                .collect(Collectors.toList());
    }

    private Map<String, Object> calcularMetricas(List<Call> chamadas) {
        Map<String, Object> metrics = new HashMap<>();

        // Total de chamadas
        metrics.put("totalChamadas", chamadas.size());

        // Chamadas derrubadas pelo operador
        long chamadasDerrubadasOperador = chamadas.stream()
                .filter(c -> "Operador".equalsIgnoreCase(c.getQuemDesligou()))
                .count();
        metrics.put("chamadasDerrubadasOperador", chamadasDerrubadasOperador);

        // Chamadas derrubadas pelo cliente
        long chamadasDerrubadasCliente = chamadas.stream()
                .filter(c -> "Cliente".equalsIgnoreCase(c.getQuemDesligou()))
                .count();
        metrics.put("chamadasDerrubadasCliente", chamadasDerrubadasCliente);

        // Chamadas com duração acima de 40 segundos
        long chamadasAcima40Segundos = chamadas.stream()
                .filter(c -> c.getTempo() != null && c.getTempo() > 40)
                .count();
        metrics.put("chamadasAcima40Segundos", chamadasAcima40Segundos);

        // Total de horas faladas
        double totalHorasFaladas = chamadas.stream()
                .filter(c -> c.getTempo() != null)
                .mapToInt(Call::getTempo)
                .sum() / 3600.0;
        metrics.put("totalHorasFaladas", totalHorasFaladas);

        // Contagem de status de chamadas
        Map<String, Integer> statusCounts = chamadas.stream()
                .filter(c -> c.getStatus() != null)
                .collect(Collectors.groupingBy(
                        Call::getStatus,
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::intValue
                        )
                ));
        metrics.put("statusCounts", statusCounts);

        return metrics;
    }
}