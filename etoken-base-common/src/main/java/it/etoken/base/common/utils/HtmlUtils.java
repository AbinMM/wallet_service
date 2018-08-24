package it.etoken.base.common.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;


public class HtmlUtils {

	static final String template = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body><div id=\"news_content\">${content}</div></body></html>";
	//添加快讯跟广告图的模板
	public static String gemHtml(String html, String savePath) throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String folder = df.format(System.currentTimeMillis());
		String filePath = folder + "/" + System.currentTimeMillis() + ".html";
		File f = new File(savePath + filePath);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		PrintStream printStream = null;
		try {
			html = nr2html(html);
			printStream = new PrintStream(new FileOutputStream(f));
			printStream.println(template.replace("${content}", html));
			return filePath;
		} catch (Exception e) {
			throw e;
		} finally {
			if (printStream != null)
				printStream.close();
		}
	}
	
	//添加资讯的html模板
	public static String gemHtmlforAlerts(String content, String title,String eosprice,String savePath,String template) throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String folder = df.format(System.currentTimeMillis());
		String filePath = folder + "/" + System.currentTimeMillis() + ".html";
		File f = new File(savePath + filePath);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		PrintStream printStream = null;
		try {
			template = template.replace("&quot;", "\"");
			if(eosprice==null || "".equals(eosprice)){
				eosprice="55.7";
				System.out.println(eosprice);
	        }
			content = nr2html(content);
			printStream = new PrintStream(new FileOutputStream(f));
			printStream.println(String.format(template, title,content,eosprice));
			return filePath;
		} catch (Exception e) {
			throw e;
		} finally {
			if (printStream != null)
				printStream.close();
		}
	}
	
	//添加资讯的html模板
	public static String gemHtmlforAlertsUpdate(String content, String title,String eosprice,String savePath,String template) throws Exception {
			File f = new File(savePath);
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			PrintStream printStream = null;
			try {
				template = template.replace("&quot;", "\"");
				if(eosprice==null || "".equals(eosprice)){
					eosprice="55.7";
					System.out.println(eosprice);
		        }
				content = nr2html(content);
				printStream = new PrintStream(new FileOutputStream(f));
				printStream.println(String.format(template, title,content,eosprice));
				return savePath;
			} catch (Exception e) {
				throw e;
			} finally {
				if (printStream != null)
					printStream.close();
			}
		}
	//修改快讯的已经产生的html模板
	public static void regemHtml(String content,String title,String eosprice,String filePath,String template) throws Exception {
		File f = new File(filePath);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		PrintStream printStream = null;
//		SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String str=datefmt.format(date);  
		try {
			template = template.replace("&quot;", "\"");
			if(eosprice==null || "".equals(eosprice)){
				eosprice="55.7";
	        }
			content = nr2html(content);
			printStream = new PrintStream(new FileOutputStream(f,false));
			printStream.println(String.format(template, title,content,eosprice));
		} catch (Exception e) {
			throw e;
		} finally {
			if (printStream != null)
				printStream.close();
		}
	}
	//修改资讯的已经产生的html模板
	public static void regemHtmlForInformation(String html,String filePath) throws Exception {
		File f = new File(filePath);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		PrintStream printStream = null;
		try {
			printStream = new PrintStream(new FileOutputStream(f));
			html = nr2html(html);
			printStream.println(template.replace("${content}", html));
		} catch (Exception e) {
			throw e;
		} finally {
			if (printStream != null)
				printStream.close();
		}
	}
	
	public static String nr2html(String str) {
		return str.replaceAll("[\n\r]", "<br>"); 
	}
	
	public static void main(String[] args) {
		String html = "<p>今日早间BTC小幅上涨，突破6500美元。BTC现报价6500.68美元，涨幅1.09%，主流币种普涨。&nbsp;\n" + 
				"1. 全球数字货币市场总市值现为2111.61亿美元，24h成交量为103.32亿美元。&nbsp;\n" + 
				"2. BTC现全球均价6526美元，三大主流交易所中，火币上价格为6500.68美元，OKEx上价格为6502.36美元，币安上价格为6505.02美元。&nbsp;\n" + 
				"3. 据《币世界》行情，其他主流币种表现如下：BCH暂报529.28美元（+1.70%）、ETC暂报12.47美元（+1.30%）、ETH暂报274.72美元（+1.02%）、LTC暂报56.87美元（+2.74%）、EOS暂报4.87美元（+1.37%）。&nbsp;\n" + 
				"4. 据《币世界》行情显示，市值前百币种24小时内行情跌多涨少。涨幅前三为：BCD(+42.43%)、GAS（+21.11%）、WTC（+17.46%）。跌幅前三为：NAS（-6.05%）、GXC（-2.81%）、EMC（-2.67%）。&nbsp;\n" + 
				"5. 概念板块24小时内仅两只下跌，涨幅前三分别为：IFO概念（+12.46%）、超级算力（+6.18%）、社交通讯（+4.32%）；下跌的分别为：2018世界杯（-1.69%）、平台币（-0.14%）。&nbsp;\n" + 
				"6. Cboe比特币9月合约实时报价6495美元，CME比特币8月合约实时报价6505美元。</p>";
		html = nr2html(html);
		
		System.out.println(html);
	}
}
