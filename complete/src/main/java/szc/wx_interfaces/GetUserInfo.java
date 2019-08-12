package szc.wx_interfaces;

import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import szc.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

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

    public JSONObject decodeUserInfo(String encryptedData, String iv, String openId) {
        Map<String, Object> map = new HashMap<>();
        try {
            String result = AesUtil.decrypt(encryptedData, userRepository.getKeyByOpenId(openId), iv, "UTF-8");
            if (null != result && result.length() > 0) {
                map.put("status", 1);
                map.put("msg", "解密成功");
                JSONObject userInfoJSON = JSONObject.fromObject(result);
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("openId", userInfoJSON.get("openId"));
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

                System.out.println(map);

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