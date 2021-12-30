package cn.sanenen.utils.http;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.log.Log;
import cn.sanenen.SunSetting;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author sun
 * @date 2021-02-25
 **/
public class OkHttpUtil {
	private static final Log log = Log.get();

	private static final long connectTimeout = SunSetting.getConnectTimeout();
	private static final long requestTimeout = SunSetting.getRequestTimeout();

	/**
	 * 发送post请求
	 *
	 * @param urlString 网址
	 * @param paramMap  post表单数据
	 * @return 返回数据
	 */
	public static String post(String urlString, Map<String, Object> paramMap) throws IOException {
		try (Response response = post(urlString, paramMap, null)) {
			return Objects.requireNonNull(response.body()).string();
		}
	}

	/**
	 * 发送post请求
	 *
	 * @param urlString 网址
	 * @param paramMap  post表单数据
	 * @param headerMap 消息头
	 * @return 返回数据
	 * @since 3.2.0
	 */
	public static Response post(String urlString, Map<String, Object> paramMap, Map<String, Object> headerMap) throws IOException {
		Request.Builder post = new Request.Builder()
				.url(urlString);
		//添加消息头
		if (MapUtil.isNotEmpty(headerMap)) {
			headerMap.forEach((k, v) -> post.addHeader(k, String.valueOf(v)));
		}
		FormBody.Builder builder = new FormBody.Builder();
		if (MapUtil.isNotEmpty(paramMap)) {
			//这里可以使用addEncoded ,如果值已经编码，这里就不再编码
			paramMap.forEach((k, v) -> builder.add(k, String.valueOf(v)));
		}
		post.post(builder.build());
		return call(post.build());

	}

	/**
	 * 发送post请求<br>
	 * 请求体body参数支持两种类型：
	 *
	 * <pre>
	 * 1. 标准参数，例如 a=1&amp;b=2 这种格式
	 * 2. Rest模式，此时body需要传入一个JSON或者XML字符串，Hutool会自动绑定其对应的Content-Type
	 * </pre>
	 *
	 * @param urlString 网址
	 * @param body      post表单数据
	 * @return 返回数据
	 */
	public static String post(String urlString, String body) throws IOException {
		try (Response response = post(urlString, body, null)) {
			return Objects.requireNonNull(response.body()).string();
		}
	}

	/**
	 * 发送post请求<br>
	 * 请求体body参数支持两种类型：
	 *
	 * <pre>
	 * 1. 标准参数，例如 a=1&amp;b=2 这种格式
	 * 2. Rest模式，此时body需要传入一个JSON或者XML字符串，Hutool会自动绑定其对应的Content-Type
	 * </pre>
	 *
	 * @param urlString 网址
	 * @param body      post表单数据
	 * @param headerMap 消息头
	 * @return 返回数据
	 * @since 3.2.0
	 */
	public static Response post(String urlString, String body, Map<String, Object> headerMap) throws IOException {
		Request.Builder post = new Request.Builder()
				.url(urlString);
		//添加消息头
		if (MapUtil.isNotEmpty(headerMap)) {
			headerMap.forEach((k, v) -> post.addHeader(k, String.valueOf(v)));
		}
		RequestBody requestBody = RequestBody.create(body.getBytes(CharsetUtil.CHARSET_UTF_8));
		post.post(requestBody);
		return call(post.build());
	}

	public static Response call(Request request) throws IOException {
		return Client.http.newCall(request).execute();
	}


	private static class Client {
		private static final OkHttpClient http = createHttp();

		private static OkHttpClient createHttp() {
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
			SSLContext sslContext = null;
			try {
				sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
			} catch (NoSuchAlgorithmException | KeyManagementException e) {
				log.error(e);
			}
			assert sslContext != null;
			return new OkHttpClient.Builder()
					.connectTimeout(connectTimeout, TimeUnit.SECONDS)
					.readTimeout(requestTimeout, TimeUnit.SECONDS)
					.sslSocketFactory(sslContext.getSocketFactory(), trustManager)
					.hostnameVerifier((hostname, session) -> true)
					.build();
		}
	}
}