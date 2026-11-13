package com.henritrip.service;

import com.henritrip.dto.guideinvitation.GuideInvitationResponseDTO;
import com.henritrip.exception.GuideNotFoundException;
import com.henritrip.exception.ResourceConflictException;
import com.henritrip.exception.UserNotFoundException;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideInvitation;
import com.henritrip.model.entity.GuideInvitationId;
import com.henritrip.model.entity.User;
import com.henritrip.repository.GuideInvitationRepository;
import com.henritrip.repository.GuideRepository;
import com.henritrip.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuideInvitationServiceTest {

    @Mock
    private GuideInvitationRepository guideInvitationRepository;

    @Mock
    private GuideRepository guideRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GuideInvitationService guideInvitationService;

    private Guide guide;
    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        guide = new Guide();
        guide.setId(1L);
        guide.setTitle("Paris Travel Guide");

        user = new User();
        user.setId(2L);
        user.setEmail("user@example.com");

        admin = new User();
        admin.setId(3L);
        admin.setEmail("admin@example.com");
    }

    @Test
    void inviteUserShouldCreateInvitationSuccessfully() {
        // arrange
        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(admin));

        when(guideInvitationRepository
                .existsByIdGuideIdAndIdUserId(1L, 2L))
                .thenReturn(false);

        when(guideInvitationRepository.save(any(GuideInvitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // act
        GuideInvitationResponseDTO result =
                guideInvitationService.inviteUser(1L, 2L, 3L);

        // assert
        assertThat(result.getGuideId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getUserEmail()).isEqualTo("user@example.com");
        assertThat(result.getInvitedById()).isEqualTo(3L);

        verify(guideInvitationRepository).save(any(GuideInvitation.class));
    }

    @Test
    void inviteUserShouldThrowConflictWhenUserIsAlreadyInvited() {
        // arrange
        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(admin));

        when(guideInvitationRepository
                .existsByIdGuideIdAndIdUserId(1L, 2L))
                .thenReturn(true);

        // act & assert
        assertThatThrownBy(() ->
                guideInvitationService.inviteUser(1L, 2L, 3L)
        )
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("User is already invited to this guide");

        verify(guideInvitationRepository, never())
                .save(any(GuideInvitation.class));
    }

    @Test
    void inviteUserShouldThrowGuideNotFoundWhenGuideDoesNotExist() {
        // arrange
        when(guideRepository.findById(1L))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() ->
                guideInvitationService.inviteUser(1L, 2L, 3L)
        )
                .isInstanceOf(GuideNotFoundException.class);

        verifyNoInteractions(userRepository);
        verify(guideInvitationRepository, never())
                .save(any(GuideInvitation.class));
    }

    @Test
    void inviteUserShouldThrowUserNotFoundWhenUserDoesNotExist() {
        // arrange
        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() ->
                guideInvitationService.inviteUser(1L, 2L, 3L)
        )
                .isInstanceOf(UserNotFoundException.class);

        verify(guideInvitationRepository, never())
                .save(any(GuideInvitation.class));
    }

    @Test
    void inviteUserShouldThrowUserNotFoundWhenInvitingAdminDoesNotExist() {
        // arrange
        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(3L))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() ->
                guideInvitationService.inviteUser(1L, 2L, 3L)
        )
                .isInstanceOf(UserNotFoundException.class);

        verify(guideInvitationRepository, never())
                .save(any(GuideInvitation.class));
    }

    @Test
    void getGuideInvitationsShouldReturnAllInvitations() {
        // arrange
        GuideInvitation invitation = new GuideInvitation();
        invitation.setId(new GuideInvitationId(1L, 2L));
        invitation.setGuide(guide);
        invitation.setUser(user);
        invitation.setInvitedBy(admin);

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(guideInvitationRepository.findByGuideId(1L))
                .thenReturn(List.of(invitation));

        // act
        List<GuideInvitationResponseDTO> result =
                guideInvitationService.getGuideInvitations(1L);

        // assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGuideId()).isEqualTo(1L);
        assertThat(result.get(0).getUserId()).isEqualTo(2L);
        assertThat(result.get(0).getUserEmail())
                .isEqualTo("user@example.com");
        assertThat(result.get(0).getInvitedById()).isEqualTo(3L);

        verify(guideInvitationRepository).findByGuideId(1L);
    }

    @Test
    void getGuideInvitationsShouldThrowWhenGuideDoesNotExist() {
        // arrange
        when(guideRepository.findById(1L))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() ->
                guideInvitationService.getGuideInvitations(1L)
        )
                .isInstanceOf(GuideNotFoundException.class);

        verify(guideInvitationRepository, never()).findByGuideId(any());
    }

    @Test
    void removeInvitationShouldDeleteInvitation() {
        // arrange
        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        // act
        guideInvitationService.removeInvitation(1L, 2L);

        // assert
        verify(guideInvitationRepository)
                .deleteById(new GuideInvitationId(1L, 2L));
    }

    @Test
    void removeInvitationShouldThrowWhenGuideDoesNotExist() {
        // arrange
        when(guideRepository.findById(1L))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() ->
                guideInvitationService.removeInvitation(1L, 2L)
        )
                .isInstanceOf(GuideNotFoundException.class);

        verify(guideInvitationRepository, never())
                .deleteById(any(GuideInvitationId.class));
    }

}
