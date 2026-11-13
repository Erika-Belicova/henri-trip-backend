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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for managing guide invitations.
 */
@Service
public class GuideInvitationService {

    private final GuideInvitationRepository guideInvitationRepository;
    private final GuideRepository guideRepository;
    private final UserRepository userRepository;

    public GuideInvitationService(
            GuideInvitationRepository guideInvitationRepository,
            GuideRepository guideRepository, UserRepository userRepository
    ) {
        this.guideInvitationRepository = guideInvitationRepository;
        this.guideRepository = guideRepository;
        this.userRepository = userRepository;
    }

    // invites a user to a guide
    @Transactional
    public GuideInvitationResponseDTO inviteUser(Long guideId, Long userId, Long invitedById) {
        Guide guide = findGuideById(guideId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User invitedBy = userRepository.findById(invitedById)
                .orElseThrow(() -> new UserNotFoundException("Inviting user not found"));

        if (guideInvitationRepository
                .existsByIdGuideIdAndIdUserId(guideId, userId)) {
            throw new ResourceConflictException("User is already invited to this guide");
        }

        GuideInvitation invitation = new GuideInvitation();
        invitation.setId(new GuideInvitationId(guideId, userId));
        invitation.setGuide(guide);
        invitation.setUser(user);
        invitation.setInvitedBy(invitedBy);

        return toResponseDTO(guideInvitationRepository.save(invitation));
    }

    // retrieves all invitations belonging to a guide
    @Transactional(readOnly = true)
    public List<GuideInvitationResponseDTO> getGuideInvitations(Long guideId) {
        findGuideById(guideId);

        return guideInvitationRepository.findByGuideId(guideId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // removes a user's invitation to a guide
    @Transactional
    public void removeInvitation(Long guideId, Long userId) {
        findGuideById(guideId);

        guideInvitationRepository.deleteById(new GuideInvitationId(guideId, userId));
    }

    // retrieves a guide or throws an exception if it does not exist
    private Guide findGuideById(Long guideId) {
        return guideRepository.findById(guideId)
                .orElseThrow(() -> new GuideNotFoundException("Guide not found"));
    }

    // converts an invitation entity to its response DTO
    private GuideInvitationResponseDTO toResponseDTO(GuideInvitation invitation) {
        GuideInvitationResponseDTO responseDTO = new GuideInvitationResponseDTO();

        responseDTO.setGuideId(invitation.getGuide().getId());
        responseDTO.setUserId(invitation.getUser().getId());
        responseDTO.setUserEmail(invitation.getUser().getEmail());

        if (invitation.getInvitedBy() != null) {
            responseDTO.setInvitedById(invitation.getInvitedBy().getId());
        }
        responseDTO.setCreatedAt(invitation.getCreatedAt());

        return responseDTO;
    }

}
