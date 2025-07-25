package etners.ebmp.lib.jpa.entity.epc.epecUserMst;

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
public class EpcUserMstCommonPK implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Schema(description = "고유 사용자 아이디(아이디가 변경 가능하도록 Key 조합함)")
  @Column(name = "UNQ_USER_ID")
  private String unqUserId;

  public EpcUserMstCommonPK(String unqUserId) {
    this.unqUserId = unqUserId;
  }
}
