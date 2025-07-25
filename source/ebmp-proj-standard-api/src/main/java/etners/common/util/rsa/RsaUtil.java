package etners.common.util.rsa;

import java.security.PrivateKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RsaUtil extends AbstractRsaUtil{

  @Override
  public PrivateKey fetchPrivateKey(String publicKey) {
    return null;
  }
}
