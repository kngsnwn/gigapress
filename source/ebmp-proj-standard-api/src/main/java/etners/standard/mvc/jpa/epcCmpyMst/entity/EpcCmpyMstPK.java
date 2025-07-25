package etners.standard.mvc.jpa.epcCmpyMst.entity;

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
public class EpcCmpyMstPK implements Serializable {

   @Serial
   private static final long serialVersionUID = 1L;

    @Schema(description = "업체코드")
    @Column(name = "CMPY_CD")
    private String cmpyCd;

  public EpcCmpyMstPK(String cmpyCd) {
    this.cmpyCd = cmpyCd;
  }
}
