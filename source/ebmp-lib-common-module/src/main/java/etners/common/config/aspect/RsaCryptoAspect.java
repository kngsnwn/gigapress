package etners.common.config.aspect;

import common.util.string.StringUtil;
import etners.common.domains.rsa.RsaCryptoVO;
import etners.common.util.annotation.rsa.ApplyRsaCrypto;
import etners.common.util.enumType.CryptoMode;
import etners.common.util.object.ObjectCommonUtil;
import etners.common.util.rsa.AbstractRsaUtil;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.kendomodel.KendoPagingResultModel;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Slf4j
public class RsaCryptoAspect {

  private final Optional<AbstractRsaUtil> optionalRsaUtil;

  @Around("@annotation(applyRsaCrypto)")
  public Object applyRsaCryptoAspect(ProceedingJoinPoint joinPoint, ApplyRsaCrypto applyRsaCrypto) throws Throwable {
    Object result = joinPoint.proceed();
    if (ObjectUtils.isEmpty(applyRsaCrypto.mode())) {
      return result;
    }

    if (ObjectUtils.isNotEmpty(result) && result instanceof RsaCryptoVO cryptoOn) {
      if (ObjectUtils.isNotEmpty(cryptoOn) && (ObjectUtils.isEmpty(cryptoOn.getKeyPublic()) && ObjectUtils.isEmpty(cryptoOn.getPrivateKey()))) {
        return result;
      }
    }
    return applyRsaCryptoByMode(result, applyRsaCrypto.mode());
  }

  private Object applyRsaCryptoByMode(Object result, CryptoMode mode) {
    if (ObjectUtils.isEmpty(result)) {
      return result;
    }

    if (result instanceof KendoPagingResultModel<?> kendoPagingResultModel) {
      List<?> data = kendoPagingResultModel.getResultData();
      List<?> formattedData = applyRsaUtilForList(data, mode);
      return new KendoPagingResultModel<>(kendoPagingResultModel.getResultStatus(), formattedData, kendoPagingResultModel.getTotal());
    }

    if (result instanceof ResultModel<?> resultModel) {
      Object data = resultModel.getResultData();
      Object formattedData = applyRsaUtilForObject(data, mode);
      return new ResultModel<>(resultModel.getResultStatus(), formattedData);
    }

    if (result instanceof Page<?> page) {
      List<?> data = page.getContent();
      List<?> formattedData = applyRsaUtilForList(data, mode);
      return new PageImpl<>(formattedData, page.getPageable(), page.getTotalElements());
    }

    return applyRsaUtilForDto(result, mode);
  }

  private Object applyRsaUtilForDto(Object response, CryptoMode mode) {
    try {
      if (ObjectUtils.isNotEmpty(response)) {
        if (response instanceof RsaCryptoVO) {
          // RsaCryptoVO인 경우: 직접 암복호화 처리
          log.debug("Processing RsaCryptoVO: {}", response.getClass().getSimpleName());
          convertField(response, mode);
        } else {
          // RsaCryptoVO가 아닌 경우: 내부 필드들을 탐색하여 처리
          log.debug("Processing non-RsaCryptoVO object: {}", response.getClass().getSimpleName());
          processNonCryptoVOFields(response, mode);
        }
      }
    } catch (Exception e) {
      log.error("암/복호화 오류 발생! : {}", StringUtil.extractStackTrace(e));
    }
    return response;
  }

  /**
   * RsaCryptoVO가 아닌 객체의 내부 필드들을 탐색하여 처리
   */
  private void processNonCryptoVOFields(Object object, CryptoMode mode) {
    if (ObjectUtils.isEmpty(object)) {
      return;
    }
    // java 기본 클래스 중 list, set, map, array등일 경우 내부 탐색
    if (ObjectCommonUtil.isJavaBasicType(object.getClass())) {
      processJavaBasicType(object, mode);
    }

    log.debug("Scanning fields in: {}", object.getClass().getSimpleName());

    // 모든 필드 탐색
    List<Field> allFields = ObjectCommonUtil.getAllFields(object.getClass());

    for (Field field : allFields) {
      try {
        field.setAccessible(true);
        Object fieldValue = field.get(object);

        if (fieldValue != null) {
          // 필드 값의 타입에 따라 처리
          processFieldValue(fieldValue, mode, field.getName());
        }

      } catch (IllegalAccessException e) {
        log.warn("필드 접근 실패: {}.{} - {}",
          object.getClass().getSimpleName(), field.getName(), e.getMessage());
      }
    }
  }

  private void processJavaBasicType(Object object, CryptoMode mode) {
    log.debug("Java 기본 제공 객체 처리");
    if (object instanceof List<?> listObject) {
      processListField(listObject, mode);
    } else if (object instanceof Set<?> setObject) {
      processSetField(setObject, mode);
    } else if (object instanceof Map<?, ?> mapObject) {
      processMapField(mapObject, mode);
    } else if (object.getClass().isArray()) {
      processArrayField(object, mode);
    } else {
    }
  }

