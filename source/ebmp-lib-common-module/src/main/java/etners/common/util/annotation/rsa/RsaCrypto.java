package etners.common.util.annotation.rsa;

import etners.common.util.enumType.CryptoMode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface RsaCrypto {

  CryptoMode mode() default CryptoMode.ALL;
}
