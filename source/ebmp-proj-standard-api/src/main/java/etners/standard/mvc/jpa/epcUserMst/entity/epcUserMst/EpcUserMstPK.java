package etners.standard.mvc.jpa.epcUserMst.entity.epcUserMst;

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
public class EpcUserMstPK implements Serializable {

   @Serial
   private static final long serialVersionUID = 1L;

    @Schema(description = "고유 사용자 아이디(아이디가 변경 가능하도록 Key 조합함)")
    @Column(name = "UNQ_USER_ID")
    private String unqUserId;

  public EpcUserMstPK(String unqUserId) {
    this.unqUserId = unqUserId;
  }
}
