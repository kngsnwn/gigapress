package etners.standard.ddd.infrastructure.building;

import etners.standard.ddd.domain.building.Building;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long>{
}
