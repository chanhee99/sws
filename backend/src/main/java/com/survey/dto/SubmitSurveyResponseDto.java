package com.survey.dto;

public class SubmitSurveyResponseDto {

    private boolean success;
    private Long responseId;
    private String message;
    private ResultDto result;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public Long getResponseId() { return responseId; }
    public void setResponseId(Long responseId) { this.responseId = responseId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public ResultDto getResult() { return result; }
    public void setResult(ResultDto result) { this.result = result; }
}

