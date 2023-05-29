package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public CatalogItemVersion removeBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        } else {
            book.setInactive(true);
        }
        dynamoDbMapper.save(book);

        return book;
    }

    public void validateBookExists(String bookId){
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if(book == null)
            throw new BookNotFoundException("The book does not exist");
    }

    public boolean validateBookExists1(String bookId){
        CatalogItemVersion book = getBookFromCatalog(bookId);
        if(book == null)
            return false;
        return true;
    }

    public CatalogItemVersion createOrUpdateBook(BookPublishRequest bookPublishRequest){

        CatalogItemVersion newBook = new CatalogItemVersion();
        if(bookPublishRequest.getBookId() == null){
            //book does not exists
            newBook.setBookId(KindlePublishingUtils.generateBookId());
            newBook.setVersion(1);
        }else {
            //book exists so we increment the version
            CatalogItemVersion oldBook = getLatestVersionOfBook(bookPublishRequest.getBookId());
            oldBook.setInactive(true);
            dynamoDbMapper.save(oldBook);
            newBook.setBookId(bookPublishRequest.getBookId());
            newBook.setVersion(oldBook.getVersion()+1);
        }
        newBook.setAuthor(bookPublishRequest.getAuthor());
        newBook.setText(bookPublishRequest.getText());
        newBook.setGenre(bookPublishRequest.getGenre());
        newBook.setTitle(bookPublishRequest.getTitle());
        newBook.setInactive(false);
        dynamoDbMapper.save(newBook);
        return newBook;
    }


}
