package kr.co.aim.common.error;

import java.util.Collections;
import java.util.List;

public class ExcelValidationException extends RuntimeException{


    private final List<String> errorMessages;


    /**
     * 여러 개의 오류 메시지 목록으로 예외를 생성합니다.
     * @param errorMessages 오류 메시지 리스트
     */
    public ExcelValidationException(List<String> errorMessages) {
        // 리스트의 첫 번째 메시지를 대표 메시지로 설정
        super(errorMessages.isEmpty() ? "Excel validation failed" : errorMessages.get(0));
        this.errorMessages = errorMessages;
    }

    /**
     * 단일 오류 메시지로 예외를 생성합니다.
     * @param message 오류 메시지
     */
    public ExcelValidationException(String message) {
        super(message);
        this.errorMessages = Collections.singletonList(message);
    }


    /**
     * 컨트롤러에서 반환할 오류 메시지 목록 전체를 반환합니다.
     * @return 오류 메시지 리스트
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
