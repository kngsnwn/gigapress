// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   EpcCodeLanRepository.java

package etners.ebmp.lib.jpa.repo.epcCodeLan;

import etners.ebmp.lib.jpa.entity.epc.epcCodeLan.EpcCodeLan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


public interface EpcCodeLanRepository extends JpaRepository<EpcCodeLan, String>, QuerydslPredicateExecutor<EpcCodeLan> {

  EpcCodeLan findByCmpyCdAndCdGrup(String cmpyCd, String cdGrup);
}
