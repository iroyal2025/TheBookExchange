// RestResponse.java
package edu.famu.thebookexchange.model.Rest;

public class RestResponse {
    private boolean success;
    private String message;
    private Object data; // Optional: to include data in the response

    // Constructors
    public RestResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public RestResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}