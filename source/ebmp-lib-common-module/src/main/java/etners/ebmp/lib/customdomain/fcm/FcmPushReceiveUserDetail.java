package etners.ebmp.lib.customdomain.fcm;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FcmPushReceiveUserDetail implements Serializable {

  private static final long serialVersionUID = -6077062750266608198L;

  private String unqUserId;

  private String authCd;

  private String solCd;

  private String userId;

  private String mblFcmKey;

  private String mblOsType;

  private String mblOsVersion;

  private String mblInfo;

  private String mblUid;

  private String cmpyCd;

  private String cmpyNm;

  private String empNm;


}
