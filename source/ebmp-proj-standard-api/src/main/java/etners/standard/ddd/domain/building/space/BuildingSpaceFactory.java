package etners.standard.ddd.domain.building.space;

import etners.standard.ddd.domain.building.Building;
import etners.standard.ddd.domain.building.BuildingCommand;

import java.util.List;

public interface BuildingSpaceFactory {
  List<BuildingSpace> store(BuildingCommand.RegisterBuildingRequest command, Building building);
}
