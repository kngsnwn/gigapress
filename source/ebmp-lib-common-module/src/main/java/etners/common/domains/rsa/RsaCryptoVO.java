package etners.common.domains.rsa;


import com.fasterxml.jackson.annotation.JsonIgnore;
import etners.common.util.annotation.rsa.RsaKey;
import etners.common.util.enumType.KeyType;
import java.security.PrivateKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RsaCryptoVO {

  @JsonIgnore
  @RsaKey(type = KeyType.PUBLIC)
  private String keyPublic;

  @JsonIgnore
  @RsaKey(type = KeyType.PRIVATE)
  private PrivateKey privateKey;
}
