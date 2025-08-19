package com.example.moyeorak.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // USER 관련
    NULL_EMAIL(HttpStatus.BAD_REQUEST, "이메일은 null일 수 없습니다."),
    INVALID_GENDER(HttpStatus.BAD_REQUEST, "성별은 '남' 또는 '여'여야 합니다."),
    INVALID_BIRTH_FORMAT(HttpStatus.BAD_REQUEST, "생년월일 형식이 잘못되었습니다. yyyy-mm-dd"),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다."),
    UNAUTHORIZED_REGION_ACCESS(HttpStatus.FORBIDDEN, "관리자 담당 지역 유저가 아닙니다."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호 확인이 일치하지 않습니다."),
    NULL_GENDER(HttpStatus.BAD_REQUEST, "성별 정보가 null입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    INVALID_ENROLLMENT_STATUS(HttpStatus.BAD_REQUEST, "이미 취소되었거나 수강중 상태가 아닙니다."),

    // REGION
    NOT_FOUND_REGION(HttpStatus.NOT_FOUND, "해당 지역이 존재하지 않습니다."),

    //FACILITY
    FACILITY_REGION_MISMATCH(HttpStatus.BAD_REQUEST, "선택한 시설이 해당 지역에 속하지 않습니다."),
    NOT_FOUND_FACILITY(HttpStatus.NOT_FOUND, "시설 정보가 없습니다."),
    UNAUTHORIZED_FACILITY_ACCESS(HttpStatus.FORBIDDEN, "해당 시설에 접근할 권한이 없습니다."),

    //PROGRAM
    NOT_FOUND_PROGRAM(HttpStatus.NOT_FOUND, "해당 프로그램이 존재하지 않습니다."),

    //NOTICE
    NOT_FOUND_NOTICE(HttpStatus.NOT_FOUND, "공지사항이 존재하지 않습니다."),

    //MAINIMAGE
    NOT_FOUND_MAIN_IMAGE(HttpStatus.NOT_FOUND, "해당 홍보물이 존재하지 않습니다."),
    NOT_FOUND_MAIN_IMAGE_ID(HttpStatus.NOT_FOUND, "해당 ID의 홍보물이 존재하지 않습니다."),

    // ENROLLMENT
    NOT_FOUND_ENROLLMENT(HttpStatus.NOT_FOUND, "해당 수강신청이 존재하지 않습니다."),
    ENROLLMENT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소되었거나 수강중 상태가 아닙니다."),
    PROGRAM_CLOSED(HttpStatus.BAD_REQUEST, "종료된 프로그램은 취소할 수 없습니다."),
    NO_ADMIN_REGION(HttpStatus.INTERNAL_SERVER_ERROR, "관리자에게 지역 정보가 설정되어 있지 않습니다.");




    private final HttpStatus status;
    private final String message;
}