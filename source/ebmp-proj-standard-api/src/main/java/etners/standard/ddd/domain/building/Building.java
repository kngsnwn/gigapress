package etners.standard.ddd.domain.building;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Entity
@NoArgsConstructor
@Table(name = "building")
public class Building {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String buildingName;

  @Enumerated(EnumType.STRING)
  private Status status;

  private String unqUserId;

  private LocalDateTime rgstDt;
  private String useYn;

  public void disable() {
    this.useYn = "N";
  }

  public void disableBuildingSpaces(List<BuildingInfo.BuildingSpace> buildingSpaces) {
    buildingSpaces.forEach(BuildingInfo.BuildingSpace::disable);
  }

  @Getter
  @RequiredArgsConstructor
  public enum Status {
    PREPARE("준비중"),
    ING("진행중"),
    END("완료");

    private final String description;
  }

  @Builder
  public Building(String buildingName, String unqUserId, LocalDateTime rgstDt) {
    this.buildingName = buildingName;
    this.unqUserId = unqUserId;
    this.rgstDt = rgstDt;
    this.status = Status.PREPARE;
  }
}
