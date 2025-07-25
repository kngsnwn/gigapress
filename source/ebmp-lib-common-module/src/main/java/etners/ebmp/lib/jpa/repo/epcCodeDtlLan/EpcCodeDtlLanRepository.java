package etners.ebmp.lib.jpa.repo.epcCodeDtlLan;

import etners.ebmp.lib.jpa.entity.epc.epcCodeDtlLan.EpcCodeDtlLan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


public interface EpcCodeDtlLanRepository extends JpaRepository<EpcCodeDtlLan, String>, QuerydslPredicateExecutor {

  List<EpcCodeDtlLan> findByCmpyCdAndCdGrupAndUseYnOrderBySeqNoAsc(String cmpyCd, String cdGrup, boolean useYn);

  List<EpcCodeDtlLan> findByCmpyCdAndCdGrupAndUseYnAndCdIdLikeOrderBySeqNoAsc(String cmpyCd, String cdGrup, boolean useYn, String cdId);

  EpcCodeDtlLan findByCmpyCdAndCdGrupAndCdId(String cmpyCd, String cdGrup, String cdId);
}
