package com.busapi.modules.identity.entity;

import com.busapi.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "agencies")
@EqualsAndHashCode(callSuper = true)
public class Agency extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;
    private String address;
    private String contactPhone;
    private boolean isActive = true;// Acenta kapatıldı mı?

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "district_id")
//    private District location; // İlçe bilgisi şehri de kapsar

}