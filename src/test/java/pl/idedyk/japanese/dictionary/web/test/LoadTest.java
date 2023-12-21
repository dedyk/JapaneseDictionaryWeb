package pl.idedyk.japanese.dictionary.web.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LoadTest {

	public static void main(String[] args) throws Exception {
		// 

		final String URL = "https://localhost:8443/wordDictionary";
		final int NUMBER_OF_THREADS = 5;
		final int NUMBER_OF_CALLS = 100;
		final int[] SLEEP_RANGE = new int[] { 500, 501 };
		
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				Random random = new Random();
				
				try {
					for (int callNumber = 0; callNumber < NUMBER_OF_CALLS; ++callNumber) {
						Thread.sleep(random.nextInt(SLEEP_RANGE[1] - SLEEP_RANGE[0]) + SLEEP_RANGE[0]);
						callURL(URL, callNumber);	
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		long start = System.currentTimeMillis();
		
		Thread[] threads = new Thread[NUMBER_OF_THREADS];
		
		for (int threadsNo = 0; threadsNo < NUMBER_OF_THREADS; ++threadsNo) {
			threads[threadsNo] = new Thread(runnable, "Caller" + threadsNo);
		}

		for (int threadsNo = 0; threadsNo < NUMBER_OF_THREADS; ++threadsNo) {
			threads[threadsNo].start();
		}

		for (int threadsNo = 0; threadsNo < NUMBER_OF_THREADS; ++threadsNo) {
			threads[threadsNo].join();
		}
		
		long stop = System.currentTimeMillis();
		
		//
		
		long testTime = (stop - start) / 1000;
		
		float throughput = (float)(NUMBER_OF_CALLS * NUMBER_OF_THREADS) / (float)testTime;
		
		System.out.println("Start: " + new Date(start));
		System.out.println("Stop: " + new Date(stop));
		System.out.println("Test tiem: " + testTime);
		System.out.println("Number of calls: " + (NUMBER_OF_CALLS * NUMBER_OF_THREADS));
		System.out.println("Throughput: " + throughput);
	}
	
	private static void callURL(String urlString, int callNumber) throws Exception {

		final int TIMEOUT = 3000;
		
		// url
		URL url = new URL(urlString);
		
		// akceptujemy wszystkie certyfikaty
		TrustManager[] trustAllCerts = new TrustManager[]{
			    new X509TrustManager() {
			        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			            return null;
			        }
			        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			        }
			        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			        }
			    }
			};

	    SSLContext sc = SSLContext.getInstance("SSL");
	    sc.init(null, trustAllCerts, new java.security.SecureRandom());

		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    
		// tworzymy polaczenie
		HttpsURLConnection httpURLConnection = (HttpsURLConnection)url.openConnection();
		
		httpURLConnection.setRequestMethod("GET");

		// parametry do polaczenia
		httpURLConnection.setUseCaches(false);
		httpURLConnection.setAllowUserInteraction(false);
		httpURLConnection.setConnectTimeout(TIMEOUT);
		httpURLConnection.setReadTimeout(TIMEOUT);

		//

		// wywolaj serwer
		httpURLConnection.connect();
		
		int statusCode = httpURLConnection.getResponseCode();
		
		System.out.println("Thread(" + Thread.currentThread().getName() + "): status code: " + statusCode + ", call number: " + callNumber);

		if (statusCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			String inputLine;
			
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		}
	}
}
