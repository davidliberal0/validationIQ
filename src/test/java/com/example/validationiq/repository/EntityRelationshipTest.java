package com.example.validationiq.repository;

import com.example.validationiq.entity.Comment;
import com.example.validationiq.entity.Failure;
import com.example.validationiq.entity.Project;
import com.example.validationiq.entity.TestRun;
import com.example.validationiq.enums.FailureSeverity;
import com.example.validationiq.enums.FailureStatus;
import com.example.validationiq.enums.TestRunResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// Uses the real Postgres from docker-compose (not an embedded DB - none is
// configured for this project) so this test needs `docker compose up -d postgres`
// running first, same as the app itself.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EntityRelationshipTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestRunRepository testRunRepository;

    @Autowired
    private FailureRepository failureRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void persistsAndReloadsFullProjectToCommentChain() {
        Project project = new Project();
        project.setName("Battery Calibration Validation");
        project.setDescription("Validates battery calibration behavior");
        project = projectRepository.save(project);

        TestRun testRun = new TestRun();
        testRun.setName("Battery Regression Run");
        testRun.setExecutionDate(LocalDate.now());
        testRun.setResult(TestRunResult.FAIL);
        testRun.setProject(project);
        testRun = testRunRepository.save(testRun);

        Failure failure = new Failure();
        failure.setTitle("Voltage threshold exceeded");
        failure.setDescription("Voltage exceeded the configured threshold during the run");
        failure.setTestRun(testRun);
        failure = failureRepository.save(failure);

        Comment comment = new Comment();
        comment.setAuthorName("David");
        comment.setContent("Reproduced locally, investigating calibration table");
        comment.setFailure(failure);
        comment = commentRepository.save(comment);

        Project reloadedProject = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(reloadedProject.getName()).isEqualTo("Battery Calibration Validation");
        assertThat(reloadedProject.getCreatedAt()).isNotNull();
        assertThat(reloadedProject.getUpdatedAt()).isNotNull();

        TestRun reloadedTestRun = testRunRepository.findById(testRun.getId()).orElseThrow();
        assertThat(reloadedTestRun.getProject().getId()).isEqualTo(project.getId());
        assertThat(reloadedTestRun.getResult()).isEqualTo(TestRunResult.FAIL);

        Failure reloadedFailure = failureRepository.findById(failure.getId()).orElseThrow();
        assertThat(reloadedFailure.getTestRun().getId()).isEqualTo(testRun.getId());
        // Defaults from business rules 4 and 5 (spec section 11), never set explicitly above.
        assertThat(reloadedFailure.getStatus()).isEqualTo(FailureStatus.OPEN);
        assertThat(reloadedFailure.getSeverity()).isEqualTo(FailureSeverity.MEDIUM);

        Comment reloadedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(reloadedComment.getFailure().getId()).isEqualTo(failure.getId());
        assertThat(reloadedComment.getCreatedAt()).isNotNull();
    }
}
