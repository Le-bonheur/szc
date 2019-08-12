package szc.controller;

import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import szc.service.UserService;

@Controller
@RequestMapping(path = "/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

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

    @ResponseBody
    @PostMapping(value = "/decodeUserInfo")
    public JSONObject decodeUserInfo(String encryptedData, String iv, String openId) {
        return userService.decodeUserInfo(encryptedData, iv, openId);
    }

}
