package com.tripfriend.domain.qna.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.qna.dto.AnswerDto;
import com.tripfriend.domain.qna.entity.Answer;
import com.tripfriend.domain.qna.entity.Question;
import com.tripfriend.domain.qna.repository.AnswerRepository;
import com.tripfriend.domain.qna.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AnswerServiceTest {

    private Member member;
    private Question question;
    private Answer answer;


    @InjectMocks
    private AnswerService answerService;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        member = Member.builder()
                .id(1L)
                .username("tester")
                .build();

        question = Question.builder()
                .id(1L)
                .title("Test Question")
                .content("Question content")
                .member(member)
                .build();

        answer = Answer.builder()
                .id(1L)
                .content("This is an answer")
                .question(question)
                .member(member)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

    }
    @Test
    @DisplayName("답변 삭제 - 작성자 본인")
    void deleteAnswerByOwner() {
        // given
        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));

        // when
        answerService.deleteAnswer(1L, member);

        // then
        verify(answerRepository).delete(answer);
    }

    @Test
    @DisplayName("답변 삭제 실패 - 작성자 아님")
    void deleteAnswerByOtherUser_shouldThrow() {
        // given
        Member other = Member.builder().id(2L).build();
        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));

        // when & then
        assertThatThrownBy(() -> answerService.deleteAnswer(1L, other))
                .isInstanceOf(SecurityException.class)
                .hasMessage("본인의 답변만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("답변 목록 조회")
    void getAnswersByQuestionId() {

        // when
        List<AnswerDto> result = answerService.getAnswersByQuestionId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("This is an answer");
    }
    @Test
    @DisplayName("관리자 답변 삭제")
    void deleteAnswerByAdmin() {
        // given
        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));

        // when
        answerService.deleteAnswerByAdmin(1L);

        // then
        verify(answerRepository).delete(answer);
    }

}
