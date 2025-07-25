package etners.standard.ddd.domain.building;

import etners.standard.ddd.domain.building.space.BuildingSpace;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

public class BuildingCommand {
  @Getter
  @Builder
  @ToString
  public static class RegisterBuildingRequest {
    private final String buildingName;
    private final List<RegisterBuildingSpaceGroupRequest> buildingSpaceGroupRequestList; // ex) 색상, 사이즈

    public Building toEntity(String unqUserId) {
      return Building.builder()
        .buildingName(buildingName)
        .rgstDt(LocalDateTime.now())
        .unqUserId(unqUserId)
        .build();
    }
  }

  @Getter
  @Builder
  @ToString
  public static class RegisterBuildingSpaceGroupRequest {  // ex) 색상
    private final String spaceName;

    public BuildingSpace toEntity(Building building) {
      return BuildingSpace.builder()
        .buildingId(building.getId())
        .spaceName(spaceName)
        .build();
    }
  }
}
