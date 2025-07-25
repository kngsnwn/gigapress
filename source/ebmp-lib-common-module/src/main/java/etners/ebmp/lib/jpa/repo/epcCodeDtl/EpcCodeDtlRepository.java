// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   EpcCodeDtlRepository.java

package etners.ebmp.lib.jpa.repo.epcCodeDtl;

import etners.ebmp.lib.jpa.entity.epc.epcCodeDtl.EpcCodeDtl;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


public interface EpcCodeDtlRepository extends JpaRepository<EpcCodeDtl, Long>, QuerydslPredicateExecutor<EpcCodeDtl> {

  List<EpcCodeDtl> findByCmpyCd(String cmpyCd);

  List<EpcCodeDtl> findByCmpyCdAndCdGrup(String cmpyCd, String cdGrup);

  List<EpcCodeDtl> findByCmpyCdInOrderByCmpyCdAscCdGrupAscCdIdAsc(List<String> cmpyCdList);

  List<EpcCodeDtl> findByUseYnAndCmpyCdInOrderByCmpyCdAscCdGrupAscCdIdAsc(boolean useYn, List<String> cmpyCdList);

  List<EpcCodeDtl> findByCdGrup(String runtimeModeCode);

  List<EpcCodeDtl> findByCdGrupAndCdIdIn(String cdGrup, List<String> cdIds);

  EpcCodeDtl findByCdGrupAndCdId(String cdGrup, String cdId);

  EpcCodeDtl findByCdGrupAndCdDesc1(String cdGrup, String keyNamespace);

  List<EpcCodeDtl> findByUseYnOrderByCmpyCdAscCdGrupAscCdIdAsc(boolean useYn);

  List<EpcCodeDtl> findByCmpyCdAndCdGrupAndUseYnOrderBySeqNoAsc(String cmpyCd, String cdGrup, boolean useYn);

  List<EpcCodeDtl> findByCmpyCdAndCdGrupAndUseYnAndCdIdLikeOrderBySeqNoAsc(String cmpyCd, String cdGrup, boolean useYn, String cdIdLike);

  EpcCodeDtl findByCmpyCdAndCdGrupAndCdId(String cmpyCd, String cdGrup, String cdId);
}
