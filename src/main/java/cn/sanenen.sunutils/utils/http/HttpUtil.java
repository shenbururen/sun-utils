package cn.sanenen.sunutils.utils.http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.sanenen.sunutils.SunSetting;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.fluent.Form;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * @author sun
 * @date 2021-02-25
 **/
public class HttpUtil {
	private static final int maxConnTotal = SunSetting.getMaxConnTotal();
	private static final Timeout connectTimeout = Timeout.ofSeconds(SunSetting.getConnectTimeout());
	private static final Timeout requestTimeout = Timeout.ofSeconds(SunSetting.getRequestTimeout());

	public static String uploadFile(String urlString, Map<String, Object> paramMap) throws IOException {

		return uploadFile(urlString, null, paramMap, CharsetUtil.CHARSET_UTF_8)
				.returnContent()
				.asString(CharsetUtil.CHARSET_UTF_8);
	}

	public static String uploadFile(String urlString, Map<String, Object> paramMap, Charset charSet) throws IOException {
		return uploadFile(urlString, null, paramMap, charSet)
				.returnContent()
				.asString(charSet);
	}

	public static Response uploadFile(String urlString, Map<String, Object> headerMap, Map<String, Object> paramMap) throws IOException {
		return uploadFile(urlString, headerMap, paramMap, CharsetUtil.CHARSET_UTF_8);
	}

	public static Response uploadFile(String urlString, Map<String, Object> headerMap, Map<String, Object> paramMap, Charset charSet) throws IOException {
		Request request = Request.post(urlString);
		//添加消息头
		if (CollUtil.isNotEmpty(headerMap)) {
			headerMap.forEach((k, v) -> request.addHeader(k, String.valueOf(v)));
		}
		MultipartEntityBuilder builder = MultipartEntityBuilder.create().setCharset(charSet);
		paramMap.forEach((k, v) -> {
			//判断是文件还是文本
			if (v instanceof File) {
				File file = (File) v;
				builder.addBinaryBody(k, file, ContentType.MULTIPART_FORM_DATA.withCharset(charSet), FileUtil.getName(file));
			} else if (v instanceof ContentBody) {
				builder.addPart(k, (ContentBody) v);
			} else {
				builder.addTextBody(k, String.valueOf(v), ContentType.TEXT_PLAIN.withCharset(charSet));
			}
		});
		request.body(builder.build());
		return call(request);
	}

	public static String post(String urlString, Map<String, Object> paramMap) throws IOException {
		return post(urlString, paramMap, CharsetUtil.CHARSET_UTF_8);
	}

	public static String post(String urlString, Map<String, Object> paramMap, Charset charSet) throws IOException {
		return post(urlString, null, paramMap, charSet)
				.returnContent()
				.asString(charSet);
	}

	public static Response post(String urlString, Map<String, Object> headerMap, Map<String, Object> paramMap) throws IOException {
		return post(urlString, headerMap, paramMap, CharsetUtil.CHARSET_UTF_8);
	}

	public static Response post(String urlString, Map<String, Object> headerMap, Map<String, Object> paramMap, Charset charSet) throws IOException {
		Request request = Request.post(urlString);
		//添加消息头
		if (CollUtil.isNotEmpty(headerMap)) {
			headerMap.forEach((k, v) -> request.addHeader(k, String.valueOf(v)));
		}
		Form form = Form.form();
		if (CollUtil.isNotEmpty(paramMap)) {
			paramMap.forEach((k, v) -> form.add(k, String.valueOf(v)));
		}
		request.bodyForm(form.build(), charSet);
		return call(request);
	}

	public static String post(String urlString, String body) throws IOException {
		return post(urlString, body, CharsetUtil.CHARSET_UTF_8);
	}

	public static String post(String urlString, String body, Charset charSet) throws IOException {
		return post(urlString, null, body, charSet)
				.returnContent()
				.asString(charSet);
	}

	public static Response post(String urlString, Map<String, Object> headerMap, String body) throws IOException {
		return post(urlString, headerMap, body, CharsetUtil.CHARSET_UTF_8);
	}

