 package com.lucas.callcenter_report.adapters.controller;

import com.lucas.callcenter_report.application.service.PauseReportService;
import com.lucas.callcenter_report.domain.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.TreeMap;

@Controller
@RequestMapping("/report")
public class PauseReportController {

    @Autowired
    private PauseReportService reportService;

    @GetMapping(value = "/pause", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getReportPausas() {
        Map<String, Map<String, Object>> report = reportService.generatePauseReport();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html lang='pt'>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<title>Relatório de Pausas por Operador</title>")
                .append("<style>")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
                .append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }")
                .append("th { background-color: #4CAF50; color: white; cursor: pointer; position: relative; }")
                .append("th:hover { background-color: #45a049; }")
                .append("tr:nth-child(even) { background-color: #f2f2f2; }")
                .append(".sort-indicator { position: absolute; right: 8px; }")
                .append(".asc::after { content: '↑'; color: #fff; }")
                .append(".desc::after { content: '↓'; color: #fff; }")
                .append(".duration-type { margin: 2px 0; padding: 4px; background-color: #f8f8f8; }")
                .append(".search-container { margin: 20px 0; }")
                .append("input[type='text'] { width: 100%; padding: 12px 20px; margin: 8px 0; ")
                .append("box-sizing: border-box; border: 2px solid #ccc; border-radius: 4px; ")
                .append("font-size: 16px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<h1>Relatório de Pausas por Operador</h1>")
                .append("<div class='search-container'>")
                .append("<input type='text' id='searchPause' placeholder='Pesquisar...' onkeyup='filterTable()'>")
                .append("</div>");

        if (report.isEmpty()) {
            html.append("<p>Nenhum dado disponível</p>");
        } else {
            html.append("<table id='reportTable'>")
                    .append("<thead>")
                    .append("<tr>")
                    .append("<th onclick='sortTable(0)'>Operador<span class='sort-indicator'></span></th>")
                    .append("<th onclick='sortTable(1)'>Total de Pausas<span class='sort-indicator'></span></th>")
                    .append("<th onclick='sortTable(2)'>Duração Total<span class='sort-indicator'></span></th>")
                    .append("<th onclick='sortTable(3)'>Duração por Tipo<span class='sort-indicator'></span></th>")
                    .append("</tr>")
                    .append("</thead>")
                    .append("<tbody>");

            report.forEach((operador, metrics) -> {
                int totalPauses = (int) metrics.getOrDefault("totalPauses", 0);
                int totalDuration = (int) metrics.getOrDefault("totalDuration", 0);

                html.append("<tr>")
                        .append("<td>").append(operador).append("</td>")
                        .append("<td data-sort='").append(totalPauses).append("'>").append(totalPauses).append("</td>")
                        .append("<td data-sort='").append(totalDuration).append("'>")
                        .append(TimeUtils.formatDuration(totalDuration)).append("</td>")
                        .append("<td>");

                Object durationByType = metrics.get("durationByType");
                if (durationByType instanceof Map) {
                    ((Map<?, ?>) durationByType).forEach((type, duration) -> {
                        int seconds = (duration instanceof Integer) ? (int) duration : 0;
                        html.append("<div class='duration-type'>")
                                .append(type).append(": ").append(TimeUtils.formatDuration(seconds))
                                .append("</div>");
                    });
                }
                html.append("</td></tr>");
            });
            html.append("</tbody></table>");
        }

        html.append("<script>")
                .append("let currentSortColumn = null;")
                .append("let sortDirection = 'asc';")
                .append("function sortTable(columnIndex) {")
                .append("  const table = document.getElementById('reportTable');")
                .append("  const tbody = table.tBodies[0];")
                .append("  const rows = Array.from(tbody.rows);")
                .append("  Array.from(table.tHead.rows[0].cells).forEach(th => th.classList.remove('asc', 'desc'));")
                .append("  if (currentSortColumn === columnIndex) {")
                .append("    sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';")
                .append("  } else {")
                .append("    currentSortColumn = columnIndex;")
                .append("    sortDirection = 'asc';")
                .append("  }")
                .append("  table.tHead.rows[0].cells[columnIndex].classList.add(sortDirection);")
                .append("  const compare = (a, b) => {")
                .append("    const aVal = a.cells[columnIndex].dataset.sort || a.cells[columnIndex].textContent;")
                .append("    const bVal = b.cells[columnIndex].dataset.sort || b.cells[columnIndex].textContent;")
                .append("    const isNumeric = !isNaN(aVal) && !isNaN(bVal);")
                .append("    if (isNumeric) {")
                .append("      return sortDirection === 'asc' ? aVal - bVal : bVal - aVal;")
                .append("    }")
                .append("    return sortDirection === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);")
                .append("  };")
                .append("  rows.sort(compare);")
                .append("  tbody.innerHTML = '';")
                .append("  rows.forEach(row => tbody.appendChild(row));")
                .append("}")
                .append("function filterTable() {")
                .append("  const input = document.getElementById('searchPause');")
                .append("  const filter = input.value.toUpperCase();")
                .append("  const table = document.getElementById('reportTable');")
                .append("  const rows = table.tBodies[0].rows;")
                .append("  for (let i = 0; i < rows.length; i++) {")
                .append("    const cells = rows[i].cells;")
                .append("    let show = false;")
                .append("    for (let j = 0; j < cells.length; j++) {")
                .append("      if (cells[j].textContent.toUpperCase().includes(filter)) {")
                .append("        show = true;")
                .append("        break;")
                .append("      }")
                .append("    }")
                .append("    rows[i].style.display = show ? '' : 'none';")
                .append("  }")
                .append("}")
                .append("</script>")
                .append("</body></html>");

        return ResponseEntity.ok(html.toString());
    }
       

