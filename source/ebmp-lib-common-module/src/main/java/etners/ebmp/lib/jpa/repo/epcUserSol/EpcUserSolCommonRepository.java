package etners.ebmp.lib.jpa.repo.epcUserSol;

import etners.ebmp.lib.jpa.entity.epc.epcUserSol.EpcUserSolCommon;
import etners.ebmp.lib.jpa.entity.epc.epcUserSol.EpcUserSolCommonPK;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EpcUserSolCommonRepository extends JpaRepository<EpcUserSolCommon, EpcUserSolCommonPK> {

  List<EpcUserSolCommon> findByUnqUserId(String unqUserId);

  EpcUserSolCommon findByUnqUserIdAndSolCdAndWmGbnAndUseYn(String unqUserId, String solCd, String wmGbn, boolean useYn);
}
