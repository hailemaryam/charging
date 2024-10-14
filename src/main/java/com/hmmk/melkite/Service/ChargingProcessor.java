package com.hmmk.melkite.Service;

import com.hmmk.melkite.DTO.SendPayItem;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class ChargingProcessor {

    @Inject
    @Channel("chargeable-item")
    Emitter<SendPayItem> emitChargeableItem;
    @Inject
    @Channel("success-notifier")
    Emitter<SendPayItem> emitSuccessNotifier;
    @Inject
    @Channel("fail-notifier")
    Emitter<SendPayItem> emitFailNotifier;

    @Inject
    SendPayRequest sendPayRequest;

    @Incoming("chargeable-item-receiver")
    @Blocking
    public void receive(SendPayItem sendPayItem) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // todo for daily limit check total no of request with time until morning and after morning
        if (!sendPayItem.getAtDateOf().equalsIgnoreCase(Instant.now().truncatedTo(ChronoUnit.DAYS).toString())){
            emitFailNotifier.send(sendPayItem);
        } else if (sendPayItem.getLastSentTime() == null) {
            doCharging(sendPayItem);
        } else if (sendPayItem.getLastSentTime().isAfter(Instant.now().minus(1, ChronoUnit.HOURS))) {
            // skip charging
            emitChargeableItem.send(sendPayItem);
        } else if (sendPayItem.getLastSentTime().isBefore(Instant.now().minus(1, ChronoUnit.HOURS))) {
            doCharging(sendPayItem);
        }
    }

    private void doCharging(SendPayItem sendPayItem) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String chargingResponse = sendPayRequest.send(sendPayItem);
        SendPayItem updatedSendPayItem = updateSendPayItem(sendPayItem, chargingResponse);
        if (updatedSendPayItem.getStatus()){
            emitSuccessNotifier.send(updatedSendPayItem);
        } else {
            emitChargeableItem.send(updatedSendPayItem);
        }
    }

    private SendPayItem updateSendPayItem(SendPayItem sendPayItem, String response){
        sendPayItem.setLastSentTime(Instant.now());
        if (response.contains("chargeAmountResponse")){
            sendPayItem.setStatus(true);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
        } else if (response.contains("NO_BALANCE")){
            sendPayItem.setStatus(false);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
            sendPayItem.setNoBalanceCount(sendPayItem.getNoBalanceCount() + 1);
        } else if (response.contains("INVALID_RENEWAL_PERIOD")){
            sendPayItem.setStatus(false);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
            sendPayItem.setInvalidRenewalPeriodCount(sendPayItem.getInvalidRenewalPeriodCount() + 1);
        } else if (response.contains("INVALID_SUBSCRIBER_STATUS")){
            sendPayItem.setStatus(false);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
            sendPayItem.setInvalidSubscriberStatusCount(sendPayItem.getInvalidSubscriberStatusCount() + 1);
        } else if (response.contains("INTERNAL_ERROR")){
            sendPayItem.setStatus(false);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
            sendPayItem.setInternalErrorCount(sendPayItem.getInternalErrorCount() + 1);
        } else if (response.contains("NO_SUBSCRIPTION")){
            sendPayItem.setStatus(false);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
            sendPayItem.setNoSubscriptionCount(sendPayItem.getNoSubscriptionCount() + 1);
        } else if (response.contains("HOUR_RETRY_LIMIT_EXCEEDED")){
            sendPayItem.setStatus(false);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
            sendPayItem.setHourRetryLimitExceededCount(sendPayItem.getHourRetryLimitExceededCount() + 1);
        } else if (response.contains("DAILY_RETRY_LIMIT_EXCEEDED")){
            sendPayItem.setStatus(false);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
            sendPayItem.setDayRetryLimitExceededCount(sendPayItem.getDayRetryLimitExceededCount() + 1);
        } else if (response.contains("USER_NOT_ELIGIBLE")){
            sendPayItem.setStatus(false);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
            sendPayItem.setUserNotEligibleCount(sendPayItem.getUserNotEligibleCount() + 1);
        } else {
            sendPayItem.setStatus(false);
            sendPayItem.setTotalNoOfRequestSent(sendPayItem.getTotalNoOfRequestSent() + 1);
            sendPayItem.setUnknownErrorCount(sendPayItem.getUnknownErrorCount() + 1);
        }
        return sendPayItem;
    }
}
