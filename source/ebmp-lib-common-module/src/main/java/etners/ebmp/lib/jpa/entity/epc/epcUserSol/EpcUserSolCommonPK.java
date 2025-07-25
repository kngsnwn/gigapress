package etners.ebmp.lib.jpa.entity.epc.epcUserSol;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import java.io.Serial;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpcUserSolCommonPK implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Schema(description = "고유 사용자 아이디(아이디가 변경 가능하도록 Key 조합함)")
  @Column(name = "UNQ_USER_ID")
  private String unqUserId;

  @Schema(description = "권한구분코드")
  @Column(name = "AUTH_CD")
  private String authCd;

  @Schema(description = "솔루션 코드(EPC_SOL_MST, SOL_CD 와 연결)")
  @Column(name = "SOL_CD")
  private String solCd;

  @Schema(description = "WEB/MOBILE 구분(CD:102)")
  @Column(name = "WM_GBN")
  private String wmGbn;

  public EpcUserSolCommonPK(String unqUserId, String authCd, String solCd, String wmGbn) {
    this.unqUserId = unqUserId;
    this.authCd = authCd;
    this.solCd = solCd;
    this.wmGbn = wmGbn;
  }
}
