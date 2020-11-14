package com.offcn.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsUtil {

    @Value("${AccessKeyID}")
    private String AccessKeyID;

    @Value("${AccessKeySecret}")
    private String AccessKeySecret;

    //定义短信发送服务器的访问地址
    private String domain="dysmsapi.aliyuncs.com";

    /**
     * 发送短信方法
     * @param mobile  接收手机号码
     * @param template_code 短信模板编号
     * @param sign      短信签名
     * @param parm      模板参数值
     * @return
     */
    public CommonResponse sendSms(String mobile,String template_code,String sign,String parm){
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", AccessKeyID, AccessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(domain);
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", mobile);
        request.putQueryParameter("SignName", sign);
        request.putQueryParameter("TemplateCode", template_code);
        request.putQueryParameter("TemplateParam", parm);
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return response;
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return null;
    }
}
