package com.douineau.selecao.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Transient
    @JsonIgnore
    protected LocalDateTime createdAt;

    @Transient
    @JsonIgnore
    protected String createdBy;

    @Transient
    @JsonIgnore
    protected LocalDateTime updatedAt;

    @Transient
    @JsonIgnore
    protected String updatedBy;


}