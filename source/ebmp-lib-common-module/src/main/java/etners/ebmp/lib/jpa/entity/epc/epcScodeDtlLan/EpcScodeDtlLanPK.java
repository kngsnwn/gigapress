package etners.ebmp.lib.jpa.entity.epc.epcScodeDtlLan;

import jakarta.persistence.Column;
import java.io.Serializable;
import lombok.Data;

@Data
public class EpcScodeDtlLanPK implements Serializable {

  private static final long serialVersionUID = -9010294650169682282L;

  @Column(name = "SOL_CD")
  private String solCd;

  @Column(name = "CD_GRUP")
  private String cdGrup;

  @Column(name = "CD_ID")
  private String cdId;
}
