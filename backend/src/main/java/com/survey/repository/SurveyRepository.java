package com.survey.repository;

import com.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("SELECT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.active = true ORDER BY s.updatedAt DESC")
    List<Survey> findAllActiveWithQuestions();

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions ORDER BY s.updatedAt DESC")
    List<Survey> findAllWithQuestions();
}
