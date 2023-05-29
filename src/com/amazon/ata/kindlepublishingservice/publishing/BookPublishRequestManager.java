package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dagger.DataAccessModule;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BookPublishRequestManager {
    private Queue<BookPublishRequest> requests;
    private BookPublishTask bookPublishTask;

    //inject the catalog dao with dagger insteads
    @Inject
    public BookPublishRequestManager(CatalogDao catalogDao, PublishingStatusDao publishingStatusDao){
        requests = new ConcurrentLinkedQueue<>();
        DataAccessModule data = new DataAccessModule();
        bookPublishTask = new BookPublishTask(this, catalogDao, publishingStatusDao);
    }
    public void addBookPublishRequest(BookPublishRequest request){
        requests.add(request);
    }

    public void startRequest(){
        bookPublishTask.run();;
    }

    public BookPublishRequest getBookPublishRequestToProcess(){
        if (!requests.isEmpty()) {
            return requests.poll();
        }
        return null;
    }
}
