package com.zenora.api;

import com.zenora.dto.ContributionRequestDto;
import com.zenora.entity.ContributionEntity;
import com.zenora.entity.GoalEntity;
import com.zenora.repository.ContributionRepository;
import com.zenora.repository.GoalRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ✅ PR-3: REST API Controller untuk Contribution.
 *
 * ✅ KETENTUAN — Spring Boot REST API + Validation:
 *   Setiap POST wajib lolos @Valid sebelum diproses.
 *   Saat kontribusi ditambah, currentSaving di GoalEntity otomatis diperbarui.
 */
@RestController
@RequestMapping("/api/contributions")
@CrossOrigin(origins = "*")
public class ContributionApiController {

    private final ContributionRepository contributionRepository;
    private final GoalRepository goalRepository;

    public ContributionApiController(ContributionRepository contributionRepository,
                                     GoalRepository goalRepository) {
        this.contributionRepository = contributionRepository;
        this.goalRepository         = goalRepository;
    }

    // ── GET /api/contributions ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<ContributionEntity>> getAll() {
        return ResponseEntity.ok(contributionRepository.findAll());
    }

    // ── GET /api/contributions/goal/{goalId} ───────────────────────────────
    @GetMapping("/goal/{goalId}")
    public ResponseEntity<List<ContributionEntity>> getByGoal(@PathVariable String goalId) {
        return ResponseEntity.ok(contributionRepository.findByGoalIdOrderByDateDesc(goalId));
    }

    // ── POST /api/contributions ────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> addContribution(@Valid @RequestBody ContributionRequestDto req) {
        // Validasi: goal harus ada
        GoalEntity goal = goalRepository.findById(req.getGoalId()).orElse(null);
        if (goal == null) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Goal dengan id '" + req.getGoalId() + "' tidak ditemukan"));
        }

        // Simpan kontribusi
        ContributionEntity contrib = new ContributionEntity(
            req.getGoalId(), req.getAmount(), req.getDate(), req.getNote()
        );
        ContributionEntity saved = contributionRepository.save(contrib);

        // Update currentSaving di Goal
        double newSaving = contributionRepository.sumAmountByGoalId(req.getGoalId());
        goal.setCurrentSaving(newSaving);
        goalRepository.save(goal);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── DELETE /api/contributions/{id} ─────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContribution(@PathVariable String id) {
        ContributionEntity contrib = contributionRepository.findById(id).orElse(null);
        if (contrib == null) return ResponseEntity.notFound().build();

        contributionRepository.deleteById(id);

        // Recalculate currentSaving untuk goal terkait
        goalRepository.findById(contrib.getGoalId()).ifPresent(goal -> {
            double newSaving = contributionRepository.sumAmountByGoalId(contrib.getGoalId());
            goal.setCurrentSaving(newSaving);
            goalRepository.save(goal);
        });

        return ResponseEntity.noContent().build();
    }
}