     @GetMapping(value = "/hourly-pauses", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getHourlyPauseReport() {
        Map<String, Map<String, Map<String, Object>>> report = reportService.generateHourlyPauseReport();

        StringBuilder html = new StringBuilder();
        appendHtmlHeader(html, "Relatório de Pausas por Hora e Consultor");

        // Barra de pesquisa global (filtra consultores)
        html.append("<div class='search-container'>")
                 .append("<input type='text' id='searchConsultor' placeholder='Pesquisar consultor...' onkeyup='filterConsultores(this.value)'>")
                .append("</div>");

        if (report.isEmpty()) {
            html.append("<p>Nenhum dado disponível</p>");
        } else {
            // Ordenar consultores alfabeticamente
            Map<String, Map<String, Map<String, Object>>> sortedReport = new TreeMap<>(report);

            sortedReport.forEach((consultor, hourlyReport) -> {
                // Tabela por consultor
                html.append("<div class='consultor-table' data-consultor='").append(consultor).append("'>")
                        .append("<h2>Consultor: ").append(consultor).append("</h2>")
                        .append("<div class='search-container'>")
                        .append("<input type='text' placeholder='Pesquisar nesta tabela...' onkeyup='filterTable(this, \"").append(consultor).append("\")'>")
                        .append("</div>")
                        .append("<table id='table-").append(consultor).append("'>")
                        .append("<thead>")
                        .append("<tr>")
                        .append("<th onclick='sortTable(\"").append(consultor).append("\", 0)'>Hora<span class='sort-indicator'></span></th>")
                        .append("<th onclick='sortTable(\"").append(consultor).append("\", 1)'>Total de Pausas<span class='sort-indicator'></span></th>")
                        .append("<th onclick='sortTable(\"").append(consultor).append("\", 2)'>Duração Total<span class='sort-indicator'></span></th>")
                        .append("<th onclick='sortTable(\"").append(consultor).append("\", 3)'>Duração por Tipo<span class='sort-indicator'></span></th>")
                        .append("</tr>")
                        .append("</thead>")
                        .append("<tbody>");

                // Ordenar horas numericamente
                Map<String, Map<String, Object>> sortedHourlyReport = new TreeMap<>((h1, h2) -> {
                    String[] h1Parts = h1.split(":");
                    String[] h2Parts = h2.split(":");
                    int h1Total = Integer.parseInt(h1Parts[0]) * 60 + (h1Parts.length > 1 ? Integer.parseInt(h1Parts[1]) : 0);
                    int h2Total = Integer.parseInt(h2Parts[0]) * 60 + (h2Parts.length > 1 ? Integer.parseInt(h2Parts[1]) : 0);
                    return Integer.compare(h1Total, h2Total);
                });
                sortedHourlyReport.putAll(hourlyReport); 

                sortedHourlyReport.forEach((hora, metrics) -> {
                    int totalPauses = (int) metrics.getOrDefault("totalPauses", 0);
                    int totalDuration = (int) metrics.getOrDefault("totalDuration", 0);

                    html.append("<tr>")
                            .append("<td data-sort='").append(hora).append("'>").append(hora).append("</td>")
                            .append("<td data-sort='").append(totalPauses).append("'>").append(totalPauses).append("</td>")
                            .append("<td data-sort='").append(totalDuration).append("'>")
                            .append(TimeUtils.formatDuration(totalDuration)).append("</td>")
                            .append("<td>");

                    Object durationByType = metrics.get("durationByType");
                    if (durationByType instanceof Map) {
                        ((Map<?, ?>) durationByType).forEach((type, duration) -> {
                            int seconds = (duration instanceof Integer) ? (int) duration : 0;
                            html.append("<div class='duration-type'>")
                                    .append(type).append(": ").append(TimeUtils.formatDuration(seconds))
                                    .append("</div>");
                        });
                    }
                    html.append("</td></tr>");
                });
                html.append("</tbody></table></div>");
            });
        }

        appendCommonScripts(html);
        html.append("</body></html>");

        return ResponseEntity.ok(html.toString());
    }

    // Métodos auxiliares para evitar duplicação de código
    private void appendHtmlHeader(StringBuilder html, String title) {
        html.append("<!DOCTYPE html>")
                .append("<html lang='pt'>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<title>").append(title).append("</title>")
                .append("<style>")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
                .append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }")
                .append("th { background-color: #4CAF50; color: white; cursor: pointer; position: relative; }")
                .append("th:hover { background-color: #45a049; }")
                .append("tr:nth-child(even) { background-color: #f2f2f2; }")
                .append(".sort-indicator { position: absolute; right: 8px; }")
                .append(".asc::after { content: '↑'; color: #fff; }")
                .append(".desc::after { content: '↓'; color: #fff; }")
                .append(".duration-type { margin: 2px 0; padding: 4px; background-color: #f8f8f8; }")
                .append(".search-container { margin: 20px 0; }")
                .append("input[type='text'] { width: 100%; padding: 12px 20px; margin: 8px 0; ")
                .append("box-sizing: border-box; border: 2px solid #ccc; border-radius: 4px; ")
                .append("font-size: 16px; }")
                .append(".consultor-table { margin-bottom: 40px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<h1>").append(title).append("</h1>");
    }

