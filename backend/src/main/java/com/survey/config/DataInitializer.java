package com.survey.config;

import com.survey.entity.Question;
import com.survey.entity.Survey;
import com.survey.repository.SurveyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * 개발용 샘플 설문 1개 생성
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final SurveyRepository surveyRepository;

    @Value("${app.seed-demo:false}")
    private boolean seedDemo;

    public DataInitializer(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @Override
    public void run(String... args) {
        // 관리자가 등록한 데이터만 사용하도록 기본은 비활성화합니다.
        if (!seedDemo) return;
        if (surveyRepository.count() > 0) return;

        Survey survey = new Survey();
        survey.setTitle("서비스 만족도 설문");
        survey.setDescription("아임웹 연동 테스트용 설문입니다.");
        survey.setActive(true);

        Question q1 = new Question();
        q1.setSurvey(survey);
        q1.setText("전반적인 만족도는 어떠신가요?");
        q1.setType("SINGLE_CHOICE");
        q1.setOptions(List.of("매우 만족", "만족", "보통", "불만족"));
        q1.setRequired(true);
        q1.setSortOrder(0);

        Question q2 = new Question();
        q2.setSurvey(survey);
        q2.setText("개선이 필요한 부분을 자유롭게 적어주세요.");
        q2.setType("LONG_TEXT");
        q2.setRequired(false);
        q2.setSortOrder(1);

        survey.setQuestions(List.of(q1, q2));
        surveyRepository.save(survey);
    }
}
