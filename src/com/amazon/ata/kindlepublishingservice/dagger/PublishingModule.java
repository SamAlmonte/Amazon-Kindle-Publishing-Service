package com.amazon.ata.kindlepublishingservice.dagger;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishTask;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublisher;

import com.amazon.ata.kindlepublishingservice.publishing.NoOpTask;
import dagger.Module;
import dagger.Provides;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Singleton;

@Module
public class PublishingModule {

    @Provides
    @Singleton
    public BookPublisher provideBookPublisher(ScheduledExecutorService scheduledExecutorService, CatalogDao catalogDao, PublishingStatusDao publishingStatusDao) {
        DataAccessModule data = new DataAccessModule();
        return new BookPublisher(scheduledExecutorService, new BookPublishTask(new BookPublishRequestManager(catalogDao, publishingStatusDao), new CatalogDao(data.provideDynamoDBMapper()), new PublishingStatusDao(data.provideDynamoDBMapper())));
    }

    @Provides
    @Singleton
    public ScheduledExecutorService provideBookPublisherScheduler() {
        return Executors.newScheduledThreadPool(1);
    }
}
