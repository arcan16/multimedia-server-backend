package MultimediaServer.infra.errors;

public class ErrorResponse {
    private String message;
    private Integer code;
    private Integer status;

    public ErrorResponse(String message, Integer code, Integer status) {
        this.message = message;
        this.code = code;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public Integer getStatus() {
        return status;
    }
}
