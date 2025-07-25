package etners.standard.mvc.jpa.epcScodeMstTest.mapper;

import etners.common.util.enumType.SolutionType;
import etners.common.util.mapper.GenericMapper;
import etners.standard.mvc.jpa.epcScodeMstTest.entity.EpcScodeMstTest;
import etners.standard.mvc.test.domain.response.TestDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EpcScodeMstTestMapper extends GenericMapper<TestDetailResponse, EpcScodeMstTest> {

  EpcScodeMstTestMapper INSTANCE = Mappers.getMapper(EpcScodeMstTestMapper.class);

  default String map(SolutionType solutionType) {
    return solutionType.getCode();
  }
}
