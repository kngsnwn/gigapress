package etners.ebmp.lib.api.resultstatus.service;

import etners.ebmp.lib.api.basemodel.ResultStatusG2;
import etners.ebmp.lib.enums.lang.EbmpLang;
import etners.ebmp.lib.jpa.entity.epc.epcCodeDtl.EpcCodeDtl;
import etners.ebmp.lib.jpa.entity.epc.epcCodeDtlLan.EpcCodeDtlLan;
import etners.ebmp.lib.jpa.repo.epcCodeDtl.EpcCodeDtlRepository;
import etners.ebmp.lib.jpa.repo.epcCodeDtlLan.EpcCodeDtlLanRepository;
import etners.ebmp.lib.jpa.repo.epcScodeDtl.EpcScodeDtlRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;
//CodeDetailRepository;

@Service
public class ResultStatusServiceImpl implements ResultStatusService {

  private final String MESSAGE_CODE_CD_GRUP = "217";

  private final EpcCodeDtlRepository epcCodeDtlRepository;

  private final EpcScodeDtlRepository epcScodeDtlRepository;
  private final EpcCodeDtlLanRepository epcCodeDtlLanRepository;

  public ResultStatusServiceImpl(EpcCodeDtlRepository epcCodeDtlRepository, EpcScodeDtlRepository epcScodeDtlRepository, EpcCodeDtlLanRepository epcCodeDtlLanRepository) {
    this.epcCodeDtlRepository = epcCodeDtlRepository;
    this.epcScodeDtlRepository = epcScodeDtlRepository;
    this.epcCodeDtlLanRepository = epcCodeDtlLanRepository;
  }

  @Override
  public ResultStatusG2 getResultStatusByMessageCode(String messageCode) {
    EpcCodeDtl code = epcCodeDtlRepository.findByCdGrupAndCdId(MESSAGE_CODE_CD_GRUP, messageCode);

    if (code == null) {
      return new ResultStatusG2("6000", "DB에 정의되지 않은 코드가 요청되었습니다. 코드번호 : " + messageCode);
    }

    return convertCodeDetailToResultStatusG2(code);
  }

  @Override
  public List<ResultStatusG2> getAllResultStatusList() {
    List<EpcCodeDtl> codeList = epcCodeDtlRepository.findByCdGrup(MESSAGE_CODE_CD_GRUP);

    List<ResultStatusG2> resultStatusList = new ArrayList<>();

    for (EpcCodeDtl codeDetail : codeList) {
      ResultStatusG2 resultStatusG2 = convertCodeDetailToResultStatusG2(codeDetail);

      resultStatusList.add(resultStatusG2);
    }

    return resultStatusList;
  }

  @Override
  public HashMap<String, ResultStatusG2> getAllResultStatusMap() {
    HashMap<String, ResultStatusG2> resultStatusMap = new HashMap<>();

    List<ResultStatusG2> resultStatusList = getAllResultStatusList();

    for (ResultStatusG2 resultStatus : resultStatusList) {
      resultStatusMap.put(resultStatus.getMessageCode(), resultStatus);
    }

    return resultStatusMap;
  }

  @Override
  public ResultStatusG2 getResultStatusByKeyNamespace(String keyNamespace) {
    EpcCodeDtl code = epcCodeDtlRepository.findByCdGrupAndCdDesc1(MESSAGE_CODE_CD_GRUP, keyNamespace);

    return convertCodeDetailToResultStatusG2(code);
  }


  private ResultStatusG2 convertCodeDetailToResultStatusG2(EpcCodeDtl code) {
    EbmpLang defaultEbmpLang = EbmpLang.KO;

    String messageCode = code.getCdId();
    String keyNamespace = code.getCdDesc1();

    HashMap<EbmpLang, String> messageTextI18N = convertI18NMap(code);

    return new ResultStatusG2(messageCode, messageTextI18N, defaultEbmpLang, keyNamespace);
  }

  private HashMap<EbmpLang, String> convertI18NMap(EpcCodeDtl code) {
    HashMap<EbmpLang, String> messageTextI18N = new HashMap<>();

    String messageTextKo = code.getCdNm();

    messageTextI18N.put(EbmpLang.KO, messageTextKo);

    try {
      //다국어 엔티티 모델

      EpcCodeDtlLan multilingual = epcCodeDtlLanRepository.findByCmpyCdAndCdGrupAndCdId(code.getCmpyCd(), code.getCdGrup(), code.getCdId());

      if (multilingual != null) {
        String messageTextEn = multilingual.getCdEn();
        String messageTextJp = multilingual.getCdJp();
        String messageTextVn = multilingual.getCdEn();
        String messageTextZh = multilingual.getCdZh();

        messageTextI18N.put(EbmpLang.EN, messageTextEn);
        messageTextI18N.put(EbmpLang.JP, messageTextJp);
        messageTextI18N.put(EbmpLang.VN, messageTextVn);
        messageTextI18N.put(EbmpLang.ZH, messageTextZh);
      } else {
        //다국어 값이 존재하지 않으면 한국어로 적용.
        messageTextI18N.put(EbmpLang.EN, messageTextKo);
        messageTextI18N.put(EbmpLang.JP, messageTextKo);
        messageTextI18N.put(EbmpLang.VN, messageTextKo);
        messageTextI18N.put(EbmpLang.ZH, messageTextKo);
      }
    } catch (Exception e) {
      messageTextI18N.put(EbmpLang.EN, messageTextKo);
      messageTextI18N.put(EbmpLang.JP, messageTextKo);
      messageTextI18N.put(EbmpLang.VN, messageTextKo);
      messageTextI18N.put(EbmpLang.ZH, messageTextKo);
    }

    return messageTextI18N;
  }

}
