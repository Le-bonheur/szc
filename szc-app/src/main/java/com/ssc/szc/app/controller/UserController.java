package com.ssc.szc.app.controller;

import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ssc.szc.app.services.user.UserService;

/**
 * @author Lebonheur
 */
@RestController
@RequestMapping(path = "/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @param code 登录凭证
     * @return openId
     */
    @PostMapping(path = "/getOpenIdByCode")
    public @ResponseBody
    String getOpenIdByCode(@RequestParam String code) {
        try{
            return userService.getOpenIdByCode(code);
        } catch (NullPointerException e){
            System.out.println(code);
        }
        return null;
    }

    /**
     * @param encryptedData 加密数据
     * @param iv 算法初始向量
     * @param openId openId
     * @return jsonObject
     */
    @ResponseBody
    @PostMapping(value = "/decodeUserInfo")
    public JSONObject decodeUserInfo(String encryptedData, String iv, String openId) {
        return userService.decodeUserInfo(encryptedData, iv, openId);
    }

}
