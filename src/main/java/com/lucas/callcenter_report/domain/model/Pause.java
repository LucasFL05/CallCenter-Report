package com.lucas.callcenter_report.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Pause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private LocalDateTime start;

    @Column(name = "`end`", nullable = true)
    private LocalDateTime end;

    @Column(nullable = true)
    private Integer duration;

    @Column(nullable = true)
    private String operator;

    @Column(nullable = true)
    private String type;

    // Construtor padrão necessário para JPA
    public Pause() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator != null ? operator.trim() : null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type != null ? type.trim() : null;
    }
}