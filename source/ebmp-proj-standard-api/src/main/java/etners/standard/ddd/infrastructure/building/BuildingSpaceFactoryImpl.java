package etners.standard.ddd.infrastructure.building;

import etners.standard.ddd.domain.building.space.BuildingSpaceFactory;
import etners.standard.ddd.domain.building.Building;
import etners.standard.ddd.domain.building.BuildingCommand;
import etners.standard.ddd.domain.building.space.BuildingSpace;
import etners.standard.ddd.domain.building.space.BuildingSpaceStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BuildingSpaceFactoryImpl implements BuildingSpaceFactory {

private final BuildingSpaceStore buildingSpaceStore;

  @Override
  public List<BuildingSpace> store(BuildingCommand.RegisterBuildingRequest command, Building building) {
    var itemOptionGroupRequestList = command.getBuildingSpaceGroupRequestList();
    if (CollectionUtils.isEmpty(itemOptionGroupRequestList)) return Collections.emptyList();

    return itemOptionGroupRequestList.stream()
            .map(requestBuildingSpaceGroup -> {
              var buildingSpace = requestBuildingSpaceGroup.toEntity(building);
              return buildingSpaceStore.store(buildingSpace);
            }).collect(Collectors.toList());
  }
}
