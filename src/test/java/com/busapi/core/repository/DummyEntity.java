package com.busapi.core.repository;

import com.busapi.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dummy_entity")
@Getter
@Setter
class DummyEntity extends BaseEntity {
    private String name;
}
