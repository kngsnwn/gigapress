package etners.ebmp.lib.jpa.customdomain.fcm;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : FcmPushReceiveUserDetail.java
 * @Description :
 * @Modification Information
 * @
 * @ 수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2019. 9. 10.  oxide     최초생성
 * @see Copyright (C) by etners All Rights Reserved.
 * @since 2019. 9. 10.
 */
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