    private void appendCommonScripts(StringBuilder html) {
        html.append("<script>")
                .append("function filterConsultores(query) {")
                .append("  const consultores = document.querySelectorAll('.consultor-table');")
                .append("  consultores.forEach(consultor => {")
                .append("    const consultorName = consultor.getAttribute('data-consultor').toUpperCase();")
                .append("    if (consultorName.includes(query.toUpperCase())) {")
                .append("      consultor.style.display = '';")
                .append("    } else {")
                .append("      consultor.style.display = 'none';")
                .append("    }")
                .append("  });")
                .append("}")
                .append("function filterTable(input, consultor) {")
                .append("  const table = document.getElementById('table-' + consultor);")
                .append("  const rows = table.tBodies[0].rows;")
                .append("  const filter = input.value.toUpperCase();")
                .append("  for (let i = 0; i < rows.length; i++) {")
                .append("    const cells = rows[i].cells;")
                .append("    let show = false;")
                .append("    for (let j = 0; j < cells.length; j++) {")
                .append("      if (cells[j].textContent.toUpperCase().includes(filter)) {")
                .append("        show = true;")
                .append("        break;")
                .append("      }")
                .append("    }")
                .append("    rows[i].style.display = show ? '' : 'none';")
                .append("  }")
                .append("}")
                .append("function sortTable(consultor, columnIndex) {")
                .append("  const table = document.getElementById('table-' + consultor);")
                .append("  const tbody = table.tBodies[0];")
                .append("  const rows = Array.from(tbody.rows);")
                .append("  const isAsc = table.tHead.rows[0].cells[columnIndex].classList.contains('asc');")
                .append("  rows.sort((a, b) => {")
                .append("    const aVal = a.cells[columnIndex].getAttribute('data-sort') || a.cells[columnIndex].textContent;")
                .append("    const bVal = b.cells[columnIndex].getAttribute('data-sort') || b.cells[columnIndex].textContent;")
                .append("    if (!isNaN(aVal) && !isNaN(bVal)) {")
                .append("      return isAsc ? bVal - aVal : aVal - bVal;")
                .append("    } else {")
                .append("      return isAsc ? bVal.localeCompare(aVal) : aVal.localeCompare(bVal);")
                .append("    }")
                .append("  });")
                .append("  tbody.innerHTML = '';")
                .append("  rows.forEach(row => tbody.appendChild(row));")
                .append("  table.tHead.rows[0].cells.forEach((th, idx) => {")
                .append("    th.classList.remove('asc', 'desc');")
                .append("    if (idx === columnIndex) th.classList.add(isAsc ? 'desc' : 'asc');")
                .append("  });")
                .append("}")
                .append("</script>");
    }
}