package etners.ebmp.lib.api.resultstatus.service;

import etners.ebmp.lib.api.basemodel.ResultStatusG2;
import java.util.HashMap;
import java.util.List;

public interface ResultStatusService {

  ResultStatusG2 getResultStatusByMessageCode(String messageCode);

  List<ResultStatusG2> getAllResultStatusList();

  HashMap<String, ResultStatusG2> getAllResultStatusMap();

  ResultStatusG2 getResultStatusByKeyNamespace(String keyNamespace);
}
