package etners.standard.mvc.jpa.epcScodeMstTest.repo;

import etners.common.util.enumType.SolutionType;
import etners.standard.mvc.jpa.epcScodeMstTest.entity.EpcScodeMstTest;
import etners.standard.mvc.jpa.epcScodeMstTest.entity.EpcScodeMstTestPK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EpcScodeMstRepository extends JpaRepository<EpcScodeMstTest, EpcScodeMstTestPK>{

  Optional<EpcScodeMstTest> findBySolCdAndCdGrupAndUseYn(SolutionType solutionType, String cdGrup, boolean useYn);

  Optional<EpcScodeMstTest> findBySolCdAndCdGrup(SolutionType solutionType, String cdGrup);
}



