package it.etoken.component.eosblock.payment.wxpay;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.wxpay.sdk.IWXPayDomain;
import com.github.wxpay.sdk.WXPayConfig;

@Component
public class WxPayConfig extends WXPayConfig {
	@Autowired
	IWXPayDomain wxPayDomain;

	@Value("${payment.wxpay.appId}")
	String appId;
	@Value("${payment.wxpay.mchID}")
	String mchID;
	@Value("${payment.wxpay.key}")
	String key;
	@Value("${payment.wxpay.certPath}")
	String certPath;
	@Value("${payment.wxpay.notifyUrl}")
	String notifyUrl;

	@Override
	public String getAppID() {
		return appId;
	}

	@Override
	public String getMchID() {
		return mchID;
	}

	@Override
	public String getKey() {
		return key;
	}
	
	public String getNotifyUrl() {
		return notifyUrl;
	}

	@Override
	public InputStream getCertStream() {
		InputStream is = null;
		try {
			is = new FileInputStream(certPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return is;
	}

	@Override
	public IWXPayDomain getWXPayDomain() {
		return wxPayDomain;
	}
}
