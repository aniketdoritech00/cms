package in.doritech.cms.util;

public class ApiResponse {

    private String message;
    private Object payload;
    private int status;

    public ApiResponse() {
    }

    public ApiResponse(String message, Object payload, int status) {
        this.message = message;
        this.payload = payload;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}