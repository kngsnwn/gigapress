package etners.standard.ddd.domain.building.space;

import jakarta.persistence.*;
import lombok.Getter;


@Getter
public class BuildingSpacePK {

  @Id
  private Long buildingId;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

}
