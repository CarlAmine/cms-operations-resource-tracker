package org.cmstracker.domain.repository;

import org.cmstracker.domain.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByName(String name);
}
