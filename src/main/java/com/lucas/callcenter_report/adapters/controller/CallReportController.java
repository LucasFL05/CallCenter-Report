package com.lucas.callcenter_report.adapters.controller;

import com.lucas.callcenter_report.domain.model.Call;
import com.lucas.callcenter_report.application.service.CallReportService;
import com.lucas.callcenter_report.domain.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/report")
public class CallReportController {


    @Autowired
    private CallReportService callReportService;

    @GetMapping(value = "/call", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getReportChamadas() {
        Map<String, Map<String, Object>> report = callReportService.gerarRelatorioChamadas();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html lang='pt'>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<title>Relatório de Chamadas</title>")
                .append(commonStyles())
                .append("</head>")
                .append("<body>")
                .append("<h1>Relatório de Chamadas por Consultor</h1>")
                .append("<div class='search-container'>")
                .append("<input type='text' id='searchReport' placeholder='Pesquisar...' onkeyup='searchTable(\"reportTable\", this.value)'>")
                .append("</div>")
                .append(buildSummaryTable(report))
                .append(sortingScript())
                .append(searchScript())
                .append("</body></html>");

        return ResponseEntity.ok(html.toString());
    }

    @GetMapping(value = "/dropped-calls", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getChamadasDerrubadas() {
        List<Call> chamadasDerrubadas = callReportService.listarChamadasDerrubadas();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html lang='pt'>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<title>Chamadas Derrubadas</title>")
                .append(commonStyles())
                .append("</head>")
                .append("<body>")
                .append("<h1>Detalhes das Chamadas Derrubadas</h1>")
                .append("<div class='search-container'>")
                .append("<input type='text' id='searchDropped' placeholder='Pesquisar...' onkeyup='searchTable(\"droppedTable\", this.value)'>")
                .append("</div>")
                .append(buildDroppedCallsTable(chamadasDerrubadas))
                .append(sortingScript())
                .append(searchScript())
                .append("</body></html>");

        return ResponseEntity.ok(html.toString());
    }

    private String commonStyles() {
        return "<style>" +
                "table { width: 100%; border-collapse: collapse; margin-top: 20px; }" +
                "th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }" +
                "th { background-color: #4CAF50; color: white; cursor: pointer; position: relative; }" +
                "th:hover { background-color: #45a049; }" +
                "tr:nth-child(even) { background-color: #f2f2f2; }" +
                ".sort-indicator { position: absolute; right: 8px; }" +
                ".asc::after { content: '↑'; color: #fff; }" +
                ".desc::after { content: '↓'; color: #fff; }" +
                "tr.operator { background-color: #ffebee; }" +         // Vermelho claro
                "tr.client { background-color: #e8f5e9; }" +           // Verde claro
                "tr.operator td { color: #b71c1c; }" +                 // Vermelho escuro
                "tr.client td { color: #1b5e20; }" +                   // Verde escuro
                ".search-container { margin: 20px 0; }" +
                "input[type='text'] { width: 100%; padding: 12px 20px; margin: 8px 0; " +
                "box-sizing: border-box; border: 2px solid #ccc; border-radius: 4px; " +
                "font-size: 16px; }" +
                ".status-item { margin: 2px 0; padding: 4px; background-color: #e3f2fd; border-radius: 4px; }" +
                "</style>";
    }

    private String buildSummaryTable(Map<String, Map<String, Object>> report) {
        StringBuilder table = new StringBuilder();
        table.append("<table id='reportTable'>")
                .append("<thead>")
                .append("<tr>")
                .append("<th onclick='sortTable(0)'>Atendente<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(1)'>Total<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(2)'>Derrubadas Operador<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(3)'>Derrubadas Cliente<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(4)'>Chamadas >40s<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(5)'>Status Detalhado<span class='sort-indicator'></span></th>") // Nova coluna
                .append("<th onclick='sortTable(6)'>Total Horas<span class='sort-indicator'></span></th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");

        report.forEach((atendente, metrics) -> {
            double totalHoras = (Double) metrics.get("totalHorasFaladas");
            int totalSeconds = (int) (totalHoras * 3600);
            Map<String, Integer> statusCounts = (Map<String, Integer>) metrics.get("statusCounts"); // Supondo que o serviço retorne este mapa

            table.append("<tr>")
                    .append("<td>").append(atendente).append("</td>")
                    .append("<td data-sort='").append(metrics.get("totalChamadas")).append("'>")
                    .append(metrics.get("totalChamadas")).append("</td>")
                    .append("<td data-sort='").append(metrics.get("chamadasDerrubadasOperador")).append("'>")
                    .append(metrics.get("chamadasDerrubadasOperador")).append("</td>")
                    .append("<td data-sort='").append(metrics.get("chamadasDerrubadasCliente")).append("'>")
                    .append(metrics.get("chamadasDerrubadasCliente")).append("</td>")
                    .append("<td data-sort='").append(metrics.get("chamadasAcima40Segundos")).append("'>")
                    .append(metrics.get("chamadasAcima40Segundos")).append("</td>")
                    .append("<td>"); // Nova célula para status detalhado

            if (statusCounts != null) {
                statusCounts.forEach((status, count) -> {
                    table.append("<div class='status-item'>")
                            .append(status).append(": ").append(count)
                            .append("</div>");
                });
            }

            table.append("</td>")
                    .append("<td data-sort='").append(totalSeconds).append("'>")
                    .append(TimeUtils.formatDuration(totalSeconds)).append("</td>")
                    .append("</tr>");
        });

        table.append("</tbody></table>");
        return table.toString();
    }

    private String buildDroppedCallsTable(List<Call> chamadas) {
        StringBuilder table = new StringBuilder();
        table.append("<table id='droppedTable'>")
                .append("<thead>")
                .append("<tr>")
                .append("<th onclick='sortTable(0)'>Atendente<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(1)'>CPF<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(2)'>Telefone<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(3)'>Duração (s)<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(4)'>Status<span class='sort-indicator'></span></th>") // Nova coluna
                .append("<th onclick='sortTable(5)'>Quem Desligou<span class='sort-indicator'></span></th>")
                .append("<th onclick='sortTable(6)'>Hora<span class='sort-indicator'></span></th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");

        chamadas.forEach(chamada -> {
            String estilo = "Operador".equalsIgnoreCase(chamada.getQuemDesligou()) ?
                    "operator" : "client";
            String horaFormatada = chamada.getHora() != null ?
                    chamada.getHora().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) : "";

            table.append("<tr class='").append(estilo).append("'>")
                    .append("<td>").append(chamada.getAtendente()).append("</td>")
                    .append("<td data-sort='").append(chamada.getCpf()).append("'>")
                    .append((chamada.getCpf())).append("</td>")
                    .append("<td data-sort='").append(chamada.getTelefone()).append("'>")
                    .append(formatTelefone(chamada.getTelefone())).append("</td>")
                    .append("<td data-sort='").append(chamada.getTempo()).append("'>")
                    .append(formatDuration(chamada.getTempo())).append("</td>")
                    .append("<td>").append(chamada.getStatus()).append("</td>") // Novo campo status
                    .append("<td>").append(chamada.getQuemDesligou()).append("</td>")
                    .append("<td data-sort='").append(horaFormatada).append("'>")
                    .append(formatDateTime(chamada.getHora())).append("</td>")
                    .append("</tr>");
        });

        table.append("</tbody></table>");
        return table.toString();
    }

    private String sortingScript() {
        return "<script>" +
                "let currentSortColumn = null;" +
                "let sortDirection = 'asc';" +
                "function sortTable(columnIndex) {" +
                "  const table = event.target.closest('table');" +
                "  const tbody = table.tBodies[0];" +
                "  const rows = Array.from(tbody.rows);" +
                "  Array.from(table.tHead.rows[0].cells).forEach(th => th.classList.remove('asc', 'desc'));" +
                "  if (currentSortColumn === columnIndex) {" +
                "    sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';" +
                "  } else {" +
                "    currentSortColumn = columnIndex;" +
                "    sortDirection = 'asc';" +
                "  }" +
                "  table.tHead.rows[0].cells[columnIndex].classList.add(sortDirection);" +
                "  const compare = (a, b) => {" +
                "    const aVal = a.cells[columnIndex].dataset.sort || a.cells[columnIndex].textContent;" +
                "    const bVal = b.cells[columnIndex].dataset.sort || b.cells[columnIndex].textContent;" +
                "    const isNumeric = !isNaN(aVal) && !isNaN(bVal);" +
                "    if (isNumeric) {" +
                "      return sortDirection === 'asc' ? aVal - bVal : bVal - aVal;" +
                "    }" +
                "    return sortDirection === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);" +
                "  };" +
                "  rows.sort(compare);" +
                "  tbody.innerHTML = '';" +
                "  rows.forEach(row => tbody.appendChild(row));" +
                "}" +
                "</script>";
    }

    private String searchScript() {
        return "<script>" +
                "function searchTable(tableId, query) {" +
                "  const table = document.getElementById(tableId);" +
                "  const tbody = table.tBodies[0];" +
                "  const rows = Array.from(tbody.rows);" +
                "  query = query.toLowerCase();" +
                "  rows.forEach(row => {" +
                "    const cells = Array.from(row.cells);" +
                "    const match = cells.some(cell => " +
                "      cell.textContent.toLowerCase().includes(query)" +
                "    );" +
                "    row.style.display = match ? '' : 'none';" +
                "  });" +
                "}" +
                "</script>";
    }

    private String formatDuration(Integer seconds) {
        return seconds != null ? TimeUtils.formatDuration(seconds) : "N/A";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ?
                dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A";
    }

    private String formatTelefone(String telefone) {
        if (telefone == null) return "N/A";
        String cleaned = telefone.replaceAll("\\D", "");
        if (cleaned.length() == 11) {
            return cleaned.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        }
        return cleaned.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
    }
}