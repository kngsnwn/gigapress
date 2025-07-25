package etners.standard.ddd.interfaces.building;

import etners.common.util.annotation.response.Mobile;
import etners.common.util.annotation.response.Web;
import etners.common.util.scope.CurrentUserData;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.resultstatus.manager.ResultStatusManager;
import etners.standard.ddd.application.building.BuildingFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/buildings")
@Web
public class BuildingController {

  private final CurrentUserData currentUserData;
  private final BuildingFacade buildingFacade;
  private final BuildingDtoMapper buildingDtoMapper;

  @PostMapping
  public ResultModel<Object> registerBuilding(@RequestBody @Valid BuildingDto.RegisterBuildingRequest request) {
    var buildingCommand = buildingDtoMapper.of(request);
    var buildingId = buildingFacade.registerBuilding(buildingCommand);
    var response = buildingDtoMapper.of(buildingId);
    return ResultStatusManager.successResultModel(currentUserData.getEbmpLang(), response);
  }

  @Mobile
  @GetMapping("/{buildingId}")
  public ResultModel<Object> getBuilding(@PathVariable("buildingId") Long buildingId) {
    var buildingInfo = buildingFacade.getBuildingInfo(buildingId);
    var response = buildingDtoMapper.of(buildingInfo);
    return ResultStatusManager.successResultModel(currentUserData.getEbmpLang(), response);
  }

  @DeleteMapping("")
  public ResultModel disableBuilding(@RequestParam("buildingId") Long buildingId) {
    buildingFacade.disableBuilding(buildingId);
    return ResultStatusManager.successResultModel(currentUserData.getEbmpLang());
  }
}
