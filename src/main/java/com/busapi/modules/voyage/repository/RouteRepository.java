package com.busapi.modules.voyage.repository;

import com.busapi.modules.voyage.entity.Route;
import com.busapi.modules.voyage.entity.Trip;
import com.busapi.modules.voyage.entity.Voyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
}

