package etners.standard.ddd.interfaces.building;

import etners.standard.ddd.domain.building.BuildingCommand;
import etners.standard.ddd.domain.building.BuildingInfo;
import etners.standard.ddd.domain.building.space.BuildingSpace;
import org.mapstruct.*;

import java.util.List;

@Mapper(
  componentModel = "spring",
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface BuildingDtoMapper {

  @Mappings({@Mapping(source = "request.buildingSpaceGroupList", target = "buildingSpaceGroupRequestList")})
  BuildingCommand.RegisterBuildingRequest of(BuildingDto.RegisterBuildingRequest request);

  BuildingDto.RegisterBuildingResponse of(String buildingId);

  BuildingDto.Main of(BuildingInfo.Main main);

  List<BuildingInfo.BuildingSpace> of(List<BuildingSpace> buildingSpace);
}
