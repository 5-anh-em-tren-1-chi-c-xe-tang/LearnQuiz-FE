package com.example.learnquiz_fe.data.model.quiz;

import com.google.gson.annotations.SerializedName;

/**
 * Generic API response wrapper
 * Used by backend to standardize all API responses
 */
public class ApiResponse<T> {
    
    /**
     * Whether the request was successful
     */
    @SerializedName("success")
    private boolean success;
    
    /**
     * Message from the server (error or success message)
     */
    @SerializedName("message")
    private String message;
    
    /**
     * The actual response data (generic type)
     */
    @SerializedName("data")
    private T data;
    
    /**
     * Error code if applicable
     */
    @SerializedName("errorCode")
    private String errorCode;

    // The attrs below match exact as the response object from backend
    private Object errors;
    private int statusCode;
    
    // Constructor
    public ApiResponse() {
    }
    
    public ApiResponse(boolean success, String message, T data) {
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
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
