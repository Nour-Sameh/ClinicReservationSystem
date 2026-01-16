package util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import model.Appointment;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    public static void exportToExcel(List<Appointment> appointments, String fileName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Appointments");

            Row header = sheet.createRow(0);
            String[] headers = {"ID", "Patient", "Date", "Time", "Status", "Type"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            int rowNum = 1;
            for (Appointment a : appointments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(a.getId());
                row.createCell(1).setCellValue(a.getPatient() != null ? a.getPatient().getName() : "—");

                var slot = a.getAppointmentDateTime();
                if (slot != null) {
                    row.createCell(2).setCellValue(slot.getDate().toString());
                    row.createCell(3).setCellValue(
                            slot.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
                    );
                } else {
                    row.createCell(2).setCellValue("—");
                    row.createCell(3).setCellValue("—");
                }

                row.createCell(4).setCellValue(a.getStatus().toString());
                row.createCell(5).setCellValue(a.getAppointmentType().name());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(fileName)) {
                workbook.write(out);
            }
        }
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}