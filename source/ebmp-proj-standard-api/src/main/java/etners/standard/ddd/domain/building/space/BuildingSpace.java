package etners.standard.ddd.domain.building.space;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor
@IdClass(BuildingSpacePK.class)
//@Table(name = "building_space")
public class BuildingSpace {

  @Id
  private Long buildingId;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String spaceName;

  private String status;

  @Builder
  public BuildingSpace(Long buildingId, String spaceName, String status) {
    this.buildingId = buildingId;
    this.spaceName = spaceName;
    this.status = status;
  }

}
