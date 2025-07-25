package etners.standard.mvc.test.controller;

import etners.common.domains.auth.EpcAuth;
import etners.common.util.annotation.response.Mobile;
import etners.common.util.annotation.response.Web;
import etners.common.util.annotation.security.Permission;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.kendomodel.KendoPagingResultModel;
import etners.standard.mvc.test.domain.request.*;
import etners.standard.mvc.test.domain.response.*;
import etners.standard.mvc.test.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
@Permission(authGroups = {EpcAuth.SYSTEM_ADMINISTRATOR, EpcAuth.ETNERS_LEADER})
@Web
public class TestController {

  private final TestService testService;

  @Operation(summary = "테스트 코드 리스트 API"
      , responses = {
      @ApiResponse(responseCode = "200"
          , description = "성공"
          , content = @Content(mediaType = "application/json"
          , schema = @Schema(implementation = TestListResponse.class)))
  })
  @Parameters({
      @Parameter(name = "locale", description = "언어", required = false, in = ParameterIn.HEADER, example = "ko"),
      @Parameter(name = "wmGbn", description = "웹 모바일 구분", required = false, in = ParameterIn.HEADER, example = "01")
  })
  @Mobile
  @GetMapping
  public @ResponseBody
  KendoPagingResultModel<TestListResponse> getCodeList(
      @Valid TestListRequestParam requestParam) {
    return testService.getCodeList(requestParam);
  }

  @Operation(summary = "테스트 코드 디테일 API"
      , responses = {
      @ApiResponse(responseCode = "200"
          , description = "성공"
          , content = @Content(mediaType = "application/json"
          , schema = @Schema(implementation = TestDetailResponse.class)))
  })
  @Parameters({
      @Parameter(name = "locale", description = "언어", required = false, in = ParameterIn.HEADER, example = "ko"),
      @Parameter(name = "wmGbn", description = "웹 모바일 구분", required = false, in = ParameterIn.HEADER, example = "01")
  })
  @Mobile
  @GetMapping(value = "/{solCd}/{cdGrup}")
  public @ResponseBody
  ResultModel<TestDetailResponse> getCodeDetail(
      @NotEmpty @PathVariable(name = "solCd") String solCd,
      @NotEmpty @PathVariable(name = "cdGrup") String cdGrup,
      @Valid TestDetailRequestParam requestParam) {
    requestParam.setSolCd(solCd);
    requestParam.setCdGrup(cdGrup);
    return testService.getCodeDetail(requestParam);
  }

  @Operation(summary = "테스트 코드 INSERT API"
      , responses = {
      @ApiResponse(responseCode = "200"
          , description = "성공"
          , content = @Content(mediaType = "application/json"
          , schema = @Schema(implementation = TestInsertResponse.class)))
  })
  @Parameters({
      @Parameter(name = "locale", description = "언어", required = false, in = ParameterIn.HEADER, example = "ko"),
      @Parameter(name = "wmGbn", description = "웹 모바일 구분", required = false, in = ParameterIn.HEADER, example = "01")
  })
  @PostMapping
  public ResultModel<TestInsertResponse> insertCodeGrup(
      @RequestBody @Valid TestInsertRequestParam requestParam) {
    return testService.insertCodeGrup(requestParam);
  }

  @Operation(summary = "테스트 코드 UPDATE 사용여부 API"
      , responses = {
      @ApiResponse(responseCode = "200"
          , description = "성공"
          , content = @Content(mediaType = "application/json"
          , schema = @Schema(implementation = TestUpdateResponse.class)))
  })
  @Parameters({
      @Parameter(name = "locale", description = "언어", required = false, in = ParameterIn.HEADER, example = "ko"),
      @Parameter(name = "wmGbn", description = "웹 모바일 구분", required = false, in = ParameterIn.HEADER, example = "01")
  })
  @PutMapping(value = "/{solCd}/{cdGrup}")
  public ResultModel<TestUpdateResponse> updateCodeGrup(
      @NotEmpty @PathVariable(name = "solCd") String solCd,
      @NotEmpty @PathVariable(name = "cdGrup") String cdGrup,
      @RequestBody @Valid TestUpdateRequestParam requestParam) {
    requestParam.setSolCd(solCd);
    requestParam.setCdGrup(cdGrup);

    return testService.updateCodeGrup(requestParam);
  }

  @Operation(summary = "테스트 코드 삭제 API"
      , responses = {
      @ApiResponse(responseCode = "200"
          , description = "성공"
          , content = @Content(mediaType = "application/json"
          , schema = @Schema(implementation = TestDeleteResponse.class)))
  })
  @Parameters({
      @Parameter(name = "locale", description = "언어", required = false, in = ParameterIn.HEADER, example = "ko"),
      @Parameter(name = "wmGbn", description = "웹 모바일 구분", required = false, in = ParameterIn.HEADER, example = "01")
  })
  @DeleteMapping(value = "/{solCd}/{cdGrup}")
  public ResultModel<TestDeleteResponse> deleteCodeGrup(
      @NotEmpty @PathVariable(name = "solCd") String solCd,
      @NotEmpty @PathVariable(name = "cdGrup") String cdGrup,
      @RequestBody @Valid TestDeleteRequestParam requestParam) {
    requestParam.setSolCd(solCd);
    requestParam.setCdGrup(cdGrup);

    return testService.deleteCodeGrup(requestParam);
  }

  @Operation(summary = "테스트 메뉴 리스트 API"
      , responses = {
      @ApiResponse(responseCode = "200"
          , description = "성공"
          , content = @Content(mediaType = "application/json"
          , schema = @Schema(implementation = TestMenuListResponse.class)))
  })
  @Mobile
  @Parameters({
      @Parameter(name = "locale", description = "언어", required = false, in = ParameterIn.HEADER, example = "ko"),
      @Parameter(name = "wmGbn", description = "웹 모바일 구분", required = false, in = ParameterIn.HEADER, example = "01")
  })
  @GetMapping(value = "/menu")
  public @ResponseBody
  ResultModel<List<TestMenuListResponse>> getMenuList(
      @Valid TestMenuListRequestParam requestParam) {

    return testService.getMenuList(requestParam);
  }

  @Operation(summary = "테스트 first access info API"
      , responses = {
      @ApiResponse(responseCode = "200"
          , description = "성공"
          , content = @Content(mediaType = "application/json"
          , schema = @Schema(implementation = TestMenuListResponse.class)))
  })
  @Mobile
  @Parameters({
      @Parameter(name = "locale", description = "언어", required = false, in = ParameterIn.HEADER, example = "ko"),
      @Parameter(name = "wmGbn", description = "웹 모바일 구분", required = false, in = ParameterIn.HEADER, example = "01")
  })
  @GetMapping(value = "/first-access-info")
  public @ResponseBody
  ResultModel<TestFirstAccessInfoResponse> getMenuList(
      @Valid TestFirstAccessInfoRequestParam requestParam) {

    return testService.getFirstAccessInfo(requestParam);
  }

  @Operation(summary = "RSA 암호화 상세 조회 테스트 API"
    , responses = {
    @ApiResponse(responseCode = "200"
      , description = "성공"
      , content = @Content(mediaType = "application/json"
      , schema = @Schema(implementation = TestRsaResponse.class)))
  })
  @GetMapping("/rsa")
  public ResultModel<TestRsaResponse> getRsaTestDetail(){
    return testService.getRsaTestDetail();
  }
}
