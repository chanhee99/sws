package com.survey.repository;

import com.survey.entity.ResultProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultProfileRepository extends JpaRepository<ResultProfile, Long> {

    List<ResultProfile> findBySurvey_Id(Long surveyId);

    void deleteAllBySurvey_Id(Long surveyId);
}

