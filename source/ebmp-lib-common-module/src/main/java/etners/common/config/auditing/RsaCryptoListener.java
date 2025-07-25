package etners.common.config.auditing;


import etners.common.config.context.SpringContext;
import etners.common.util.rsa.AbstractRsaUtil;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class RsaCryptoListener {

  private AbstractRsaUtil rsaUtil;

  public RsaCryptoListener() {
  }

  @PrePersist
  @PreUpdate
  public void encrypt(Object entity) throws Exception {
    if (rsaUtil == null) {
      rsaUtil = SpringContext.getBean(AbstractRsaUtil.class);
    }
    rsaUtil.encryptFields(entity);
  }

  @PostLoad
  public void decrypt(Object entity) {
    if (rsaUtil == null) {
      rsaUtil = SpringContext.getBean(AbstractRsaUtil.class);
    }
    rsaUtil.decryptFields(entity);
  }
}
