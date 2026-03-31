package com.survey.dto;

import java.util.List;

public class ResultConfigDto {

    private List<ResultProfileDto> profiles;
    private List<ResultConditionDto> conditions;

    public List<ResultProfileDto> getProfiles() { return profiles; }
    public void setProfiles(List<ResultProfileDto> profiles) { this.profiles = profiles; }
    public List<ResultConditionDto> getConditions() { return conditions; }
    public void setConditions(List<ResultConditionDto> conditions) { this.conditions = conditions; }
}

