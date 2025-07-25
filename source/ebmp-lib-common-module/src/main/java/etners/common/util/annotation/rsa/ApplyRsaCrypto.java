package etners.common.util.annotation.rsa;

import etners.common.util.enumType.CryptoMode;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplyRsaCrypto {

  @NotNull CryptoMode mode();
}
