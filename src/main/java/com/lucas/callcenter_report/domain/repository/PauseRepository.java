package com.lucas.callcenter_report.domain.repository;

import com.lucas.callcenter_report.domain.model.Pause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PauseRepository extends JpaRepository<Pause, Long> {
}