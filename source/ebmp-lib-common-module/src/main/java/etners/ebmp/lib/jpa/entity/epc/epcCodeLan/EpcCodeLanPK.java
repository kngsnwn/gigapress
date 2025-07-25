package etners.ebmp.lib.jpa.entity.epc.epcCodeLan;

import jakarta.persistence.Column;
import java.io.Serializable;
import lombok.Data;

@Data
public class EpcCodeLanPK implements Serializable {

  private static final long serialVersionUID = -4342847923766383117L;

  @Column(name = "CMPY_CD", nullable = false, insertable = false, updatable = false)
  private String cmpyCd;

  @Column(name = "CD_GRUP", nullable = false)
  private String cdGrup;
}
