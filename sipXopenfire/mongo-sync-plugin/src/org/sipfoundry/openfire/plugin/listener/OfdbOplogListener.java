package org.sipfoundry.openfire.plugin.listener;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sipfoundry.openfire.plugin.MongoOperation;

import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class OfdbOplogListener extends MongoOplogListener {
    private static final String WATCHED_NAMESPACE = "openfiredb.ofMucRoom";
    private static final List<MongoOperation> WATCHED_OPERATIONS = Arrays.asList(MongoOperation.INSERT,
            MongoOperation.UPDATE, MongoOperation.DELETE);

    @Override
    protected DBObject buildOpLogQuery() {
        return QueryBuilder.start(NAMESPACE).is(WATCHED_NAMESPACE).get();
    }

    @Override
    protected Collection<MongoOperation> getWatchedOperations() {
        return WATCHED_OPERATIONS;
    }
}
