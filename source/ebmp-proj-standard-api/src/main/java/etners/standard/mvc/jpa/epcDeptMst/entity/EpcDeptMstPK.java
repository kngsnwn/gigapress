package etners.standard.mvc.jpa.epcDeptMst.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import java.io.Serial;
import jakarta.persistence.Column;
import java.io.Serializable;


@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpcDeptMstPK implements Serializable {

   @Serial
   private static final long serialVersionUID = 1L;

    @Schema(description = "회사코드")
    @Column(name = "CMPY_CD")
    private String cmpyCd;

    @Schema(description = "부서코드")
    @Column(name = "DEPT_CD")
    private String deptCd;

  public EpcDeptMstPK(String cmpyCd, String deptCd) {
    this.cmpyCd = cmpyCd;
    this.deptCd = deptCd;
  }
}
