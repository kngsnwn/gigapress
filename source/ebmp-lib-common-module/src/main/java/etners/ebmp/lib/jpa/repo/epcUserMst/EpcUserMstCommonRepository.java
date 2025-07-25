package etners.ebmp.lib.jpa.repo.epcUserMst;

import etners.ebmp.lib.jpa.entity.epc.epecUserMst.EpcUserMstCommon;
import etners.ebmp.lib.jpa.entity.epc.epecUserMst.EpcUserMstCommonPK;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EpcUserMstCommonRepository extends JpaRepository<EpcUserMstCommon, EpcUserMstCommonPK> {

  EpcUserMstCommon findByUnqUserId(String unqUserId);

  EpcUserMstCommon findByUserId(String username);
}
