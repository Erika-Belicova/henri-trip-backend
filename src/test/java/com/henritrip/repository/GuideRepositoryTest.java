package com.henritrip.repository;

import com.henritrip.TestcontainersConfiguration;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideInvitation;
import com.henritrip.model.entity.GuideInvitationId;
import com.henritrip.model.entity.User;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class GuideRepositoryTest {

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuideInvitationRepository guideInvitationRepository;

    @Test
    void findDistinctByInvitationsUserIdShouldReturnOnlyInvitedGuides() {
        // arrange
        User user = createUser("user@example.com");
        User anotherUser = createUser("another@example.com");

        Guide guide = createGuide("Paris Travel Guide");
        Guide anotherGuide = createGuide("London Travel Guide");

        inviteUserToGuide(user, guide);
        inviteUserToGuide(anotherUser, anotherGuide);

        // act
        Page<@NonNull Guide> result = guideRepository.findDistinctByInvitationsUserId(
                user.getId(),
                PageRequest.of(0, 10)
        );

        // assert
        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Guide::getTitle)
                .containsExactly("Paris Travel Guide");
    }

    @Test
    void findDistinctByInvitationsUserIdShouldReturnPaginatedResults() {
        // arrange
        User user = createUser("user@example.com");

        Guide firstGuide = createGuide("Guide One");
        Guide secondGuide = createGuide("Guide Two");
        Guide thirdGuide = createGuide("Guide Three");

        inviteUserToGuide(user, firstGuide);
        inviteUserToGuide(user, secondGuide);
        inviteUserToGuide(user, thirdGuide);

        // act
        Page<@NonNull Guide> firstPage = guideRepository.findDistinctByInvitationsUserId(
                user.getId(),
                PageRequest.of(0, 2, Sort.by("id").ascending())
        );

        Page<@NonNull Guide> secondPage = guideRepository.findDistinctByInvitationsUserId(
                user.getId(),
                PageRequest.of(1, 2, Sort.by("id").ascending())
        );

        // assert
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.hasNext()).isTrue();

        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    void existsByIdAndInvitationsUserIdShouldReturnCorrectResult() {
        // arrange
        User user = createUser("user@example.com");
        User anotherUser = createUser("another@example.com");

        Guide guide = createGuide("Paris Travel Guide");

        inviteUserToGuide(user, guide);

        // act & assert
        assertThat(guideRepository.existsByIdAndInvitationsUserId(
                guide.getId(), user.getId()
        )).isTrue();

        assertThat(guideRepository.existsByIdAndInvitationsUserId(
                guide.getId(), anotherUser.getId()
        )).isFalse();

        assertThat(guideRepository.existsByIdAndInvitationsUserId(
                999L, user.getId()
        )).isFalse();
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("test-password-hash");

        return userRepository.save(user);
    }

    private Guide createGuide(String title) {
        Guide guide = new Guide();
        guide.setTitle(title);
        guide.setDescription("Test guide description");

        return guideRepository.save(guide);
    }

    private void inviteUserToGuide(User user, Guide guide) {
        GuideInvitation invitation = new GuideInvitation();
        invitation.setId(new GuideInvitationId(guide.getId(), user.getId()));
        invitation.setGuide(guide);
        invitation.setUser(user);

        guideInvitationRepository.save(invitation);
    }
}
