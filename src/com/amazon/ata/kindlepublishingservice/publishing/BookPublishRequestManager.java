package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;

import java.util.LinkedList;
import java.util.Queue;

public class BookPublishRequestManager {
    private Queue<BookPublishRequest> requests;
    private CatalogDao catalogDao;

    public BookPublishRequestManager(){
        requests = new LinkedList<>();
    }
    public void addBookPublishRequest(BookPublishRequest request){
        requests.add(request);
    }

    public BookPublishRequest getBookPublishRequestToProcess(){
        if (!requests.isEmpty())
            return requests.poll();
        return null;
    }
}
