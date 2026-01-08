package kr.co.aim.api.service;

import jakarta.servlet.ServletOutputStream;
import kr.co.aim.common.error.ExcelValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ExcelService {
    /**
     * [제네릭 엑셀 생성 메서드]
     * @param response  HttpServletResponse
     * @param dataList  List<?> 타입의 모든 DTO 리스트
     */
    public void writeToExcel(HttpServletResponse response, List<?> dataList) {
        String fileName = "data_List.xlsx";
        try (Workbook workbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("Data_List");

            // --- 1. 스타일 준비 ---
            CellStyle headerStyle = createHeaderStyle(workbook);
            // [중요] 날짜 포맷팅을 위한 별도 스타일
            CellStyle dateCellStyle = createDateCellStyle(workbook);

            // --- 4. 데이터 리스트가 비어있는지 확인 ---
            if (dataList == null || dataList.isEmpty()) {
                // (헤더만 있는 빈 엑셀 파일 응답)
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                workbook.write(outputStream);
                return;
            }

            // --- 5. [리플렉션] DTO의 필드 정보 미리 캐싱 (성능 최적화) ---
            // (부모 클래스의 private 필드까지 모두 가져오는 헬퍼 메서드 사용)
            Class<?> dtoClass = dataList.get(0).getClass();
            Map<String, Field> fieldMap = getAllFields(dtoClass);

            // --- 2. 헤더 정보 추출 ---
            List<String> fieldNames = this.getAllFieldList(dtoClass);

            // --- 3. 헤더 행 생성 ---
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < fieldNames.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fieldNames.get(i));
                cell.setCellStyle(headerStyle);
            }

            // --- 6. 데이터 행 생성 ---
            int rowNum = 1;
            for (Object item : dataList) {
                Row row = sheet.createRow(rowNum++);

                for (int i = 0; i < fieldNames.size(); i++) {
                    String fieldName = fieldNames.get(i);
                    Field field = fieldMap.get(fieldName);

                    if (field == null) {
                        // (안전장치) 맵에 정의한 필드명이 DTO에 없는 경우
                        createCell(row, i, "No Field: " + fieldName, null);
                        continue;
                    }

                    try {
                        field.setAccessible(true); // private 필드 접근 허용
                        Object value = field.get(item); // 리플렉션으로 값 조회

                        // [수정] createCell 헬퍼를 사용하여 타입별로 셀 생성
                        createCell(row, i, value, dateCellStyle);

                    } catch (IllegalAccessException e) {
                        createCell(row, i, "Access Error", null);
                    }
                }
            }

            // --- 7. 컬럼 너비 자동 조정 ---
            for (int i = 0; i < fieldNames.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // --- 8. 응답 헤더 설정 ---
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // --- 9. 엑셀 쓰기 ---
            workbook.write(outputStream);

        } catch (IOException e) {
            throw new RuntimeException("Failed to write Excel file", e);
        }
    }

    // --- 헬퍼 메서드들 ---

    /**
     * 헤더 스타일 생성
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    /**
     * 날짜 포맷용 셀 스타일 생성
     */
    private CellStyle createDateCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        // (날짜/시간 포맷은 필요에 따라 "yyyy-mm-dd" 등으로 수정)
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }

    /**
     * [리플렉션 헬퍼] 부모 클래스를 포함한 모든 필드를 Map으로 반환 (빠른 조회용)
     */
    private Map<String, Field> getAllFields(Class<?> clazz) {
        Map<String, Field> fields = new HashMap<>();
        while (clazz != null && clazz != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                // 자식 클래스의 필드가 부모 필드를 덮어쓰지 않도록 (혹은 그 반대)
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
            clazz = clazz.getSuperclass(); // 부모 클래스로 이동
        }
        return fields;
    }

    /**
     * [리플렉션 헬퍼] 부모 클래스를 포함한 모든 필드를 Map으로 반환 (빠른 조회용)
     */
    private List<String> getAllFieldList(Class<?> clazz) {
        List<String> fieldList = new ArrayList<>();
        Set<String> fieldSet = new HashSet<>();
        while (clazz != null && clazz != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                // 자식 클래스의 필드가 부모 필드를 덮어쓰지 않도록 (혹은 그 반대)
                if (!fieldSet.contains(field.getName())) {
                    fieldSet.add(field.getName());
                    fieldList.add(field.getName());
                }
            }
            clazz = clazz.getSuperclass(); // 부모 클래스로 이동
        }
        return fieldList;
    }

    /**
     * [타입-안전 헬퍼] 값의 타입에 따라 셀을 생성
     */
    private void createCell(Row row, int colIndex, Object value, CellStyle dateStyle) {
        Cell cell = row.createCell(colIndex);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Date) { // java.util.Date, java.sql.Timestamp 모두 처리
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateStyle); // [중요] 날짜 포맷 적용
        } else {
            // 그 외 타입은 toString() 호출
            cell.setCellValue(value.toString());
        }
    }




    /**
     * [제네릭 엑셀 가져오기(Import) 메서드]
     * 엑셀 파일을 파싱하여 DTO의 리스트로 변환합니다.
     * (헤더 순서가 달라도, 불필요한 컬럼이 있어도 DTO 필드명과 일치하는 헤더를 찾아 매핑합니다.)
     *
     * @param file     업로드된 MultipartFile
     * @param dtoClass 변환할 DTO의 Class 객체 (예: UserDto.class)
     * @param <T>      제네릭 DTO 타입
     * @return 파싱 및 매핑이 완료된 DTO 리스트
     * @throws IOException              파일 읽기 실패 시
     * @throws ExcelValidationException 엑셀 유효성 검사 (헤더, 데이터 파싱) 실패 시
     */
    public <T> List<T> importData(MultipartFile file, Class<T> dtoClass) throws IOException, ExcelValidationException {

        List<T> dataList = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        // --- 1. 엑셀 파일 열기 ---
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트 사용
            if (sheet == null) {
                throw new ExcelValidationException("Excel file is empty.");
            }

            // --- 2. [헤더 검증] (유연한 방식) ---
            // (1) 엑셀의 헤더(1행)를 읽어 <헤더명, 컬럼인덱스> 맵을 생성
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ExcelValidationException("Header row not found. The file is empty.");
            }

            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String headerName = cell.getStringCellValue();
                if (headerName != null && !headerName.trim().isEmpty()) {
                    headerMap.put(headerName.trim(), cell.getColumnIndex());
                }
            }

            // (2) DTO의 필드 정보를 리플렉션으로 가져옴 (기존 헬퍼 사용)
            Map<String, Field> fieldMap = getAllFields(dtoClass);

            // (3) (선택적) DTO 필드 중 하나라도 엑셀 헤더에 존재하는지 최소 검증
            boolean atLeastOneMatch = false;
            for (String fieldName : fieldMap.keySet()) {
                if (headerMap.containsKey(fieldName)) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                throw new ExcelValidationException("Invalid Excel template. No matching headers found for DTO fields.");
            }


            // --- 3. [데이터 파싱] (2행부터) ---
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue; // 빈 행은 건너뜀
                }

                T dtoInstance = null;
                try {
                    // (1) DTO 객체 생성
                    dtoInstance = dtoClass.getDeclaredConstructor().newInstance();

                    // (2) DTO의 모든 필드에 대해 반복
                    for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                        String fieldName = entry.getKey();
                        Field field = entry.getValue();

                        // (3) DTO 필드명과 일치하는 헤더가 엑셀에 있는지 확인
                        if (headerMap.containsKey(fieldName)) {
                            int colIndex = headerMap.get(fieldName);
                            Cell cell = row.getCell(colIndex);

                            // (4) [헬퍼 호출] 셀의 값을 읽어 DTO 필드에 주입
                            setFieldValue(dtoInstance, field, cell);
                        }
                        // (일치하는 헤더가 없으면, 해당 DTO 필드는 null로 유지됨)
                    }

                    // 파싱 성공한 DTO를 리스트에 추가
                    dataList.add(dtoInstance);

                } catch (Exception e) {
                    // (오류 수집) 특정 행 파싱 실패 시 (타입 불일치, DTO 생성 실패 등)
                    // (e.getMessage()는 setFieldValue에서 던진 명확한 오류 메시지를 포함)
                    String errorMessage = "Row " + (i + 1) + ": " + e.getMessage();
                    errorMessages.add(errorMessage);
                }
            }

            // --- 4. 최종 오류 확인 ---
            // 파싱 과정에서 수집된 오류가 하나라도 있으면,
            // DB에 저장하지 못하도록 예외를 발생시킴
            if (!errorMessages.isEmpty()) {
                throw new ExcelValidationException(errorMessages);
            }

            return dataList;

        } catch (Exception e) {
            // IO 오류 또는 래핑된 ExcelValidationException 처리
            if (e instanceof ExcelValidationException) {
                throw (ExcelValidationException) e;
            } else if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                // InstantiationException, IllegalAccessException 등
                throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
            }
        }
    }


    /**
     * [리플렉션 헬퍼] 셀의 값을 읽어 DTO 객체의 특정 필드에 주입
     */
    private void setFieldValue(Object dtoInstance, Field field, Cell cell)
            throws IllegalAccessException {

        // 셀이 비어있으면 필드를 null로 설정 (또는 기본값 유지)
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            field.setAccessible(true);
            field.set(dtoInstance, null);
            return;
        }

        field.setAccessible(true);
        Class<?> fieldType = field.getType();

        // DTO의 필드 타입에 따라 셀의 값을 파싱하여 주입
        try {
            if (fieldType == String.class) {
                String value;
                // 숫자 셀도 문자열로 변환 (예: "123"으로 읽기)
                if (cell.getCellType() == CellType.NUMERIC) {
                    // 정수일 경우 ".0" 제거
                    double d = cell.getNumericCellValue();
                    if (d == (long) d) {
                        value = String.valueOf((long) d);
                    } else {
                        value = String.valueOf(d);
                    }
                } else {
                    // 그 외 (String, Boolean 등)은 toString()으로 안전하게
                    value = cell.toString();
                }
                field.set(dtoInstance, value.trim());
            }
            else if (fieldType == Integer.class || fieldType == int.class) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    field.set(dtoInstance, (int) cell.getNumericCellValue());
                } else if (cell.getCellType() == CellType.STRING) {
                    field.set(dtoInstance, Integer.parseInt(cell.getStringCellValue().trim()));
                }
            }
            else if (fieldType == Long.class || fieldType == long.class) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    field.set(dtoInstance, (long) cell.getNumericCellValue());
                } else if (cell.getCellType() == CellType.STRING) {
                    field.set(dtoInstance, Long.parseLong(cell.getStringCellValue().trim()));
                }
            }
            else if (fieldType == Double.class || fieldType == double.class) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    field.set(dtoInstance, cell.getNumericCellValue());
                } else if (cell.getCellType() == CellType.STRING) {
                    field.set(dtoInstance, Double.parseDouble(cell.getStringCellValue().trim()));
                }
            }
            else if (fieldType == Date.class) {
                // 엑셀에서 '날짜' 서식으로 지정된 셀만 허용
                if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                    field.set(dtoInstance, cell.getDateCellValue());
                } else {
                    // (문자열 형식의 날짜 파싱은 SimpleDateFormat이 필요하며 복잡하므로
                    //  엑셀 표준 날짜 서식을 사용하도록 강제하는 것이 좋음)
                    throw new RuntimeException("Field '" + field.getName() + "' requires a Date. Please format the cell as a Date in Excel (not Text).");
                }
            }
            else if (fieldType == Boolean.class || fieldType == boolean.class) {
                if (cell.getCellType() == CellType.BOOLEAN) {
                    field.set(dtoInstance, cell.getBooleanCellValue());
                } else if (cell.getCellType() == CellType.STRING) {
                    field.set(dtoInstance, Boolean.parseBoolean(cell.getStringCellValue().trim()));
                }
            }
            // (필요시 다른 타입: Float, BigDecimal 등 추가)

        } catch (Exception e) {
            // NumberFormatException, IllegalStateException 등
            String cellValue = "N/A";
            try {
                // 오류 발생 시 셀의 값을 확인하기 위해 toString() 시도
                cellValue = cell.toString();
            } catch (Exception ignored) {}

            // 파싱 실패 시, 어느 행/필드에서 문제인지 명확한 예외 메시지를 생성하여 상위로 전달
            throw new RuntimeException("Type mismatch for field '" + field.getName() + "'. Cannot parse cell value '" + cellValue + "' as " + fieldType.getSimpleName() + ".", e);
        }
    }

}
