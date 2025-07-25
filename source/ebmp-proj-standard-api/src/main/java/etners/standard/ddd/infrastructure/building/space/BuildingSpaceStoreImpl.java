package etners.standard.ddd.infrastructure.building.space;

import etners.standard.ddd.domain.building.space.BuildingSpace;
import etners.standard.ddd.domain.building.space.BuildingSpaceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BuildingSpaceStoreImpl implements BuildingSpaceStore {
  private final BuildingSpaceRepository buildingSpaceRepository;

  @Override
  public BuildingSpace store(BuildingSpace buildingSpace) {
    return buildingSpaceRepository.save(buildingSpace);
  }
}
