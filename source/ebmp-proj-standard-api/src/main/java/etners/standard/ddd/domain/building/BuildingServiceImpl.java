package etners.standard.ddd.domain.building;

import etners.common.util.scope.CurrentUserData;
import etners.standard.ddd.domain.building.space.BuildingSpaceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Service
public class BuildingServiceImpl implements BuildingService{

  private final BuildingStore buildingStore;
  private final BuildingReader buildingReader;
  private final BuildingSpaceFactory buildingSpaceFactory;
  private final CurrentUserData currentUserData;

  @Transactional
  @Override
  public String registerBuilding(BuildingCommand.RegisterBuildingRequest command) {
    var initBuilding = command.toEntity(currentUserData.getUnqUserId());
    var building = buildingStore.store(initBuilding);
    buildingSpaceFactory.store(command, building);
    return initBuilding.getId().toString();
  }

  @Transactional(readOnly = true)
  @Override
  public BuildingInfo.Main getBuildingInfo(Long buildingId) {
    var building = buildingReader.getBuilding(buildingId);
    var buildingSpaceGroupInfoList = buildingReader.getBuildingSpaces(building);
    return new BuildingInfo.Main(building, buildingSpaceGroupInfoList);
  }

  @Transactional
  @Override
  public void disableBuilding(Long buildingId) {
    var building = buildingReader.getBuilding(buildingId);
    building.disable();
    var buildingSpaceGroupInfoList = buildingReader.getBuildingSpaces(building);
    disableIfNotEmpty(buildingSpaceGroupInfoList, building::disableBuildingSpaces);
  }
  private <T> void disableIfNotEmpty(List<T> list, Consumer<List<T>> disableMethod) {
    if (!CollectionUtils.isEmpty(list)) {
      disableMethod.accept(list);
    }
  }
}
