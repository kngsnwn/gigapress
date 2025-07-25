package etners.common.util.validator;

import java.util.List;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class ListSizeValidator implements ConstraintValidator<ValidListSize, List<?>> {

  private int minSize;

  @Override
  public boolean isValid(List<?> itemList, ConstraintValidatorContext cxt) {
    if (ObjectUtils.isNotEmpty(itemList) && itemList.size() >= minSize) {
      return true;
    } else {
      cxt.disableDefaultConstraintViolation();
      cxt.buildConstraintViolationWithTemplate(
          String.format("리스트 갯수는 최소 %s 개 입니다.", minSize))
          .addConstraintViolation();
      return false;
    }
  }

  @Override
  public void initialize(ValidListSize constraintAnnotation) {
    this.minSize = constraintAnnotation.min();
  }
}

