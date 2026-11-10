package com.henritrip.service;

import com.henritrip.dto.guide.GuideRequestDTO;
import com.henritrip.dto.guide.GuideResponseDTO;
import com.henritrip.dto.guide.GuideUpdateRequestDTO;
import com.henritrip.exception.GuideNotFoundException;
import com.henritrip.exception.UserNotFoundException;
import com.henritrip.mapper.GuideMapper;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.User;
import com.henritrip.model.enums.UserRole;
import com.henritrip.repository.GuideRepository;
import com.henritrip.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GuideServiceTest {

    private GuideService guideService;
    private GuideRepository guideRepository;
    private UserRepository userRepository;
    private GuideMapper guideMapper;
    private GuideDayService guideDayService;

    private User admin;
    private User regularUser;
    private Guide guide;
    private GuideResponseDTO guideResponse;

    @BeforeEach
    void setUp() {

        guideRepository = mock(GuideRepository.class);
        userRepository = mock(UserRepository.class);
        guideMapper = mock(GuideMapper.class);
        guideDayService = mock(GuideDayService.class);

        admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(UserRole.USER);

        guide = new Guide();
        guide.setId(1L);

        guideResponse = new GuideResponseDTO();
        guideResponse.setId(1L);
        guideResponse.setTitle("Weekend in Paris");

        guideService = new GuideService(
                guideRepository,
                userRepository,
                guideMapper,
                guideDayService
        );
    }

    @Test
    void createGuideShouldPersistGuideWithCreatorAndSynchronizeDays() {

        GuideRequestDTO request = new GuideRequestDTO();
        request.setNumberOfDays(2);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        when(guideMapper.toGuideEntity(request))
                .thenReturn(guide);

        when(guideRepository.save(guide))
                .thenReturn(guide);

        when(guideMapper.toGuideResponseDTO(guide))
                .thenReturn(guideResponse);

        GuideResponseDTO result = guideService.createGuide(request, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertSame(admin, guide.getCreatedBy());

        verify(guideDayService).synchronizeDays(guide, 2);
        verify(guideRepository).save(guide);
        verify(guideMapper).toGuideResponseDTO(guide);
    }

    @Test
    void createGuideShouldThrowExceptionWhenCreatorDoesNotExist() {

        GuideRequestDTO request = new GuideRequestDTO();

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> guideService.createGuide(request, 99L)
        );

        verifyNoInteractions(
                guideMapper,
                guideRepository,
                guideDayService
        );
    }

    @Test
    void getAllGuidesShouldReturnAllGuidesForAdmin() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<@NonNull Guide> guides = new PageImpl<>(List.of(guide));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        when(guideRepository.findAll(pageable))
                .thenReturn(guides);

        when(guideMapper.toGuideResponseDTO(guide))
                .thenReturn(guideResponse);

        Page<@NonNull GuideResponseDTO> result =
                guideService.getAllGuides(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().getFirst().getId());

        verify(guideRepository).findAll(pageable);

        verify(guideRepository, never())
                .findDistinctByInvitationsUserId(
                        anyLong(),
                        any(Pageable.class)
                );
    }

    @Test
    void getAllGuidesShouldReturnOnlyInvitedGuidesForRegularUser() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<@NonNull Guide> guides = new PageImpl<>(List.of(guide));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(regularUser));

        when(guideRepository.findDistinctByInvitationsUserId(2L, pageable))
                .thenReturn(guides);

        when(guideMapper.toGuideResponseDTO(guide))
                .thenReturn(guideResponse);

        Page<@NonNull GuideResponseDTO> result =
                guideService.getAllGuides(2L, pageable);

        assertEquals(1, result.getTotalElements());

        verify(guideRepository)
                .findDistinctByInvitationsUserId(2L, pageable);

        verify(guideRepository, never()).findAll(pageable);
    }

    @Test
    void getGuideByIdShouldReturnGuideForAdmin() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toGuideResponseDTO(guide))
                .thenReturn(guideResponse);

        GuideResponseDTO result = guideService.getGuideById(1L, 1L);

        assertEquals(1L, result.getId());

        verify(guideRepository, never())
                .existsByIdAndInvitationsUserId(anyLong(), anyLong());
    }

    @Test
    void getGuideByIdShouldReturnGuideWhenRegularUserIsInvited() {

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(regularUser));

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(guideRepository.existsByIdAndInvitationsUserId(1L, 2L))
                .thenReturn(true);

        when(guideMapper.toGuideResponseDTO(guide))
                .thenReturn(guideResponse);

        GuideResponseDTO result = guideService.getGuideById(1L, 2L);

        assertEquals(1L, result.getId());

        verify(guideRepository)
                .existsByIdAndInvitationsUserId(1L, 2L);
    }

    @Test
    void getGuideByIdShouldThrowExceptionWhenRegularUserIsNotInvited() {

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(regularUser));

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(guideRepository.existsByIdAndInvitationsUserId(1L, 2L))
                .thenReturn(false);

        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.getGuideById(1L, 2L)
        );

        verify(guideMapper, never())
                .toGuideResponseDTO(any(Guide.class));
    }

    @Test
    void getGuideByIdShouldThrowExceptionWhenGuideDoesNotExist() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        when(guideRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.getGuideById(99L, 1L)
        );
    }

    @Test
    void updateGuideShouldSynchronizeIncreasedNumberOfDays() {

        GuideUpdateRequestDTO request = new GuideUpdateRequestDTO();
        request.setNumberOfDays(3);

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toGuideResponseDTO(guide))
                .thenReturn(guideResponse);

        GuideResponseDTO result = guideService.updateGuide(1L, request);

        assertNotNull(result);

        verify(guideMapper)
                .updateGuideEntityFromDTO(request, guide);

        verify(guideDayService)
                .synchronizeDays(guide, 3);
    }

    @Test
    void updateGuideShouldSynchronizeDecreasedNumberOfDays() {

        GuideUpdateRequestDTO request = new GuideUpdateRequestDTO();
        request.setNumberOfDays(2);

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toGuideResponseDTO(guide))
                .thenReturn(guideResponse);

        guideService.updateGuide(1L, request);

        verify(guideDayService)
                .synchronizeDays(guide, 2);
    }

    @Test
    void updateGuideShouldSynchronizeUnchangedNumberOfDays() {

        GuideUpdateRequestDTO request = new GuideUpdateRequestDTO();
        request.setNumberOfDays(2);

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        when(guideMapper.toGuideResponseDTO(guide))
                .thenReturn(guideResponse);

        guideService.updateGuide(1L, request);

        verify(guideDayService)
                .synchronizeDays(guide, 2);
    }

    @Test
    void updateGuideShouldThrowExceptionWhenGuideDoesNotExist() {

        GuideUpdateRequestDTO request = new GuideUpdateRequestDTO();
        request.setNumberOfDays(2);

        when(guideRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.updateGuide(99L, request)
        );

        verifyNoInteractions(guideMapper, guideDayService);
    }

    @Test
    void deleteGuideShouldDeleteExistingGuide() {

        when(guideRepository.findById(1L))
                .thenReturn(Optional.of(guide));

        guideService.deleteGuide(1L);

        verify(guideRepository).delete(guide);
    }

    @Test
    void deleteGuideShouldThrowExceptionWhenGuideDoesNotExist() {

        when(guideRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                GuideNotFoundException.class,
                () -> guideService.deleteGuide(99L)
        );

        verify(guideRepository, never()).delete(any(Guide.class));
    }

}
