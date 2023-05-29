package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {

    private final BookPublishRequestManager bookPublishRequestManager;
    private CatalogDao catalogDao;
    private PublishingStatusDao publishingStatusDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager, CatalogDao catalogDao, PublishingStatusDao publishingStatusDao){
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.catalogDao = catalogDao;
        this.publishingStatusDao = publishingStatusDao;
    }


    public KindleFormattedBook getBookPublishRequestToProcess(){
        BookPublishRequest request = bookPublishRequestManager.getBookPublishRequestToProcess();
        if(request == null)
            return null;
//        if(request.getBookId() == null || request.getBookId().isEmpty() ) {
//            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.FAILED, request.getBookId(), "Book id empty or null");
//            throw new BookNotFoundException("Invalid bookid");
//        }
        if(request.getBookId() != null){
            try {
                catalogDao.validateBookExists(request.getBookId());
            } catch (BookNotFoundException e){
                publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.FAILED, request.getBookId(), "Invalid bookId");
                throw new BookNotFoundException("Book does not exist in castalog");
            }
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, request.getBookId(), "Success.");
        } else {
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, null, "Success.");
        }
        CatalogItemVersion catalogItem = catalogDao.createOrUpdateBook(request); //get book Id from here
        //does catalogItem always return a book id?
        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.SUCCESSFUL, catalogItem.getBookId());
        return KindleFormatConverter.format(request);

    }
/*
public CatalogItemVersion getBookPublishRequestToProcess(){
    //BookPublishRequest request = submitBookForPublishingActivity.getBookPublishRequestToProcess();
    BookPublishRequest request = bookPublishRequestManager.getBookPublishRequestToProcess();
    if(request == null){
        return null;
    }
    KindleFormattedBook book;
    CatalogItemVersion version;
    if(request.getBookId() != null && !request.getBookId().isEmpty()){
        //book exists
        try {
            catalogDao.validateBookExists(request.getBookId());
        } catch (BookNotFoundException e){
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.FAILED, request.getBookId(), "Book not found in catalog");
        }

        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, request.getBookId());
        book = KindleFormatConverter.format(request);
        version = catalogDao.createOrUpdateBook(request);
    } else {
        //book is new
        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, request.getBookId());
        book = KindleFormatConverter.format(request);
        version = catalogDao.createOrUpdateBook(request);
    }
    publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.SUCCESSFUL, request.getBookId());
    return version;
}
*/

    @Override
    public void run(){
        try{
            this.getBookPublishRequestToProcess();
        } catch (Exception e){
            throw new RuntimeException("Thrown from run method of BookPublishTask");
        }
    }

}
