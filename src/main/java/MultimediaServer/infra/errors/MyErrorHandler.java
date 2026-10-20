package MultimediaServer.infra.errors;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;

@RestControllerAdvice
public class MyErrorHandler{
    @ExceptionHandler(MyIntegrityValidationException.class)
    public ResponseEntity<?> myIntegrityValidationExceptionHandler(MyIntegrityValidationException exception){
        HttpStatus status = exception.getStatus() != null
                ? exception.getStatus()
                : HttpStatus.BAD_REQUEST;

        ErrorResponse response = new ErrorResponse(
                exception.getMessage(),
                exception.getCode(),
                status.value()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> myMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e){
        // getErrorCode() devuelve "typeMismatch" (no numérico) y getMessage() expone tipos Java,
        // por eso se usa un código fijo y un mensaje propio con el nombre del parámetro.
        ErrorResponse response = new ErrorResponse(
                "El parámetro '" + e.getName() + "' tiene un formato inválido",
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> myMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<errors> errorsList = e.getFieldErrors().stream().map(errors::new).toList();

        return ResponseEntity.badRequest().body(errorsList);
    }

    // Archivo mayor que spring.servlet.multipart.max-file-size.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> myMaxUploadSizeExceededException(MaxUploadSizeExceededException e){
        ErrorResponse response = new ErrorResponse(
                "El archivo supera el tamaño máximo permitido",
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // Falta un archivo obligatorio del multipart (p. ej. "video" al crear un Multimedia).
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> myMissingServletRequestPartException(MissingServletRequestPartException e){
        ErrorResponse response = new ErrorResponse(
                "Falta el archivo '" + e.getRequestPartName() + "'",
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(response);
    }

    public record errors(String field, String message){
        public errors (FieldError f){
            // Un fallo de conversión (p. ej. texto en un campo numérico de un formulario) trae un mensaje con tipos Java.
            this(f.getField(), f.isBindingFailure()
                    ? "El campo tiene un formato inválido"
                    : f.getDefaultMessage());
        }
    }
}
