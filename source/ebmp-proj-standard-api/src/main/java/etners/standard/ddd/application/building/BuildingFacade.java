package etners.standard.ddd.application.building;

import etners.standard.ddd.domain.building.BuildingCommand;
import etners.standard.ddd.domain.building.BuildingInfo;
import etners.standard.ddd.domain.building.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuildingFacade {

  private final BuildingService buildingService;

  public String registerBuilding(BuildingCommand.RegisterBuildingRequest request) {
    String buildingId = buildingService.registerBuilding(request);
    return buildingId;
  }

  public BuildingInfo.Main getBuildingInfo(Long buildingId) {
    return buildingService.getBuildingInfo(buildingId);
  }

  public void disableBuilding(Long buildingId) {
    buildingService.disableBuilding(buildingId);
  }
}
