package szc.service;

import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import szc.repository.UserRepository;
import szc.wx_interfaces.GetOpenIdByCode;
import szc.wx_interfaces.GetUserInfo;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final GetOpenIdByCode getOpenIdByCode;

    private final GetUserInfo getUserInfo;

    @Autowired
    public UserService(UserRepository userRepository, GetOpenIdByCode getOpenIdByCode, GetUserInfo getUserInfo) {
        this.userRepository = userRepository;
        this.getOpenIdByCode = getOpenIdByCode;
        this.getUserInfo = getUserInfo;
    }

    public String getOpenIdByCode(String code) {
        Map map = getOpenIdByCode.getOpenIdByCode(code);
        String openId = map.get("openId").toString();
        String sessionKey = map.get("session_key").toString();
        System.out.println(openId);
        userRepository.updateSessionKey(openId, sessionKey);

        return openId;
    }

    public JSONObject decodeUserInfo(String encryptedData, String iv, String openId) {
        return getUserInfo.decodeUserInfo(encryptedData, iv, openId);
    }
}
