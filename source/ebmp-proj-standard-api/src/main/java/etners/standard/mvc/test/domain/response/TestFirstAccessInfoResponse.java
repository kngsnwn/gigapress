package etners.standard.mvc.test.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestFirstAccessInfoResponse implements Serializable {

  @Schema(description = "회사코드")
  private String cmpyCd;

  @Schema(description = "회사명")
  private String cmpyNm;

  @Schema(description = "성명")
  private String empNm;

  @Schema(description = "아이디")
  private String userId;

  @Schema(description = "유니크 아이디")
  private String unqUserId;

  @Schema(description = "사진 FILE ID")
  private String picFileId;

  @Schema(description = "회사내 사용자의 부서 코드")
  private String deptCd;

  @Schema(description = "회사내 사용자의 부서명")
  private String deptNm;

  @Schema(description = "전화번호(회사)")
  private String wkTelNo;

  @Schema(description = "전화번호(휴대폰)")
  private String spTelNo;

  @Schema(description = "사용자 사번")
  private String sabun;

  @Schema(description = "직급")
  private String psnScn;

  @Schema(description = "직급명")
  private String psnScnNm;
}
