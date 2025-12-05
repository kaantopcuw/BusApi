package com.busapi.modules.location.entity;

import com.busapi.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "cities")
@EqualsAndHashCode(callSuper = true)
public class City extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private int plateCode; // JSON'daki "id" buraya eşleşecek

    private Long population; // Nüfus

    private Integer area; // Yüzölçümü (km2)

    private Integer altitude; // Rakım

    private boolean isCoastal; // Sahil şehri mi?

    private boolean isMetropolitan; // Büyükşehir mi?

    // Alan Kodları (Örn: 212, 216)
    // Bu, cities_area_codes adında yan bir tablo oluşturur.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "city_area_codes", joinColumns = @JoinColumn(name = "city_id"))
    @Column(name = "code")
    private List<Integer> areaCodes = new ArrayList<>();

    // Koordinatlar (Enlem/Boylam)
    @Embedded
    private Coordinates coordinates;

    // Bölge (Akdeniz, Marmara vb.) - Sadece TR ismini tutacağız
    private String region;

    private String googleMapUrl;

    private String openStreetMap;

    // --- İlişkiler ---

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<District> districts;
}