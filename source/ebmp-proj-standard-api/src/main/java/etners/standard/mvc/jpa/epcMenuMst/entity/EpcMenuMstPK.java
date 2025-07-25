package etners.standard.mvc.jpa.epcMenuMst.entity;

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
public class EpcMenuMstPK implements Serializable {

   @Serial
   private static final long serialVersionUID = 1L;

    @Schema(description = "솔루션코드")
    @Column(name = "SOL_CD")
    private String solCd;

    @Schema(description = "WEB/MOBILE 구분(CD:102), 단 이솝메인(solCd : 0002)은 메뉴 구성을 위해 11, 21을 별도로 추가하여 사용하고 있다.")
    @Column(name = "WM_GBN")
    private String wmGbn;

    @Schema(description = "메뉴 ID")
    @Column(name = "MENU_ID")
    private String menuId;

  public EpcMenuMstPK(String solCd, String wmGbn, String menuId) {
    this.solCd = solCd;
    this.wmGbn = wmGbn;
    this.menuId = menuId;
  }
}
