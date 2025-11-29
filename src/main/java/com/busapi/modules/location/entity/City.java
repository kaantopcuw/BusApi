package com.busapi.modules.location.entity;

import com.busapi.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@Entity
@Table(name = "cities")
@EqualsAndHashCode(callSuper = true)
public class City extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private int plateCode;

    // Bir şehrin ilçeleri (Eager fetch yapmıyoruz, performans için Lazy kalmalı)
    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<District> districts;
}