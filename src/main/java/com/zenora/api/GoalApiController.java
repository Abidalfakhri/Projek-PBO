package com.zenora.api;

import com.zenora.dto.GoalRequestDto;
import com.zenora.dto.GoalResponseDto;
import com.zenora.entity.GoalEntity;
import com.zenora.repository.GoalRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ✅ PR-3: REST API Controller untuk Goal.
 *
 * ✅ KETENTUAN — REST API dengan Spring Boot:
 *   @RestController  = otomatis serialisasi respons ke JSON
 *   @RequestMapping  = prefix URL untuk semua endpoint di class ini
 *   @Valid           = trigger Bean Validation sebelum masuk method
 *
 * ✅ KETENTUAN — MVC Architecture:
 *   Controller → hanya handle request/response HTTP
 *   Tidak ada logika bisnis di sini → logika di Service (PR-2 pakai Repository)
 *
 * ✅ OOP PILAR — ENCAPSULATION:
 *   GoalRepository disuntik via constructor injection.
 *   Controller tidak perlu tahu implementasi SQL-nya.
 */
@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "*") // untuk JavaFX client
public class GoalApiController {

    private final GoalRepository goalRepository;

    // Constructor Injection (best practice Spring)
    public GoalApiController(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    // ── GET /api/goals ─────────────────────────────────────────────────────
    /** Ambil semua goal, diurutkan berdasarkan priority. */
    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getAllGoals() {
        List<GoalResponseDto> result = goalRepository.findAllByOrderByPriorityAsc()
                .stream()
                .map(GoalResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── GET /api/goals/{id} ────────────────────────────────────────────────
    /** Ambil satu goal berdasarkan ID. */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponseDto> getGoalById(@PathVariable String id) {
        return goalRepository.findById(id)
                .map(GoalResponseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── POST /api/goals ────────────────────────────────────────────────────
    /** Buat goal baru. @Valid = jalankan Bean Validation dulu. */
    @PostMapping
    public ResponseEntity<GoalResponseDto> createGoal(@Valid @RequestBody GoalRequestDto req) {
        GoalEntity entity = new GoalEntity();
        entity.setName(req.getName());
        entity.setTargetAmount(req.getTargetAmount());
        entity.setMonths(req.getMonths());
        entity.setInterestRate(req.getInterestRate() != null ? req.getInterestRate() : 0.0);
        entity.setPriority(req.getPriority() != null ? req.getPriority() : 3);
        entity.setCategory(req.getCategory() != null ? req.getCategory() : "UMUM");
        entity.setCurrentSaving(0.0);

        GoalEntity saved = goalRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(GoalResponseDto.fromEntity(saved));
    }

    // ── PUT /api/goals/{id} ────────────────────────────────────────────────
    /** Update goal yang sudah ada. */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponseDto> updateGoal(
            @PathVariable String id,
            @Valid @RequestBody GoalRequestDto req) {

        return goalRepository.findById(id).map(entity -> {
            entity.setName(req.getName());
            entity.setTargetAmount(req.getTargetAmount());
            entity.setMonths(req.getMonths());
            if (req.getInterestRate() != null) entity.setInterestRate(req.getInterestRate());
            if (req.getPriority() != null)     entity.setPriority(req.getPriority());
            if (req.getCategory() != null)     entity.setCategory(req.getCategory());
            GoalEntity updated = goalRepository.save(entity);
            return ResponseEntity.ok(GoalResponseDto.fromEntity(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE /api/goals/{id} ─────────────────────────────────────────────
    /** Hapus goal. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable String id) {
        if (!goalRepository.existsById(id)) return ResponseEntity.notFound().build();
        goalRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/goals/summary ─────────────────────────────────────────────
    /** Ringkasan statistik semua goal. */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        double totalTarget  = goalRepository.sumAllTargetAmounts();
        double totalCurrent = goalRepository.sumAllCurrentSavings();
        double progress     = totalTarget > 0 ? (totalCurrent / totalTarget) * 100 : 0;

        return ResponseEntity.ok(Map.of(
            "totalGoals",       goalRepository.count(),
            "activeGoals",      goalRepository.findActiveGoals().size(),
            "totalTarget",      totalTarget,
            "totalCurrentSaving", totalCurrent,
            "overallProgress",  Math.round(progress * 10.0) / 10.0
        ));
    }
}
