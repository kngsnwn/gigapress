package etners.common.config.aspect;

import etners.common.domains.MaskingParamVO;
import etners.common.util.annotation.masking.ApplyMasking;
import etners.common.util.annotation.masking.Mask;
import etners.common.util.enumType.MaskingType;
import etners.common.util.masking.MaskingUtil;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.kendomodel.KendoPagingResultModel;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Aspect
@EnableAspectJAutoProxy
@Component
@Slf4j
public class MaskingAspect {

  @Around("@annotation(applyMasking)")
  public Object applyMaskingAspect(ProceedingJoinPoint joinPoint, ApplyMasking applyMasking) throws Throwable {
    Object[] args = joinPoint.getArgs();
    Object result = joinPoint.proceed();

    if (!applyMasking.masking()) {
      return result;
    }

    if (ObjectUtils.isNotEmpty(args)) {
      MaskingParamVO maskingOn = (MaskingParamVO) Arrays.stream(args)
        .filter(arg -> arg instanceof MaskingParamVO)
        .findFirst()
        .orElse(null);
      if (ObjectUtils.isNotEmpty(maskingOn) && maskingOn.disableMasking()) {
        return result;
      }
    }

    return applyMaskingUtil(result);
  }

  private Object applyMaskingUtil(Object result) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    if (ObjectUtils.isEmpty(result)) {
      return result;
    }

    if (result instanceof KendoPagingResultModel<?> kendoPagingResultModel) {
      List<?> data = kendoPagingResultModel.getResultData();
      List<?> maskedData = applyMaskingUtilForList(data);
      return new KendoPagingResultModel<>(kendoPagingResultModel.getResultStatus(), maskedData, kendoPagingResultModel.getTotal());
    }

    if (result instanceof ResultModel<?> resultModel) {
      Object data = resultModel.getResultData();
      Object maskedData = applyMaskingForObject(data);
      return new ResultModel<>(resultModel.getResultStatus(), maskedData);
    }

    return applyMaskingForObject(result);
  }

  private Object applyMaskingForObject(Object result) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    if (result instanceof List<?>) {
      return applyMaskingUtilForList((List<?>) result);
    }

    if (result instanceof Map<?, ?>) {
      return applyMaskingUtilForMap((Map<?, ?>) result);
    }

    if (result instanceof Page<?>) {
      List<?> content = ((Page<?>) result).getContent();
      List<?> maskedContent = applyMaskingUtilForList(content);
      Pageable pageable = ((Page<?>) result).getPageable();
      long total = ((Page<?>) result).getTotalElements();
      return new PageImpl<>(maskedContent, pageable, total);
    }

    return applyMaskingUtilForDto(result);
  }


  private List<?> applyMaskingUtilForList(List<?> listObject) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    if (ObjectUtils.isNotEmpty(listObject)) {
      Class<?> clazz = listObject.get(0).getClass();
      List<Object> list = new ArrayList<>();
      for (Object object : listObject) {
        if (ObjectUtils.isNotEmpty(object) && object.getClass().equals(clazz)) {
          var responseDto = applyMaskingUtilForDto(object);
          list.add(responseDto);
        }
      }
      return list;
    }
    return listObject;
  }

  private Object applyMaskingUtilForMap(Map<?, ?> mapObject) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    if (ObjectUtils.isNotEmpty(mapObject)) {
      for (Entry<?, ?> entry : mapObject.entrySet()) {
        Object key = entry.getKey();
        Object value = entry.getValue();
        if (ObjectUtils.isNotEmpty(key)) {
          applyMaskingForObject(key);
        }
        if (ObjectUtils.isNotEmpty(value)) {
          applyMaskingForObject(value);
        }
      }
    }
    return mapObject;
  }

  private Object applyMaskingUtilForDto(Object response) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = response.getClass();
    Field[] fields = clazz.getDeclaredFields();
    Object responseDto = clazz.getDeclaredConstructor().newInstance();
    Arrays.stream(fields).forEach(
      field -> {
        field.setAccessible(true);
        try {
          Object fieldValue = field.get(response);
          convertField(responseDto, field, fieldValue);
        } catch (Exception e) {
          log.error("마스킹 적용 오류 발생!");
        }
      }
    );
    return responseDto;
  }

  private void convertField(Object responseDto, Field field, Object fieldValue) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
    if (ObjectUtils.isNotEmpty(fieldValue)) {
      if (fieldValue instanceof String) {
        // 기존 String 타입 처리 로직
        if (field.isAnnotationPresent(Mask.class) && field.getAnnotation(Mask.class).masking()) {
          Mask mask = field.getAnnotation(Mask.class);
          MaskingType maskingType = mask.type();
          String maskedValue = MaskingUtil.MaskingOf(maskingType, (String) fieldValue);
          field.set(responseDto, maskedValue);
        } else {
          field.set(responseDto, fieldValue);
        }
      } else if (fieldValue instanceof List<?>) {
        // List 타입 처리
        List<?> maskedList = applyMaskingUtilForList((List<?>) fieldValue);
        field.set(responseDto, maskedList);
      } else if (fieldValue instanceof Map<?, ?>) {
        // Map 타입 처리
        Map<?, ?> maskedMap = (Map<?, ?>) applyMaskingUtilForMap((Map<?, ?>) fieldValue);
        field.set(responseDto, maskedMap);
      } else if (fieldValue.getClass().getPackage() != null &&
        !fieldValue.getClass().getPackage().getName().startsWith("java.") &&
        !fieldValue.getClass().isPrimitive() && !fieldValue.getClass().isEnum() && (fieldValue.getClass().isAnnotationPresent(Mask.class) || hasMaskAnnotationInFields(fieldValue.getClass()))) {
        // 사용자 정의 객체(DTO) 처리
        Object maskedDto = applyMaskingUtilForDto(fieldValue);
        field.set(responseDto, maskedDto);
      } else {
        // 기본 타입이나 다른 Java 내장 객체는 그대로 설정
        field.set(responseDto, fieldValue);
      }
    } else {
      field.set(responseDto, fieldValue);
    }
  }

  private boolean hasMaskAnnotationInFields(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (field.isAnnotationPresent(Mask.class)) {
        return true;
      }
    }
    return false;
  }
}