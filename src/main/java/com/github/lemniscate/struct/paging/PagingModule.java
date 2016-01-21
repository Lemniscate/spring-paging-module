package com.github.lemniscate.struct.paging;

import com.fasterxml.jackson.databind.module.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by dave on 7/8/15.
 */
public class PagingModule extends SimpleModule {

    public PagingModule() {
        super("SpringPagingModule");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addAbstractTypeResolver(new SimpleAbstractTypeResolver()
                        .addMapping(Page.class, InMemoryPage.class)
                        .addMapping(Pageable.class, InMemoryPage.InMemoryPageRequest.class)
        );
    }
}
