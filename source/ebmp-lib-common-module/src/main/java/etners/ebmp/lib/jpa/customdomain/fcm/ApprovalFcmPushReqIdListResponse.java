package etners.ebmp.lib.jpa.customdomain.fcm;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApprovalFcmPushReqIdListResponse implements Serializable {

  private static final long serialVersionUID = -2844775414963055348L;

  private String reqId;
}
