package com.ssc.szc.app.services.user;

import com.ssc.szc.app.repository.UserRepository;
import com.ssc.szc.app.wx_interfaces.GetOpenIdByCode;
import com.ssc.szc.app.wx_interfaces.GetUserInfo;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Lebonheur
 */
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


    /**
     * @param code 登录凭证
     * @return openId
     */
    public String getOpenIdByCode(String code) {
        Map map = getOpenIdByCode.getOpenIdByCode(code);

        String openId = map.get("openId").toString();
        String sessionKey = map.get("session_key").toString();
        System.out.println(openId);
        userRepository.updateSessionKey(openId, sessionKey);

        return openId;
    }

    /**
     * 对UserInfo解密
     *
     * @param encryptedData 加密数据
     * @param iv 算法初始向量
     * @param openId openId
     * @return userInfo
     */
    public JSONObject decodeUserInfo(String encryptedData, String iv, String openId) {
        return getUserInfo.decodeUserInfo(encryptedData, iv, openId);
    }
}