  /**
   * 필드 값의 타입에 따른 재귀적 처리
   */
  private void processFieldValue(Object fieldValue, CryptoMode mode, String fieldName) {
    log.debug("Processing field '{}' of type: {}", fieldName, fieldValue.getClass().getSimpleName());

    if (fieldValue instanceof List<?> listValue) {
      // List 처리
      processListField(listValue, mode);
    } else if (fieldValue instanceof Set<?> setValue) {
      // Set 처리
      processSetField(setValue, mode);
    } else if (fieldValue instanceof Map<?, ?> mapValue) {
      // Map 처리
      processMapField(mapValue, mode);
    } else if (fieldValue.getClass().isArray()) {
      // Array 처리
      processArrayField(fieldValue, mode);
    } else if (fieldValue instanceof RsaCryptoVO) {
      // RsaCryptoVO 발견: 직접 처리
      log.debug("Found RsaCryptoVO in field '{}': {}", fieldName, fieldValue.getClass().getSimpleName());
      convertField(fieldValue, mode);
    } else if (!ObjectCommonUtil.isJavaBasicType(fieldValue.getClass())) {
      // 일반 커스텀 객체: 재귀적으로 내부 필드 탐색
      log.debug("Recursively processing custom object in field '{}': {}", fieldName, fieldValue.getClass().getSimpleName());
      processNonCryptoVOFields(fieldValue, mode);
    }
    // Java 기본 타입은 무시
  }

  /**
   * List 필드 내부 요소들 처리
   */
  private void processListField(List<?> listValue, CryptoMode mode) {
    if (ObjectUtils.isEmpty(listValue)) {
      return;
    }

    log.debug("Processing List with {} elements", listValue.size());

    for (int i = 0; i < listValue.size(); i++) {
      Object item = listValue.get(i);
      if (item != null) {
        processFieldValue(item, mode, "List[" + i + "]");
      }
    }
  }

  /**
   * Set 필드 내부 요소들 처리
   */
  private void processSetField(Set<?> setValue, CryptoMode mode) {
    if (ObjectUtils.isEmpty(setValue)) {
      return;
    }

    log.debug("Processing Set with {} elements", setValue.size());

    int index = 0;
    for (Object item : setValue) {
      if (item != null) {
        processFieldValue(item, mode, "Set[" + (index++) + "]");
      }
    }
  }

  /**
   * Map 필드 내부 키와 값들 처리
   */
  private void processMapField(Map<?, ?> mapValue, CryptoMode mode) {
    if (ObjectUtils.isEmpty(mapValue)) {
      return;
    }

    log.debug("Processing Map with {} entries", mapValue.size());

    for (Entry<?, ?> entry : mapValue.entrySet()) {
      Object key = entry.getKey();
      Object value = entry.getValue();

      // Map의 key 처리
      if (key != null && !ObjectCommonUtil.isJavaBasicType(key.getClass())) {
        processFieldValue(key, mode, "Map.key");
      }

      // Map의 value 처리
      if (value != null) {
        processFieldValue(value, mode, "Map.value[" + key + "]");
      }
    }
  }

  /**
   * Array 필드 내부 요소들 처리
   */
  private void processArrayField(Object arrayValue, CryptoMode mode) {
    if (arrayValue == null) {
      return;
    }

    int length = Array.getLength(arrayValue);
    log.debug("Processing Array with {} elements", length);

    for (int i = 0; i < length; i++) {
      Object item = Array.get(arrayValue, i);
      if (item != null) {
        processFieldValue(item, mode, "Array[" + i + "]");
      }
    }
  }

  private void convertField(Object responseDto, CryptoMode mode) {
    if (optionalRsaUtil.isPresent()) {
      AbstractRsaUtil rsaUtil = optionalRsaUtil.get();
      switch (mode) {
        case DECRYPT -> rsaUtil.decryptObjectRecursively(responseDto);
        case ENCRYPT -> rsaUtil.encryptObjectRecursively(responseDto);
        default -> throw new RuntimeException("지원되지 않는 타입입니다.");
      }
    }
  }

  private Object applyRsaUtilForObject(Object result, CryptoMode mode) {
    if (result instanceof List<?>) {
      return applyRsaUtilForList((List<?>) result, mode);
    }

    if (result instanceof Map<?, ?>) {
      return applyRsaUtilForMap((Map<?, ?>) result, mode);
    }

    if (result instanceof Page<?>) {
      List<?> content = ((Page<?>) result).getContent();
      List<?> formattedContent = applyRsaUtilForList(content, mode);
      Pageable pageable = ((Page<?>) result).getPageable();
      long total = ((Page<?>) result).getTotalElements();
      return new PageImpl<>(formattedContent, pageable, total);
    }

    return applyRsaUtilForDto(result, mode);
  }

  private Object applyRsaUtilForMap(Map<?, ?> mapObject, CryptoMode mode) {
    if (ObjectUtils.isNotEmpty(mapObject)) {
      for (Entry<?, ?> entry : mapObject.entrySet()) {
        Object key = entry.getKey();
        Object value = entry.getValue();
        if (ObjectUtils.isNotEmpty(key)) {
          applyRsaUtilForObject(key, mode);
        }
        if (ObjectUtils.isNotEmpty(value)) {
          applyRsaUtilForObject(value, mode);
        }
      }
    }
    return mapObject;
  }

  private List<?> applyRsaUtilForList(List<?> listObject, CryptoMode mode) {
    if (ObjectUtils.isNotEmpty(listObject)) {
      Class<?> clazz = listObject.get(0).getClass();
      List<Object> list = new ArrayList<>();
      for (Object object : listObject) {
        if (ObjectUtils.isNotEmpty(object) && object.getClass().equals(clazz)) {
          var responseDto = applyRsaUtilForDto(object, mode);
          list.add(responseDto);
        }
      }
      return list;
    }
    return listObject;
  }
}
