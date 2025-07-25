package etners.standard.ddd.infrastructure.building;

import etners.common.config.exception.ApplicationException;
import etners.common.util.enumType.ErrorCode;
import etners.standard.ddd.domain.building.Building;
import etners.standard.ddd.domain.building.BuildingStore;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuildingStoreImpl implements BuildingStore {

  private final BuildingRepository buildingRepository;

  @Override
  public Building store(Building building) {
    validCheck(building);
    return buildingRepository.save(building);
  }

  private void validCheck(Building building) {
    if (StringUtils.isEmpty(building.getBuildingName())) throw new ApplicationException(ErrorCode.NOT_FOUND_AUTH);
    if (StringUtils.isEmpty(building.getUnqUserId())) throw new ApplicationException(ErrorCode.NOT_FOUND_AUTH);
  }
}
