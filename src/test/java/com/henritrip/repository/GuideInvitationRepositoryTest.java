package com.henritrip.repository;

import com.henritrip.TestcontainersConfiguration;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideInvitation;
import com.henritrip.model.entity.GuideInvitationId;
import com.henritrip.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class GuideInvitationRepositoryTest {

    @Autowired
    private GuideInvitationRepository guideInvitationRepository;

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByIdGuideIdAndIdUserIdShouldReturnCorrectResult() {
        // arrange
        User user = createUser("user@example.com");
        User anotherUser = createUser("another@example.com");

        Guide guide = createGuide("Paris Travel Guide");

        createInvitation(user, guide);

        // act & assert
        assertThat(guideInvitationRepository.existsByIdGuideIdAndIdUserId(
                guide.getId(), user.getId()
        )).isTrue();

        assertThat(guideInvitationRepository.existsByIdGuideIdAndIdUserId(
                guide.getId(), anotherUser.getId()
        )).isFalse();
    }

    @Test
    void findByGuideIdShouldReturnAllInvitationsForGuide() {
        // arrange
        User firstUser = createUser("first@example.com");
        User secondUser = createUser("second@example.com");
        User thirdUser = createUser("third@example.com");

        Guide guide = createGuide("Paris Travel Guide");
        Guide anotherGuide = createGuide("London Travel Guide");

        createInvitation(firstUser, guide);
        createInvitation(secondUser, guide);
        createInvitation(thirdUser, anotherGuide);

        // act
        var result = guideInvitationRepository.findByGuideId(guide.getId());

        // assert
        assertThat(result)
                .hasSize(2)
                .extracting(invitation -> invitation.getUser().getId())
                .containsExactlyInAnyOrder(firstUser.getId(), secondUser.getId());
    }

    @Test
    void findByUserIdShouldReturnAllInvitationsForUser() {
        // arrange
        User user = createUser("user@example.com");
        User anotherUser = createUser("another@example.com");

        Guide firstGuide = createGuide("Paris Travel Guide");
        Guide secondGuide = createGuide("London Travel Guide");
        Guide thirdGuide = createGuide("Rome Travel Guide");

        createInvitation(user, firstGuide);
        createInvitation(user, secondGuide);
        createInvitation(anotherUser, thirdGuide);

        // act
        var result = guideInvitationRepository.findByUserId(user.getId());

        // assert
        assertThat(result)
                .hasSize(2)
                .extracting(invitation -> invitation.getGuide().getId())
                .containsExactlyInAnyOrder(firstGuide.getId(), secondGuide.getId());
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

    private void createInvitation(User user, Guide guide) {
        GuideInvitation invitation = new GuideInvitation();
        invitation.setId(new GuideInvitationId(guide.getId(), user.getId()));
        invitation.setGuide(guide);
        invitation.setUser(user);

        guideInvitationRepository.save(invitation);
    }
}
