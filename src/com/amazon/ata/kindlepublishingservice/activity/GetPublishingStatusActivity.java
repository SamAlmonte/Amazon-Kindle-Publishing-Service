package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GetPublishingStatusActivity {

    private PublishingStatusDao publishingStatusDao;
    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {
        List<PublishingStatusItem> itemList =  publishingStatusDao.getPublishingStatuses(publishingStatusRequest.getPublishingRecordId());
        if(itemList.isEmpty())
            return null;
        List<PublishingStatusRecord> returnList = new ArrayList<>();
        for (PublishingStatusItem item: itemList){
            PublishingStatusRecord record =  PublishingStatusRecord.builder().withStatus(item.getStatus().toString()).withBookId(item.getBookId().toString()).withStatusMessage(item.getStatusMessage().toString()).build();
            returnList.add(record);
        }
        return GetPublishingStatusResponse.builder().withPublishingStatusHistory(returnList).build();
    }
}
