package common.util.kendo;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import common.util.convert.ObjectUtil;
import common.util.string.StringUtil;
import etners.ebmp.lib.api.kendomodel.KendoPagingParamVO;
import etners.ebmp.lib.api.kendomodel.KendoSort;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class KendoUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(KendoUtil.class);

  public static Pageable toSpringPageable(KendoPagingParamVO kendoPagingParam, List<String> defaultSortingColumnName) {
    PageRequest pageRequest = null;

    int pageNo = kendoPagingParam.getPage() - 1;
    int pageSize = kendoPagingParam.getPageSize();

    KendoSort[] kendoSortArray = kendoPagingParam.getSort();

    if (pageNo >= 0 && pageSize > 0) {
      //현재 Multiple Order By 적용이 어렵다고 판단하여 일단 1단계 Order By만 적용함.
      if (kendoSortArray != null && kendoSortArray.length > 0) {
        KendoSort kendoSort = kendoSortArray[0];
        Sort sort = null;

        Direction direction = null;

        switch (kendoSort.getDir()) {
          case "asc":
            direction = Direction.ASC;
            break;
          case "desc":
          default:
            direction = Direction.DESC;
            break;
        }

        String fieldName = kendoSort.getField();

        if (fieldName.startsWith("cvt") || fieldName.startsWith("fmt")) {
          fieldName = fieldName.substring(3);
          fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        }

        if (sort == null) {
          sort = Sort.by(direction, fieldName);
        } else {
          sort = sort.and(Sort.by(direction, fieldName));
        }

        pageRequest = PageRequest.of(pageNo, pageSize, sort);
      } else if (defaultSortingColumnName != null && !defaultSortingColumnName.isEmpty()) {
        pageRequest = PageRequest.of(pageNo, pageSize,
          Sort.by(Direction.DESC, String.valueOf(defaultSortingColumnName)));
      } else {
        pageRequest = PageRequest.of(pageNo, pageSize);
      }
    } else {
      pageRequest = PageRequest.of(0, 10);
    }

    return pageRequest;
  }

  public static Pageable toSpringPageable(KendoPagingParamVO kendoPagingParam) {

    return toSpringPageable(kendoPagingParam, null);
  }

  public static <Q extends EntityPathBase> List<OrderSpecifier<? extends Comparable>> getQuerydslOrderSpecifierList(KendoPagingParamVO kendoPagingParam, Q qClass) {
    KendoSort[] kendoSortArray = kendoPagingParam.getSort();

    if (kendoSortArray == null || kendoSortArray.length == 0) {
      return null;
    }

    HashMap<String, Field> classFieldMap = ObjectUtil.getClassFieldMap(qClass.getClass());

    List<OrderSpecifier<? extends Comparable>> orderList = new ArrayList<>();

    for (KendoSort kendoSort : kendoSortArray) {
      String fieldName = kendoSort.getField();
      String direction = kendoSort.getDir();

      if (fieldName.startsWith("cvt") || fieldName.startsWith("fmt")) {
        fieldName = fieldName.substring(3);
        fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
      }

      OrderSpecifier<? extends Comparable> order = getOrderSpecfier(qClass, classFieldMap, fieldName, direction);

      orderList.add(order);
    }

    return orderList;
  }

  public static <Q extends EntityPathBase> OrderSpecifier[] getQuerydslMultiOrderSpecifierList(
    KendoPagingParamVO kendoPagingParam, Q... qClassArray) {
    KendoSort[] kendoSortArray = kendoPagingParam.getSort();

    if (kendoSortArray == null || kendoSortArray.length == 0) {
      return new OrderSpecifier[0];
    }

    List<OrderSpecifier<? extends Comparable>> orderList = new ArrayList<>();

    for (Q qClass : qClassArray) {

      HashMap<String, Field> classFieldMap = ObjectUtil.getClassFieldMap(qClass.getClass());

      for (KendoSort kendoSort : kendoSortArray) {
        String fieldName = kendoSort.getField();
        String direction = kendoSort.getDir();

        if (fieldName.startsWith("cvt") || fieldName.startsWith("fmt")) {
          fieldName = fieldName.substring(3);
          fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        }

        OrderSpecifier<? extends Comparable> order = getOrderSpecfier(qClass, classFieldMap,
          fieldName, direction);

        if (order != null) {
          kendoSortArray = ArrayUtils.removeElement(kendoSortArray, kendoSort);
          orderList.add(order);
        }
      }
    }

    for (KendoSort kendoSort : kendoSortArray) {
      String fieldName = kendoSort.getField();
      String direction = kendoSort.getDir();
      OrderSpecifier<? extends Comparable> order = getCutomOrderSpecfier(fieldName, direction);
      orderList.add(order);
    }

    return orderList.stream().filter(Objects::nonNull).toArray(OrderSpecifier[]::new);
  }

  /**
   * 설명	: Querydsl용으로 동적으로 orderBy에 넣을 인자값을 생성하도록 만든 메서드. 여러개의 Entity를 조인해서 동적으로 정렬할 때 사용.
   */
  public static <Q extends EntityPathBase> OrderSpecifier<? extends Comparable> getQuerydslOrderSpecifier(KendoPagingParamVO kendoPagingParam, Q... qClassArray) {
    OrderSpecifier<? extends Comparable> order = null;

    for (Q qClass : qClassArray) {
      order = getQuerydslOrderSpecifier(kendoPagingParam, qClass);

      if (order != null) {
        break;
      }
    }

    return order;
  }

  /**
   * 설명	: Querydsl용으로 동적으로 orderBy에 넣을 인자값을 생성하도록 만든 메서드. 하나의 정렬 값만을 리턴 가능하도록 구현함.
   */
  public static <Q extends EntityPathBase> OrderSpecifier<? extends Comparable> getQuerydslOrderSpecifier(KendoPagingParamVO kendoPagingParam, Q qClass) {
    KendoSort[] kendoSortArray = kendoPagingParam.getSort();

    if (kendoSortArray == null || kendoSortArray.length == 0) {
      return null;
    }

    KendoSort kendoSort = kendoSortArray[0];

    HashMap<String, Field> classFieldMap = ObjectUtil.getClassFieldMap(qClass.getClass());

    String fieldName = kendoSort.getField();
    String direction = kendoSort.getDir();

    if (fieldName.startsWith("cvt") || fieldName.startsWith("fmt")) {
      fieldName = fieldName.substring(3);
      fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

    return getOrderSpecfier(qClass, classFieldMap, fieldName, direction);
  }

  private static <Q extends EntityPathBase> OrderSpecifier<? extends Comparable> getOrderSpecfier(Q qClass, HashMap<String, Field> classFieldMap, String fieldName, String direction) {
    OrderSpecifier<? extends Comparable> order = null;

    try {
      if (classFieldMap.containsKey(fieldName)) {
        Field field = classFieldMap.get(fieldName);
        field.setAccessible(true);

        Object value = field.get(qClass);

        if (value instanceof StringPath currentStringPath) {

          if ("desc".equals(direction)) {
            order = currentStringPath.desc();
          } else if ("asc".equals(direction)) {
            order = currentStringPath.asc();
          }

        } else if (value instanceof DateTimePath currentDateTimePath) {

          if ("desc".equals(direction)) {
            order = currentDateTimePath.desc();
          } else if ("asc".equals(direction)) {
            order = currentDateTimePath.asc();
          }

        } else if (value instanceof NumberPath currentNumberPath) {

          if ("desc".equals(direction)) {
            order = currentNumberPath.desc();
          } else if ("asc".equals(direction)) {
            order = currentNumberPath.asc();
          }
        } else {
          throw new IllegalArgumentException("정의되지 않은 타입입니다. 이윤성D에게 확인해주세요.");
        }
      }

    } catch (IllegalArgumentException | IllegalAccessException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }

    return order;
  }

  private static OrderSpecifier<? extends Comparable> getCutomOrderSpecfier(String fieldName,
    String direction) {
    OrderSpecifier<? extends Comparable> order = null;

    try {

      StringPath currentStringPath = Expressions.stringPath(fieldName);

      if ("desc".equals(direction)) {
        order = currentStringPath.desc().nullsLast();
      } else if ("asc".equals(direction)) {
        order = currentStringPath.asc();
      }

    } catch (IllegalArgumentException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }

    return order;
  }

  public static OrderSpecifier<?> getSortedColumn(String dir, Path<?> parent, String fieldName) {
    Order direction = Order.ASC;

    if (dir == null || fieldName == null) {
      return null;
    }

    if (dir.equals("desc")) {
      direction = Order.DESC;
    }

    Path<Object> fieldPath = Expressions.path(Object.class, parent, fieldName);
    return new OrderSpecifier(direction, fieldPath);
  }

}
