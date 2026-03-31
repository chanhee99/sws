package com.survey.repository;

import com.survey.entity.SurveyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    List<SurveyResponse> findBySurveyIdOrderBySubmittedAtDesc(Long surveyId);

    Page<SurveyResponse> findAllByOrderBySubmittedAtDesc(Pageable pageable);

    Page<SurveyResponse> findBySurveyIdOrderBySubmittedAtDesc(Long surveyId, Pageable pageable);

    @Query("SELECT r FROM SurveyResponse r LEFT JOIN FETCH r.survey LEFT JOIN FETCH r.answers a LEFT JOIN FETCH a.question WHERE r.id = :id")
    Optional<SurveyResponse> findByIdWithAnswersAndQuestions(@Param("id") Long id);

    @Query("SELECT r FROM SurveyResponse r LEFT JOIN FETCH r.answers a LEFT JOIN FETCH a.question WHERE r.survey.id = :surveyId ORDER BY r.submittedAt DESC")
    List<SurveyResponse> findBySurveyIdWithAnswersAndQuestions(@Param("surveyId") Long surveyId);
}
