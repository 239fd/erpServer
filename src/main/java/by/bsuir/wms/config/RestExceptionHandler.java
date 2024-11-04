package by.wms.server.config;

import by.wms.server.DTO.ErrorDTO;
import by.wms.server.Exceptions.AppException;
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
