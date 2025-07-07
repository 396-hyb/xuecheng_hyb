package com.xuecheng.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xuecheng.orders.config.AlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author hyb
 * @version 1.0
 * @description 测试支付宝接口
 * @date 2025/1/18
 */

@Slf4j
@Controller
public class PayTestController {

    @Value("${pay.alipay.APP_ID}")
    private String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    private String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    private String ALIPAY_PUBLIC_KEY;

    @RequestMapping("/alipaytest")
    public void doPost(HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse) throws ServletException, IOException, AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
//        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request

        // 构造请求参数以调用接口
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();  //创建API对应的request
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        // 设置商户订单号
        model.setOutTradeNo("20150320010101004");
        // 设置订单总金额
        model.setTotalAmount("88.88");
        // 设置订单标题
        model.setSubject("Iphone16 1T");
        // 设置产品码
        model.setProductCode("QUICK_WAP_WAY");

        alipayRequest.setBizModel(model);
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
//        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"product_code\":\"QUICK_WAP_WAY\"" +
//                "  }");//填充业务参数


//        String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        String form = alipayClient.pageExecute(alipayRequest, "POST").getBody(); //调用SDK生成表单
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();

        System.out.println(form);



    }

}
