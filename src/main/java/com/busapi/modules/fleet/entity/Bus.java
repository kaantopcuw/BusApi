package com.busapi.modules.fleet.entity;

import com.busapi.core.entity.BaseEntity;
import com.busapi.modules.fleet.enums.BusType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "buses")
@EqualsAndHashCode(callSuper = true)
// BaseEntity'deki global @SQLRestriction("is_deleted = false") zaten var,
// ancak hibernate native delete çağrılırsa diye buraya da soft delete sql'i ekleyebiliriz.
public class Bus extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String plateNumber; // Plaka (34 ABC 123)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusType busType;

    @Column(nullable = false)
    private int seatCapacity;

    private boolean isActive = true; // Arıza vs. durumunda pasife çekmek için
}