package etners.standard.ddd.domain.building;

import java.util.List;

public interface BuildingReader {
  Building getBuilding(Long buildingId);
  List<BuildingInfo.BuildingSpace> getBuildingSpaces(Building building);
}
