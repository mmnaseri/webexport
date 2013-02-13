package com.agileapes.webexport.event;

import com.agileapes.webexport.model.PageModel;

import java.util.Collection;

/**
 * This event is fired when the application's work is done, and all the states have been
 * visited or decided to be irrelevant. In this case, the worker threads are all closed,
 * and the application is left with a pool of all the collected page meta data.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/2/13, 16:38)
 */
public class BeforeShutdownEvent extends CrawlerEvent {

    private final Collection<PageModel> models;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     * @param models the models collected so far in the application
     */
    public BeforeShutdownEvent(Object source, Collection<PageModel> models) {
        super(source);
        this.models = models;
    }

    public Collection<PageModel> getModels() {
        return models;
    }

}