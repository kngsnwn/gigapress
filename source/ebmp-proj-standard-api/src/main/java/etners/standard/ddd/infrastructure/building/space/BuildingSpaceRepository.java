package etners.standard.ddd.infrastructure.building.space;

import etners.standard.ddd.domain.building.space.BuildingSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildingSpaceRepository extends JpaRepository<BuildingSpace, Long>{

  List<BuildingSpace> findByBuildingId(Long id);
}