	public static Response post(String urlString, Map<String, Object> headerMap, String body, Charset charSet) throws IOException {
		Request request = Request.post(urlString);
		//添加消息头
		if (CollUtil.isNotEmpty(headerMap)) {
			headerMap.forEach((k, v) -> request.addHeader(k, String.valueOf(v)));
		}
		HttpEntity httpEntity = HttpEntities.create(body, getContentType(body, charSet));
		request.body(httpEntity);
		return call(request);
	}

	public static String get(String urlString) throws IOException, ParseException {
		return get(urlString, null, CharsetUtil.CHARSET_UTF_8);
	}

	public static String get(String urlString, Map<String, Object> paramMap) throws IOException, ParseException {
		return get(urlString, paramMap, CharsetUtil.CHARSET_UTF_8);
	}

	public static String get(String urlString, Map<String, Object> paramMap, Charset charSet) throws IOException, ParseException {
		return get(urlString, null, paramMap, charSet)
				.returnContent()
				.asString(charSet);
	}

	public static String get(String urlString, Map<String, Object> headerMap, Map<String, Object> paramMap) throws IOException, ParseException {
		return get(urlString, headerMap, paramMap, CharsetUtil.CHARSET_UTF_8)
				.returnContent()
				.asString(CharsetUtil.CHARSET_UTF_8);
	}

	public static Response get(String urlString, Map<String, Object> headerMap, Map<String, Object> paramMap, Charset charSet) throws IOException, ParseException {
		Form form = Form.form();
		if (CollUtil.isNotEmpty(paramMap)) {
			paramMap.forEach((k, v) -> form.add(k, String.valueOf(v)));
		}
		String paramStr = EntityUtils.toString(new UrlEncodedFormEntity(form.build(), charSet));
		Request request = Request.get(urlString + '?' + paramStr);
		//添加消息头
		if (CollUtil.isNotEmpty(headerMap)) {
			headerMap.forEach((k, v) -> request.addHeader(k, String.valueOf(v)));
		}
		return call(request);
	}

	public static Response call(Request request) throws IOException {
		return request.execute(Client.c);
	}

	public static ContentType getContentType(String body, Charset charSet) {
		ContentType contentType = ContentType.TEXT_PLAIN;
		if (StrUtil.isNotBlank(body)) {
			char firstChar = body.charAt(0);
			switch (firstChar) {
				case '{':
				case '[':
					// JSON请求体
					contentType = ContentType.APPLICATION_JSON;
					break;
				case '<':
					// XML请求体
					contentType = ContentType.APPLICATION_XML;
					break;
				default:
					break;
			}
		}
		if (charSet != null) {
			contentType.withCharset(charSet);
		}
		return contentType;
	}

	private static class Client {
		private static final CloseableHttpClient c = HttpClientBuilder.create()
				.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
						.setSSLSocketFactory(getSSLFactory())
						.setValidateAfterInactivity(TimeValue.ofSeconds(10))
						.setMaxConnPerRoute(maxConnTotal - 1)
						.setMaxConnTotal(maxConnTotal)
						.build())
				.evictIdleConnections(TimeValue.ofMinutes(1))
				.disableAutomaticRetries()
				.setDefaultRequestConfig(RequestConfig.custom()
						.setConnectTimeout(connectTimeout)
						.setConnectionRequestTimeout(requestTimeout)
						.build())
				.build();

		private static SSLConnectionSocketFactory getSSLFactory() {
			X509ExtendedTrustManager trustManager = new X509ExtendedTrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {
				}

				@Override
				public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {
				}

				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			};
			SSLContext ctx = null;
			try {
				ctx = SSLContext.getInstance("TLS");
				ctx.init(null, new TrustManager[]{trustManager}, null);
			} catch (NoSuchAlgorithmException | KeyManagementException e) {
				e.printStackTrace();
			}
			assert ctx != null;
			return new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
		}
	}
}