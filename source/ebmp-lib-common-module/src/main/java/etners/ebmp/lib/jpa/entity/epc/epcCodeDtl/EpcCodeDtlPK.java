package etners.ebmp.lib.jpa.entity.epc.epcCodeDtl;

import jakarta.persistence.Column;
import java.io.Serializable;
import lombok.Data;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : EpcCodeDtl.java
 * @Description :
 * @Modification Information
 * @
 * @ 수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2020. 8. 3  oxide     최초생성
 * @see
 * @since 2020. 8. 3
 */
@Data
public class EpcCodeDtlPK implements Serializable {

  private static final long serialVersionUID = -2940915079091141177L;

  @Column(name = "CMPY_CD")
  private String cmpyCd;

  @Column(name = "CD_GRUP")
  private String cdGrup;

  @Column(name = "CD_ID")
  private String cdId;

}
