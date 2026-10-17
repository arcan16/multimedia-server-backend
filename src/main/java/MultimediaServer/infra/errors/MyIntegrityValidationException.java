package MultimediaServer.infra.errors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MyIntegrityValidationException extends RuntimeException{
    final private Integer code;
    private HttpStatus status;

    public MyIntegrityValidationException(String message, int code) {
        super(message);
        this.code = code;
    }

    public MyIntegrityValidationException(String message, Integer code, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
