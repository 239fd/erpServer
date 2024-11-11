package by.bsuir.wms.config;

import by.bsuir.wms.DTO.ErrorDTO;
import by.bsuir.wms.Exception.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(value = {AppException.class})
    @ResponseBody
    public ResponseEntity<ErrorDTO> handleException(AppException ex){
        return ResponseEntity.status(ex.getCode())
                .body(ErrorDTO.builder().message(ex.getMessage()).build());
    }
}
