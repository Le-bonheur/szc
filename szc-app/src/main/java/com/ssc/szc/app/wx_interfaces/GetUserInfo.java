package com.ssc.szc.app.wx_interfaces;

import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ssc.szc.app.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lebonheur
 */
@Service
public class GetUserInfo {

    private final UserRepository userRepository;

    private String openId;

    @Autowired
    public GetUserInfo(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getOpenId() {
        return openId;
    }

    private void setOpenId(String openId) {
        this.openId = openId;
    }

    /**
     * 解密UserInfo
     *
     * @param encryptedData 加密数据
     * @param iv 算法初始向量
     * @param openId openId
     * @return jsonObject
     */
    public JSONObject decodeUserInfo(String encryptedData, String iv, String openId) {
        Map<String, Object> map = new HashMap<>(3);
        try {
            String result = AesUtil.decrypt(encryptedData, userRepository.getKeyByOpenId(openId), iv, "UTF-8");
            if (null != result && result.length() > 0) {

                JSONObject userInfoJSON = JSONObject.fromObject(result);

                String openIdDecode = userInfoJSON.getString("openId");
                //登录的openId与解密出的openId不一致，加入黑名单
                if(!openIdDecode.equals(openId)) {
                    userRepository.insertBlacklist(openId, "登录的openId与解密出的openId不一致");

                    map.put("status", 2);
                    map.put("msg", "解密失败");
                    return JSONObject.fromObject(map);
                }
                //解密出来的水印
                JSONObject watermark = userInfoJSON.getJSONObject("watermark");
                String appId = watermark.getString("appid");
                //配置文件中的appid
                String appIdConfig = WxConfigs.getProperties("appId");
                if(!appId.equals(appIdConfig)) {
                    userRepository.insertBlacklist(openId, "解密出的appid不是配置文件中的appid");

                    map.put("status", 3);
                    map.put("msg", "解密失败");
                    return JSONObject.fromObject(map);
                }

                Map<String, Object> userInfo = new HashMap<>(8);
                userInfo.put("openId", openIdDecode);
                //保存openId
                setOpenId(userInfoJSON.getString("openId"));
                userInfo.put("nickName", userInfoJSON.get("nickName"));
                userInfo.put("gender", userInfoJSON.get("gender"));
                userInfo.put("city", userInfoJSON.get("city"));
                userInfo.put("province", userInfoJSON.get("province"));
                userInfo.put("country", userInfoJSON.get("country"));
                userInfo.put("avatarUrl", userInfoJSON.get("avatarUrl"));
///                userInfo.put("unionId", userInfoJSON.get("unionId"));
                map.put("userInfo", userInfo);

                map.put("status", 1);
                map.put("msg", "解密成功");

                return JSONObject.fromObject(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("status", 0);
        map.put("msg", "解密失败");

        return JSONObject.fromObject(map);
    }
}