package common.util.convert;

import common.util.string.StringUtil;
import common.util.support.CustomAction;
import common.util.support.MapKeyGenerationExpression;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ObjectUtil.class);

  public static Map<String, Object> convertObjectToMap(Object obj) {
    Map<String, Object> map = new HashMap<String, Object>();
    Field[] fields = obj.getClass().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      fields[i].setAccessible(true);
      try {
        map.put(fields[i].getName(), fields[i].get(obj));
      } catch (Exception e) {
        LOGGER.error(StringUtil.extractStackTrace(e));
      }
    }
    return map;
  }

  public static <O, T> List<T> toListCopyAvailableFieldForTargetClass(List<O> originalClassList, Class<T> targetClass) {
    List<T> targetClassList = new ArrayList<>();

    originalClassList.forEach(originalInstance -> {
      T targetInstance = copyAvailableFieldForTargetClass(originalInstance, targetClass);

      targetClassList.add(targetInstance);
    });

    return targetClassList;
  }

  /**
   * <pre>
   * 설명	: 원본 인스턴스의 필드값을 대상 클래스의 필드값으로 복사하는 메서드.
   *
   *        ******************************************************************************************
   *        * 이 메서드의 주의할 점은 이 메서드의 경우 타겟 클래스에 해당하는 신규 인스턴스를 reflection API를 활용해 생성한다는 점이다.
   *        * '클래스'와 '인스턴스'는 서로 다른 개념이라는 점에 유의.
   *        ******************************************************************************************
   *
   *        원본 인스턴스 getter 메서드와 대상 클래스의 setter 메서드가 get/set을 제외한 나머지 네이밍이 일치해야하고
   *        getter의 리턴 타입과 setter의 인자 타입이 일치할 경우
   *        대상 클래스의 setter 메서드를 통해 대상 클래스의 필드값에 값을 주입하여 리턴한다.
   * </pre>
   *
   * @param originalInstance 기존에 데이터를 가지고 있는 오리지널 인스턴스.
   * @param targetClass      Reflection API를 통해 새로운 인스턴스를 생성하고자 하는 대상 클래스.
   * @return
   */
  public static <O, T> T copyAvailableFieldForTargetClass(O originalInstance, Class<T> targetClass) {
    Method[] originalInstanceMethodArray = originalInstance.getClass().getDeclaredMethods();

    T targetInstance = null;

    try {
      targetInstance = targetClass.newInstance(); //new 키워드를 통해 생성하는 것과 동일.
    } catch (InstantiationException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    } catch (IllegalAccessException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }

    Method[] targetClassMethodArray = targetInstance.getClass().getDeclaredMethods();

    for (Method originalInstanceMethod : originalInstanceMethodArray) {
      String originalClassMethodName = originalInstanceMethod.getName();

      if (originalClassMethodName.startsWith("get")) { //getter인 경우에만

        for (Method targetClassMethod : targetClassMethodArray) {
          String targetClassMethodName = targetClassMethod.getName();

          //setter 형태로 네이밍을 만들어 일치 여부 확인
          String matcheMethodName = "set" + originalClassMethodName.substring(3, 4).toUpperCase() + originalClassMethodName.substring(4);

          if (targetClassMethodName.equals(matcheMethodName)) {

            try {
              originalInstanceMethod.setAccessible(true);

              Object value = originalInstanceMethod.invoke(originalInstance);

              if (value != null) {
                targetClassMethod.setAccessible(true);

                //setter에 값을 넣도록 처리.
                if (value instanceof Byte) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Short) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Integer) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Long) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Float) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Double) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Character) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Boolean) {
                  targetClassMethod.invoke(targetInstance, value);
                } else {
                  targetClassMethod.invoke(targetInstance, value);
                }
              }

            } catch (IllegalAccessException e) {
              LOGGER.error(StringUtil.extractStackTrace(e));
            } catch (IllegalArgumentException e) {
              LOGGER.error("[ERROR targetClassMethodName: " + targetClassMethodName + "]");
              LOGGER.error(StringUtil.extractStackTrace(e));
            } catch (InvocationTargetException e) {
              LOGGER.error(StringUtil.extractStackTrace(e));
            }
          }
        }
      }

    }

    return targetInstance;
  }

  /**
   * <pre>
   * 메소드명	: copyAvailableFieldForTargetInstance
   * 작성자	: oxide
   * 설명	: 2019. 3. 1.
   * 설명   : 원본 인스턴스의 필드값을 대상 인스턴스의 필드값으로 복사하는 메서드.
   *
   *       원본 인스턴스 getter 메서드와 대상 인스턴스의 setter 메서드가 get/set을 제외한 나머지 네이밍이 일치해야하고
   *       getter의 리턴 타입과 setter의 인자 타입이 일치할 경우
   *       대상 인스턴스의 setter 메서드를 통해 대상 인스턴스의 필드값에 값을 주입하여 리턴한다.
   * </pre>
   *
   * @param originalInstance 기존에 데이터를 가지고 있는 오리지널 인스턴스.
   * @param targetInstance   이미 new 키워드를 통해 생성된 대상 인스턴스.
   * @return
   */
  public static <O, T> T copyAvailableFieldForTargetInstance(O originalInstance, T targetInstance) {
    Method[] originalInstanceMethodArray = originalInstance.getClass().getDeclaredMethods();

    Method[] targetClassMethodArray = targetInstance.getClass().getDeclaredMethods();

    for (Method originalInstanceMethod : originalInstanceMethodArray) {
      String originalClassMethodName = originalInstanceMethod.getName();

      if (originalClassMethodName.startsWith("get")) { //getter인 경우에만

        for (Method targetClassMethod : targetClassMethodArray) {
          String targetClassMethodName = targetClassMethod.getName();

          //setter 형태로 네이밍을 만들어 일치 여부 확인
          String matcheMethodName = "set" + originalClassMethodName.substring(3, 4).toUpperCase() + originalClassMethodName.substring(4);

          if (targetClassMethodName.equals(matcheMethodName)) {

            try {
              originalInstanceMethod.setAccessible(true);

              Object value = originalInstanceMethod.invoke(originalInstance);

              if (value != null) {
                targetClassMethod.setAccessible(true);

                //setter에 값을 넣도록 처리.
                if (value instanceof Byte) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Short) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Integer) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Long) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Float) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Double) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Character) {
                  targetClassMethod.invoke(targetInstance, value);
                } else if (value instanceof Boolean) {
                  targetClassMethod.invoke(targetInstance, value);
                } else {
                  targetClassMethod.invoke(targetInstance, value);
                }
              }

            } catch (IllegalAccessException e) {
              LOGGER.error(StringUtil.extractStackTrace(e));
            } catch (IllegalArgumentException e) {
              LOGGER.error("[ERROR targetClassMethodName: " + targetClassMethodName + "]");
              LOGGER.error(StringUtil.extractStackTrace(e));
            } catch (InvocationTargetException e) {
              LOGGER.error(StringUtil.extractStackTrace(e));
            }
          }
        }
      }

    }

    return targetInstance;
  }

  public static <T> T[] joinArrayGeneric(T[]... arrays) {
    int length = 0;
    for (T[] array : arrays) {
      length += array.length;
    }

    //T[] result = new T[length];
    final T[] result = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), length);

    int offset = 0;
    for (T[] array : arrays) {
      System.arraycopy(array, 0, result, offset, array.length);
      offset += array.length;
    }

    return result;
  }

  /**
   * <pre>
   * 메소드명	: convertSetToHashMap
   * 작성자	: oxide
   * 작성일	: 2019. 3. 2.
   * 설명	: Set을 HashMap으로 변환해주는 메서드.
   *
   *        MapKeyGenerationExpression 인터페이스를 통해서 사용할 Key값을 정의할 수 있도록 했다.
   *        단순하게 특정 필드값이 키 값이 될 수도 있지만, 조합해서 키값을 만들 경우도 고려한 선택이다.
   *
   *
   * </pre>
   *
   * @param solutions
   * @param expression
   * @return
   */
  public static <K, V> HashMap<K, V> convertSetToHashMap(Set<V> solutions, MapKeyGenerationExpression<K, V> expression) {
    HashMap<K, V> hashMap = new HashMap<>();

    solutions.forEach(valueObject ->
      hashMap.put(expression.generateKey(valueObject), valueObject)
    );

    return hashMap;
  }

  @SuppressWarnings("rawtypes")
  public static HashMap<String, Field> getClassFieldMap(Class clazz) {
    Field[] classFields = clazz.getFields();

    HashMap<String, Field> classFieldMap = new HashMap<>();

    for (Field classField : classFields) {
      classFieldMap.put(classField.getName(), classField);
    }

    return classFieldMap;
  }

  public static <T, O> T afterProcessing(T targetObject, CustomAction<T> customAction) {
    return customAction.processing(targetObject);
  }


}
