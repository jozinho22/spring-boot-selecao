package com.douineau.selecao.model;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Player extends AbstractEntity {

    private String name;
    private Integer number;
}
