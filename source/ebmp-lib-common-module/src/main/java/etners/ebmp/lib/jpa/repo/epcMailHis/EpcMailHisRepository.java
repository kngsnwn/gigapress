package etners.ebmp.lib.jpa.repo.epcMailHis;

import etners.ebmp.lib.jpa.entity.epc.epcMailHis.EpcMailHis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpcMailHisRepository extends JpaRepository<EpcMailHis, Long> {

  EpcMailHis findBySolCdAndSn(String var1, String var2);
}