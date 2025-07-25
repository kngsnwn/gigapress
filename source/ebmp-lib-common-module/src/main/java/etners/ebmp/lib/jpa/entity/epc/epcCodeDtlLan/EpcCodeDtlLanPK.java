package etners.ebmp.lib.jpa.entity.epc.epcCodeDtlLan;

import jakarta.persistence.Column;
import java.io.Serializable;
import lombok.Data;


@Data
public class EpcCodeDtlLanPK implements Serializable {

  private static final long serialVersionUID = -5910044454585880101L;

  @Column(name = "CMPY_CD", nullable = false, insertable = false, updatable = false)
  private String cmpyCd;

  @Column(name = "CD_GRUP", nullable = false)
  private String cdGrup;

  @Column(name = "CD_ID", nullable = false, length = 20)
  private String cdId;
}
