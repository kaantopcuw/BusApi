package com.busapi.modules.identity.repository;

import com.busapi.core.repository.BaseRepository;
import com.busapi.modules.identity.entity.Agency;

public interface AgencyRepository extends BaseRepository<Agency> {
    boolean existsByName(String name);
}
