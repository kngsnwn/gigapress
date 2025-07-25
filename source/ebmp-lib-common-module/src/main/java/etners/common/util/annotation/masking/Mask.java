package etners.common.util.annotation.masking;

import etners.common.util.enumType.MaskingType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mask {

  MaskingType type();

  boolean masking() default true;
}
