// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   EpcScodeDtlRepository.java

package etners.ebmp.lib.jpa.repo.epcScodeDtl;

import etners.ebmp.lib.jpa.entity.epc.epcScodeDtl.EpcScodeDtl;
import etners.ebmp.lib.jpa.entity.epc.epcScodeDtl.EpcScodeDtlPK;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


public interface EpcScodeDtlRepository extends JpaRepository<EpcScodeDtl, EpcScodeDtlPK>, QuerydslPredicateExecutor<EpcScodeDtl> {

  List<EpcScodeDtl> findBySolCdInOrderBySolCdAscCdGrupAscCdIdAsc(List<String> solCdList);

  List<EpcScodeDtl> findByUseYnAndSolCdInOrderBySolCdAscCdGrupAscCdIdAsc(boolean useYn, List<String> solCdList);

  List<EpcScodeDtl> findBySolCdAndCdGrupOrderByCdIdAsc(String solCd, String CdGrup);

  List<EpcScodeDtl> findBySolCdAndCdGrupAndCdIdLikeOrderByCdIdAsc(String solCd, String CdGrup, String cdIdLike);

  EpcScodeDtl findBySolCdAndCdGrupAndCdId(String solCd, String CdGrup, String cdId);

  List<EpcScodeDtl> findBySolCdAndCdGrupAndCdIdLikeAndUseYnOrderBySeqNoAsc(String solCd, String cdGrup, String cdId, boolean useYn);

  List<EpcScodeDtl> findBySolCdAndCdGrupAndUseYnOrderByCdIdAsc(String solCd, String CdGrup, boolean useYn);
}
