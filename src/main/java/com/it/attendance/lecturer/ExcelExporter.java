package com.it.attendance.lecturer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.it.attendance.Adapters.Students.StudentAttendance;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    public static void exportToExcel(Map<String, List<StudentAttendance>> attendanceMap, String filePath, Context context) {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance");

        // Set font size
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 14); // Set font size

        // Create a cell style with font and alignment
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.ALIGN_CENTER);

        CellStyle style1 = workbook.createCellStyle();
        style1.setFont(font);
        style1.setAlignment(CellStyle.ALIGN_RIGHT);
        style1.setVerticalAlignment(CellStyle.ALIGN_CENTER);

        // Create header row
        Row headerRow = sheet.createRow(0);
        createStyledCell(headerRow, 0, "الإسم", style);
        createStyledCell(headerRow, 1, "الرقم الجامعي", style);

        // Create columns for dates
        int colNum = 2;
        for (Map.Entry<String, List<StudentAttendance>> entry : attendanceMap.entrySet()) {
            for (StudentAttendance attendance : entry.getValue()) {
                String date = attendance.getDay() + "/" + attendance.getMonth() + "/" + attendance.getYear();
                createStyledCell(headerRow, colNum++, date, style);
            }
            break; // Only need to add date columns once
        }

        // Sort the rows based on email addresses
        List<String> emails = new ArrayList<>(attendanceMap.keySet());
        Collections.sort(emails);

        // Populate data rows
        int rowNum = 1;
        for (String email : emails) {
            Row row = sheet.createRow(rowNum++);
            List<StudentAttendance> attendanceList = attendanceMap.get(email);

            Log.e("email is", email);
            if (!attendanceList.isEmpty()) {
                createStyledCell(row, 0, attendanceList.get(0).getName(), style1);
                createStyledCell(row, 1, email, style);
            }

            // Write IsPresent values in the corresponding date columns
            for (StudentAttendance attendance : attendanceList) {
                for (int i = 2; i < headerRow.getLastCellNum(); i++) {
                    Cell headerCell = headerRow.getCell(i);
                    if (headerCell.getStringCellValue().equals(attendance.getDay() + "/" + attendance.getMonth() + "/" + attendance.getYear())) {
                        String isPresentText = "";
                        switch (attendance.getIsPresent()) {
                            case "present":
                                isPresentText = "حاضر";
                                break;
                            case "absent":
                                isPresentText = "غائب";
                                break;
                            case "exabsent":
                                isPresentText = "غائب بعذر";
                                break;
                            default:
                                isPresentText = "-";
                                break;
                        }
                        // Apply font style
                        createStyledCell(row, i, isPresentText, style);
                    }
                }
            }
        }

        // Manually adjust column widths based on content length
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            sheet.setColumnWidth(i, calculateColumnWidth(sheet, i));
        }

        // Write the output to a file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the workbook
        try {
            File file = new File(filePath);
            workbook.close();
            shareFile(context, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Auto-size columns
    private static int calculateColumnWidth(Sheet sheet, int columnIndex) {
        int maxColumnWidth = 255; // Maximum column width in characters
        int defaultColumnWidth = 10; // Default column width in characters
        int columnWidth = defaultColumnWidth;

        for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                Cell cell = row.getCell(columnIndex);
                if (cell != null) {
                    int cellValueLength = cell.toString().length();
                    columnWidth = Math.max(columnWidth, cellValueLength);
                }
            }
        }

        // Ensure the calculated column width does not exceed the maximum column width
        columnWidth = Math.min(columnWidth, maxColumnWidth);
        // Convert column width from character width to Excel width units
        return columnWidth * 350;
    }

    private static void createStyledCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void shareFile(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, "com.it.attendance.lecturer.fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // Add FLAG_ACTIVITY_NEW_TASK if the context is not an activity context
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(Intent.createChooser(intent, "Share XLSX file using"));
    }
}
