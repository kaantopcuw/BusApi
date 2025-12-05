package com.busapi.modules.location.entity;

import com.busapi.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "districts")
@EqualsAndHashCode(callSuper = true)
public class District extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private Long population;

    private Integer area; // Yüzölçümü

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
}