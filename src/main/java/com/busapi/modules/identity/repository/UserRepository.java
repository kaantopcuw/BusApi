package com.busapi.modules.identity.repository;

import com.busapi.core.repository.BaseRepository;
import com.busapi.modules.identity.entity.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
