package com.survey.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ResultConfigUpsertRequest {

    @NotNull
    @Valid
    private List<ResultProfileDto> profiles;

    @NotNull
    @Valid
    private List<ResultConditionDto> conditions;

    public List<ResultProfileDto> getProfiles() { return profiles; }
    public void setProfiles(List<ResultProfileDto> profiles) { this.profiles = profiles; }
    public List<ResultConditionDto> getConditions() { return conditions; }
}

