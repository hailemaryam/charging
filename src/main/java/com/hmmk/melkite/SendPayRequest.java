package com.hmmk.melkite;

import com.hmmk.melkite.Entity.SendPayItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


@ApplicationScoped
public class SendPayRequest {

    String URL = "https://10.175.206.42/soap-payment-api/ws/AmountChargingService/services/chargeAmount";
    String ContentType = "text/xml";

    public String send(SendPayItem sendPayItem) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        HttpClient client = HttpClients
                .custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        HttpPost post = new HttpPost(URL);
        post.addHeader(HttpHeaders.CONTENT_TYPE, ContentType);
        String xmlString = generateXmlString(sendPayItem);
        post.setEntity(new StringEntity(xmlString));
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, StandardCharsets.UTF_8);
    }

    private String generateXmlString(SendPayItem sendPayItem) {
        return "    <soapenv:Envelope\n" +
                "      xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "      xmlns:loc=\"http://www.csapi.org/schema/parlayx/payment/amount_charging/v3_1/local\" >\n" +
                "      <soapenv:Header>\n" +
                "          <tns:RequestSOAPHeader\n" +
                "              xmlns:tns=\"http://www.huawei.com.cn/schema/common/v2_1\">\n" +
                "              <tns:spId>" + sendPayItem.spId + "</tns:spId>\n" +
                "              <tns:spPassword>" + sendPayItem.hash + "</tns:spPassword>\n" +
                "              <tns:timeStamp>20220621000000</tns:timeStamp>\n" +
                "              <tns:serviceId>" + sendPayItem.serviceId + "</tns:serviceId>\n" +
                "              <tns:OA>"+ sendPayItem.phone + "</tns:OA>\n" +
                "              <tns:FA>" + sendPayItem.phone + "</tns:FA>\n" +
                "          </tns:RequestSOAPHeader>\n" +
                "      </soapenv:Header>\n" +
                "      <soapenv:Body>\n" +
                "          <loc:chargeAmount>\n" +
                "              <loc:endUserIdentifier>" + sendPayItem.phone + "</loc:endUserIdentifier>\n" +
                "              <loc:charge>\n" +
                "                  <description>charged</description>\n" +
                "                  <currency>Birr</currency>\n" +
                "                  <amount>200</amount>\n" +
                "                  <code>255</code>\n" +
                "              </loc:charge>\n" +
                "              <loc:referenceCode>225</loc:referenceCode>\n" +
                "          </loc:chargeAmount>\n" +
                "      </soapenv:Body>\n" +
                "    </soapenv:Envelope>";
    }
}

