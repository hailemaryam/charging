package com.hmmk.melkite;

import com.hmmk.melkite.Entity.SendPayItem;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Path("/test")
public class Test {

    private final SendPayRequest sendPayRequest;

    public Test(SendPayRequest sendPayRequest) {
        this.sendPayRequest = sendPayRequest;
    }

    @POST
    @Path("pay")
    @Consumes("application/json")
    @Produces("application/json")
    public String sendPay(SendPayItem sendPayItem) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return sendPayRequest.send(sendPayItem);
    }
}
