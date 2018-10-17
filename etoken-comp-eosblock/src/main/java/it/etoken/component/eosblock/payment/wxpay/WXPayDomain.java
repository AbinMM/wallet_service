package it.etoken.component.eosblock.payment.wxpay;

import org.springframework.stereotype.Component;

import com.github.wxpay.sdk.IWXPayDomain;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConstants;

@Component
public class WXPayDomain implements IWXPayDomain {

	@Override
	public void report(String domain, long elapsedTimeMillis, Exception ex) {
	}

	@Override
	public DomainInfo getDomain(WXPayConfig config) {
		String domain = WXPayConstants.DOMAIN_API;
		boolean primaryDomain = true;
		IWXPayDomain.DomainInfo di = new IWXPayDomain.DomainInfo(domain, primaryDomain);
		return di;
	}

}
