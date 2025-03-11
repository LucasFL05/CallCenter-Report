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
                .append("<title>Relatório de Pausas</title>")
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
                .append("<input type='text' id='searchPause' placeholder='Pesquisar...' onkeyup='searchTable(\"reportTable\", this.value)'>")
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
                .append("function searchTable(tableId, query) {")
                .append("  const table = document.getElementById(tableId);")
                .append("  const tbody = table.tBodies[0];")
                .append("  const rows = Array.from(tbody.rows);")
                .append("  query = query.toLowerCase();")
                .append("  rows.forEach(row => {")
                .append("    const cells = Array.from(row.cells);")
                .append("    const match = cells.some(cell => ")
                .append("      cell.textContent.toLowerCase().includes(query)")
                .append("    );")
                .append("    row.style.display = match ? '' : 'none';")
                .append("  });")
                .append("}")
                .append("</script>")
                .append("</body></html>");

        return ResponseEntity.ok(html.toString());
    }
}