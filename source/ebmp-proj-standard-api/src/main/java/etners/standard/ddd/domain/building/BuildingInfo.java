package etners.standard.ddd.domain.building;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

public class BuildingInfo {

  @Getter
  @ToString
  public static class Main {

    private final String buildingName;
    private final Building.Status status;
    private String unqUserId;
    private LocalDateTime rgstDt;
    private final Long id;
    private final List<BuildingSpace> buildingSpaceGroupList;

    public Main(Building item, List<BuildingSpace> buildingSpaceGroupList) {

      this.status = item.getStatus();
      this.buildingName = item.getBuildingName();
      this.unqUserId = item.getUnqUserId();
      this.rgstDt = item.getRgstDt();
      this.id = item.getId();
      this.buildingSpaceGroupList = buildingSpaceGroupList;
    }
  }

  @Getter
  @ToString
  public static class BuildingSpace {
    private  Long id;
    private  String spaceName;
    private  String status;

    public void disable() {
      this.status = Status.REMOVE.getDescription();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Status {
      PREPARE("01","준비중"),
      ING("02","진행중"),
      END("03","완료"),
      REMOVE("04","폐기");
      private final String code;
      private final String description;
    }
    @Builder
    public BuildingSpace(etners.standard.ddd.domain.building.space.BuildingSpace buildingSpace) {
      this.spaceName = buildingSpace.getSpaceName();
      this.id = buildingSpace.getId();
    }
  }
}

