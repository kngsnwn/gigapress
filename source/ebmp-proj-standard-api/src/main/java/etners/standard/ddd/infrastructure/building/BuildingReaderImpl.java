package etners.standard.ddd.infrastructure.building;

import etners.common.config.exception.ApplicationException;
import etners.standard.ddd.domain.building.Building;
import etners.standard.ddd.interfaces.building.BuildingDtoMapper;
import etners.standard.ddd.domain.building.BuildingInfo;
import etners.standard.ddd.domain.building.BuildingReader;
import etners.standard.ddd.domain.building.space.BuildingSpace;
import etners.standard.ddd.infrastructure.building.space.BuildingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BuildingReaderImpl implements BuildingReader {
  private final BuildingRepository buildingRepository;
  private final BuildingSpaceRepository buildingSpaceRepository;
  private final BuildingDtoMapper buildingDtoMapper;

  @Override
  public Building getBuilding(Long buildingId) {
    return buildingRepository.findById(buildingId).orElseThrow(ApplicationException::new);
  }

  @Override
  public List<BuildingInfo.BuildingSpace> getBuildingSpaces(Building building) {
    List<BuildingSpace> buildingSpaceList = buildingSpaceRepository.findByBuildingId(building.getId());
    List<BuildingInfo.BuildingSpace> buildingSpaceInfo = buildingDtoMapper.of(buildingSpaceList);
    return buildingSpaceInfo;
  }
}
