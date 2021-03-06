/**
 * CollectionService.java
 * ====================================
 * The class receives all incoming transactions from
 * Beyonic and sends them to the payment gateway for processing.
 * @author vladimirfomene
 */

package org.mifos.beyonicIntegration.service.BeyonicService;

import org.mifos.beyonicIntegration.service.BeyonicService.domain.CollectionNotification;
import org.mifos.beyonicIntegration.service.MifosGatewayService.GatewayService;
import org.mifos.beyonicIntegration.service.MifosGatewayService.domain.InboundRequest;
import org.mifos.beyonicIntegration.service.MifosGatewayService.domain.Status;
import org.mifos.beyonicIntegration.util.MfiProperties;
import org.mifos.beyonicIntegration.util.StatusCategory;
import org.mifos.beyonicIntegration.util.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;


@RestController
public class CollectionService {

    @Autowired
    private MfiProperties mfiProperties;

    @Autowired
    private GatewayService gatewayService;

    private Status receptionStatus;

    @RequestMapping(value = "/collections", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Status> receiveCollection(@RequestBody CollectionNotification notif){
        Status collectionReceptionStatus = null;

        System.out.println(notif.getData().toString());

        if(notif != null){
            collectionReceptionStatus = new Status();
            collectionReceptionStatus.setCode(String.valueOf(TransactionStatus.REQUEST_RECEPTION_SUCCESS_CODE));
            collectionReceptionStatus.setDescription(TransactionStatus.REQUEST_RECEPTION_SUCCESS);
            collectionReceptionStatus.setStatusCategory(StatusCategory.MMP_CATEGORY);
        }
        try {
            receptionStatus = sendCollectionToGateway(notif, mfiProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(collectionReceptionStatus, HttpStatus.OK);

    }


    private Status sendCollectionToGateway(CollectionNotification notif, MfiProperties mfiProperties) throws IOException{

        //Get Fineract Account number
        System.out.println(mfiProperties.getMfiName());
        String accNumber = notif.getData().getReference().substring(mfiProperties.getMfiName().length());

        //build inbound request from collection object
        InboundRequest inboundReq = new InboundRequest();
        inboundReq.setAmount(notif.getData().getAmount());
        inboundReq.setSourceRef(notif.getData().getPhonenumber());
        inboundReq.setDestinationRef(mfiProperties.getPhoneNumber());
        inboundReq.setPaymentMethod("mobile money");
        inboundReq.setPaymentMethodType("Beyonic");
        inboundReq.setFineractAccNo(accNumber);

        System.out.println(inboundReq.toString());


        //Send request to payment gateway via the retrofit service
        return gatewayService.sendInboundRequest(inboundReq, mfiProperties.getMfiName());

    }
}
