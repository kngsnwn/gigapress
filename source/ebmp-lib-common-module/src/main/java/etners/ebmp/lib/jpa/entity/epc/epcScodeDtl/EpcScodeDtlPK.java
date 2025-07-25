package etners.ebmp.lib.jpa.entity.epc.epcScodeDtl;

import java.io.Serializable;
import lombok.Data;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : EpcScodeDtl.java
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
public class EpcScodeDtlPK implements Serializable {

  private static final long serialVersionUID = 575749605929479111L;

  private String solCd;

  private String cdGrup;

  private String cdId;

}
