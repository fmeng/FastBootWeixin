package com.example.myproject.mvc.advice;

import com.example.myproject.module.WxRequest;
import com.example.myproject.module.message.WxMessage;
import com.example.myproject.mvc.WxRequestResponseUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.AbstractXmlHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;

/**
 * ResponseBodyAdvice Spring 4.1以上才支持。
 * 这个作用是为响应自动添加fromUser
 * 不加这个注解会有问题@ControllerAdvice，不识别
 *
 * @author Guangshan
 * @since 2017年8月15日
 */
@ControllerAdvice
public class WxMessageResponseBodyAdvice implements ResponseBodyAdvice<WxMessage>, Ordered {

    private static final Log logger = LogFactory.getLog(MethodHandles.lookup().lookupClass());

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 200000;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return AbstractXmlHttpMessageConverter.class.isAssignableFrom(converterType) &&
                WxMessage.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public WxMessage beforeBodyWrite(WxMessage body, MethodParameter returnType,
                                  MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!(request instanceof ServletServerHttpRequest)) {
            return body;
        }
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        WxRequest wxRequest = WxRequestResponseUtils.getWxRequestFromRequestAttribute(servletRequest);
        if (wxRequest != null) {
            if (body.getFromUserName() == null) {
                body.setFromUserName(wxRequest.getToUserName());
            }
            if (body.getToUserName() == null) {
                body.setToUserName(wxRequest.getFromUserName());
            }
        }
        return body;
    }

}