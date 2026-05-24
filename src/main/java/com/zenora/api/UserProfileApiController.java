package com.zenora.api;

import com.zenora.dto.UserProfileRequestDto;
import com.zenora.entity.UserProfileEntity;
import com.zenora.repository.UserProfileRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ✅ PR-3: REST API Controller untuk UserProfile.
 *
 * ✅ KETENTUAN — Spring Boot REST API + Spring Validation:
 *   GET  /api/profile       — ambil profil aktif
 *   POST /api/profile       — buat profil baru (jika belum ada)
 *   PUT  /api/profile/{id}  — update profil
 */
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class UserProfileApiController {

    private final UserProfileRepository userProfileRepository;

    public UserProfileApiController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    // ── GET /api/profile ───────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<UserProfileEntity> getProfile() {
        return userProfileRepository.findFirstByOrderByCreatedAtAsc()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ── POST /api/profile ──────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<UserProfileEntity> createProfile(
            @Valid @RequestBody UserProfileRequestDto req) {

        UserProfileEntity entity = new UserProfileEntity();
        applyRequest(entity, req);
        UserProfileEntity saved = userProfileRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── PUT /api/profile/{id} ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileEntity> updateProfile(
            @PathVariable String id,
            @Valid @RequestBody UserProfileRequestDto req) {

        return userProfileRepository.findById(id).map(entity -> {
            applyRequest(entity, req);
            return ResponseEntity.ok(userProfileRepository.save(entity));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private void applyRequest(UserProfileEntity entity, UserProfileRequestDto req) {
        entity.setName(req.getName());
        entity.setAge(req.getAge());
        entity.setMonthlyIncome(req.getMonthlyIncome());
        entity.setMonthlyExpense(req.getMonthlyExpense());
        entity.setEmergencyMonths(req.getEmergencyMonths());
        entity.setHouseholdStatus(req.getHouseholdStatus());
        entity.setInflationPct(req.getInflationPct());
    }
}
