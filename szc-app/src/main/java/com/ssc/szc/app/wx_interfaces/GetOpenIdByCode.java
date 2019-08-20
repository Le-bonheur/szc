package com.ssc.szc.app.wx_interfaces;

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lebonheur
 */
@Component
public class GetOpenIdByCode {

    public Map getOpenIdByCode(String code){
        System.out.println(code);
        Map<String, Object> map = new HashMap<>();
        if (code == null || code.length() == 0) {
            map.put("status", 0);
            map.put("msg", "code 不能为空");
            return map;
        }
        //小程序唯一标识   (在微信小程序管理后台获取)
        String wxspAppid = WxConfigs.getProperties("appId");
        //小程序的 app secret (在微信小程序管理后台获取)
        String wxspSecret = WxConfigs.getProperties("appSecret");
        //授权（必填）
        String grant_type = "authorization_code";
        //////////////// 1、向微信服务器 使用登录凭证 code 获取 session_key 和 openid ////////////////
        //请求参数
        String params = "appid=" + wxspAppid + "&secret=" + wxspSecret + "&js_code=" + code + "&grant_type=" + grant_type;
        //发送请求
        //Creates CloseableHttpClient instance with default configuration.
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = null;
        try{
            httpGet = new HttpGet("https://api.weixin.qq.com/sns/jscode2session?"+params+"");
        }catch (IllegalArgumentException e){
            System.out.println("IllegalArgumentException:"+code);
        }
        CloseableHttpResponse response;
        String temp;
        try{
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            temp = EntityUtils.toString(entity,"UTF-8");
            //解析相应内容（转换成json对象）
            JSONObject json = JSONObject.fromObject(temp);
            //获取会话密钥（session_key）
            String session_key = json.getString("session_key");
            //用户的唯一标识（openid）
            String openid = json.getString("openid");
            map.put("openId", openid);
            map.put("session_key", session_key);
        }catch (IllegalArgumentException e){
            System.out.println("IllegalArgumentException:" + code);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();//释放资源
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(map.isEmpty()){
            map.put("status", 0);
            map.put("msg", "解密失败");
        }
        return map;
    }

}
