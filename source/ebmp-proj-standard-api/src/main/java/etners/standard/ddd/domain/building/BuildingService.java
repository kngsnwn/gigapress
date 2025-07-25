package etners.standard.ddd.domain.building;

public interface BuildingService {
    String registerBuilding(BuildingCommand.RegisterBuildingRequest buildingCommand);

    BuildingInfo.Main getBuildingInfo(Long buildingId);

  void disableBuilding(Long buildingId);
}
