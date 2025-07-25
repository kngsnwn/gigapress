package etners.common.util.annotation.security;

import etners.common.domains.auth.EpcAuth;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Permission {

  //해당하는 권한 배열
  EpcAuth[] authGroups() default {};
}
