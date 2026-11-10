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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for creating, retrieving, updating and deleting guides.
 */
@Service
public class GuideService {

    private final GuideRepository guideRepository;
    private final UserRepository userRepository;
    private final GuideMapper guideMapper;
    private final GuideDayService guideDayService;

    public GuideService(
            GuideRepository guideRepository, UserRepository userRepository,
            GuideMapper guideMapper, GuideDayService guideDayService
    ) {
        this.guideRepository = guideRepository;
        this.userRepository = userRepository;
        this.guideMapper = guideMapper;
        this.guideDayService = guideDayService;
    }

    // creates a guide and automatically generates its days
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public GuideResponseDTO createGuide(GuideRequestDTO requestDTO, Long userId) {
        User creator = findUserById(userId);
        Guide guide = guideMapper.toGuideEntity(requestDTO);
        guide.setCreatedBy(creator);

        guideDayService.synchronizeDays(guide, requestDTO.getNumberOfDays());
        Guide savedGuide = guideRepository.save(guide);

        return guideMapper.toGuideResponseDTO(savedGuide);
    }

    // retrieves all guides accessible to the authenticated user
    @Transactional(readOnly = true)
    public Page<@NonNull GuideResponseDTO> getAllGuides(Long userId, Pageable pageable) {
        User user = findUserById(userId);

        // admins can access all guides, while regular users only see invited guides
        Page<@NonNull Guide> guides = user.getRole() == UserRole.ADMIN
                ? guideRepository.findAll(pageable)
                : guideRepository.findDistinctByInvitationsUserId(userId, pageable);

        return guides.map(guideMapper::toGuideResponseDTO);
    }

    // retrieves a guide after checking the authenticated user's access
    @Transactional(readOnly = true)
    public GuideResponseDTO getGuideById(Long guideId, Long userId) {
        User user = findUserById(userId);
        Guide guide = findGuideById(guideId);

        // regular users can only access guides they have been invited to
        if (user.getRole() != UserRole.ADMIN
                && !guideRepository.existsByIdAndInvitationsUserId(guideId, userId)) {
            throw new GuideNotFoundException("Guide not found");
        }

        return guideMapper.toGuideResponseDTO(guide);
    }

    // updates guide information and synchronises its itinerary
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public GuideResponseDTO updateGuide(Long guideId, GuideUpdateRequestDTO requestDTO) {
        Guide guide = findGuideById(guideId);

        guideMapper.updateGuideEntityFromDTO(requestDTO, guide);
        guideDayService.synchronizeDays(guide, requestDTO.getNumberOfDays());

        return guideMapper.toGuideResponseDTO(guide);
    }

    // deletes a guide and its associated entities
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteGuide(Long guideId) {
        Guide guide = findGuideById(guideId);

        guideRepository.delete(guide);
    }

    // retrieves a guide or throws an exception if it does not exist
    private Guide findGuideById(Long guideId) {
        return guideRepository.findById(guideId)
                .orElseThrow(() -> new GuideNotFoundException("Guide not found")
        );
    }

    // retrieves a user or throws an exception if it does not exist
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found")
        );
    }

}
