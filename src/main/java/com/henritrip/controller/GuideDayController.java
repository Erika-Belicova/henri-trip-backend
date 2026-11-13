package com.henritrip.controller;

import com.henritrip.dto.guideday.GuideDayResponseDTO;
import com.henritrip.service.GuideDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for retrieving guide days.
 */
@Tag(name = "Guide Days", description = "Guide day retrieval endpoints")
@RestController
@RequestMapping("/api/guides/{guideId}/days")
public class GuideDayController {

    private final GuideDayService guideDayService;

    public GuideDayController(GuideDayService guideDayService) {
        this.guideDayService = guideDayService;
    }

    @Operation(summary = "Retrieve all days of a guide")
    @GetMapping
    public ResponseEntity<@NonNull List<GuideDayResponseDTO>> getDays(
            @PathVariable Long guideId
    ) {
        return ResponseEntity.ok(
                guideDayService.getDays(guideId)
        );
    }

    @Operation(summary = "Retrieve a specific day of a guide")
    @GetMapping("/{dayId}")
    public ResponseEntity<@NonNull GuideDayResponseDTO> getDay(
            @PathVariable Long guideId,
            @PathVariable Long dayId
    ) {
        return ResponseEntity.ok(
                guideDayService.getDay(guideId, dayId)
        );
    }
}
