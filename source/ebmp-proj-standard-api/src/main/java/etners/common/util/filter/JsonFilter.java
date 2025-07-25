package etners.common.util.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import etners.common.util.annotation.response.Ce;
import etners.common.util.annotation.response.Cm;
import etners.common.util.annotation.response.Etners;
import etners.common.util.enumType.WebAccessType;
import etners.common.util.annotation.response.Mobile;
import etners.common.util.annotation.response.Web;
import etners.common.util.scope.CurrentUserData;


public class JsonFilter extends SimpleBeanPropertyFilter {

  private final CurrentUserData currentUserData;

  public JsonFilter(CurrentUserData currentUserData) {
    this.currentUserData = currentUserData;
  }

  @Override
  public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider prov,
      PropertyWriter writer) throws Exception {
    if (includeField(writer)) {
      writer.serializeAsField(pojo, jgen, prov);
    }
  }

  private boolean includeField(PropertyWriter writer) {
    WebAccessType webAccessType = currentUserData.getWebAccessType();
    boolean isEtners = currentUserData.isEtners();
    boolean isCm = currentUserData.isCm();

    if (WebAccessType.MOBILE.equals(webAccessType)) {
      if (isEtners) {
        return etnersCondition(writer) && mobileCondition(writer);
      } else {
        return isCm ? cmCondition(writer) && mobileCondition(writer) : ceCondition(writer) && mobileCondition(writer);
      }
    } else if (WebAccessType.WEB.equals(webAccessType)) {
      if (isEtners) {
        return etnersCondition(writer) && webCondition(writer);
      } else {
        return isCm ? cmCondition(writer) && webCondition(writer) : ceCondition(writer) && webCondition(writer);
      }
    }

    if (isEtners) {
      return etnersCondition(writer);
    } else {
      return isCm ? cmCondition(writer) : ceCondition(writer);
    }
  }

  private boolean mobileCondition(PropertyWriter writer) {
    return writer.getAnnotation(Mobile.class) != null || writer.getAnnotation(Web.class) == null;
  }

  private boolean webCondition(PropertyWriter writer) {
    return writer.getAnnotation(Mobile.class) == null || writer.getAnnotation(Web.class) != null;
  }

  private boolean etnersCondition(PropertyWriter writer) {
    return writer.getAnnotation(Etners.class) != null || (writer.getAnnotation(Ce.class) == null && writer.getAnnotation(Cm.class) == null);
  }

  private boolean ceCondition(PropertyWriter writer) {
    return writer.getAnnotation(Ce.class) != null || (writer.getAnnotation(Cm.class) == null && writer.getAnnotation(Etners.class) == null);
  }

  private boolean cmCondition(PropertyWriter writer) {
    return writer.getAnnotation(Cm.class) != null || (writer.getAnnotation(Ce.class) == null && writer.getAnnotation(Etners.class) == null);
  }
}
