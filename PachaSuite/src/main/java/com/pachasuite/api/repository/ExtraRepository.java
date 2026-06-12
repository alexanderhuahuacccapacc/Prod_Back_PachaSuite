package com.pachasuite.api.repository;

import com.pachasuite.api.entities.Extra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtraRepository extends JpaRepository<Extra, Long> {

    Optional<Extra> findByCodigo(String codigo);

    List<Extra> findByCodigoIn(List<String> codigos);
}