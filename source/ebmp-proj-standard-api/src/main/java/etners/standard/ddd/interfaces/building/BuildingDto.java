package etners.standard.ddd.interfaces.building;

import etners.standard.ddd.domain.building.Building;
import etners.standard.ddd.domain.building.BuildingInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

public class BuildingDto {

  public record RegisterBuildingRequest (
    @NotEmpty @Schema(description = "건물명") String buildingName
    , List<RegisterBuildingSpaceGroupRequest> buildingSpaceGroupList){
  }

  public record  RegisterBuildingSpaceGroupRequest ( String spaceName){
  }

  public record  RegisterBuildingResponse (String buildingId){
  }

  public record  Main (String buildingName,  Building.Status status, String unqUserId, LocalDateTime rgstDt, Long id, List<BuildingInfo.BuildingSpace> buildingSpaceGroupList){
  }
}
