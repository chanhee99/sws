package com.survey.repository;

import com.survey.entity.ResultCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResultConditionRepository extends JpaRepository<ResultCondition, Long> {

    void deleteAllByProfile_Survey_Id(Long surveyId);

    @Query("SELECT c FROM ResultCondition c JOIN FETCH c.profile p WHERE p.survey.id = :surveyId")
    List<ResultCondition> findAllWithProfileBySurveyId(@Param("surveyId") Long surveyId);
}

