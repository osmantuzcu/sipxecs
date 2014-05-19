package org.sipfoundry.openfire.provider;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Date;

import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoom;
import org.jivesoftware.openfire.muc.spi.MultiUserChatServiceImpl;
import org.jivesoftware.openfire.provider.MultiUserChatProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@SuppressWarnings("static-method")
public class MucProviderTest extends BaseMongoTest {

    @Before
    public void setup() {
        DBObject srvObj = new BasicDBObject();

        srvObj.put("_id", 1L);
        srvObj.put("subdomain", "testDomain");
        srvObj.put("description", "used for testing");
        srvObj.put("isHidden", false);

        getOpenfireDb().getCollection("ofMucService").insert(srvObj);
    }

    @After
    public void teardown() {
        getOpenfireDb().getCollection("ofMucRoom").remove(new BasicDBObject());
        getOpenfireDb().getCollection("ofMucService").remove(new BasicDBObject());
    }

    @Test
    public void testRoomWrite() {
        LocalMUCRoom room1 = new LocalMUCRoom();
        LocalMUCRoom room2 = new LocalMUCRoom();
        MultiUserChatService service = new MultiUserChatServiceImpl("testDomain", "", false);
        Date dummy = new Date();

        room1.setID(1L);
        room1.setSubject("subj1");
        room1.setMUCService(service);
        room1.setCreationDate(dummy);
        room1.setModificationDate(dummy);
        room2.setID(2L);
        room2.setSubject("subj2");
        room2.setMUCService(service);
        room2.setCreationDate(dummy);
        room2.setModificationDate(dummy);

        MultiUserChatProvider provider = new MongoMucProvider();

        provider.saveToDB(room1);
        provider.saveToDB(room2);
        assertEquals(2, getOpenfireDb().getCollection("ofMucRoom").count());
    }

    @Test
    public void testRoomRead() {
        MultiUserChatService service = new MultiUserChatServiceImpl("testDomain", "", false);
        LocalMUCRoom room = new LocalMUCRoom(service, "testRoom", null);
        LocalMUCRoom roomFromDB = new LocalMUCRoom(service, "testRoom", null);
        Date dummy = new Date();

        room.setID(1L);
        room.setSubject("subj1");
        room.setCreationDate(dummy);
        room.setModificationDate(dummy);

        MultiUserChatProvider provider = new MongoMucProvider();

        provider.saveToDB(room);
        roomFromDB.setID(1L);

        provider.loadFromDB(roomFromDB);

        assertEquals("subj1", roomFromDB.getSubject());
        assertEquals(dummy.getTime(), roomFromDB.getCreationDate().getTime());
    }

    @Test
    public void testRoomReadAll() {
        LocalMUCRoom room1 = new LocalMUCRoom();
        LocalMUCRoom room2 = new LocalMUCRoom();
        MultiUserChatService service = new MultiUserChatServiceImpl("testDomain", "", false);
        Date dummy = new Date();

        room1.setID(1L);
        room1.setSubject("subj1");
        room1.setMUCService(service);
        room1.setCreationDate(dummy);
        room1.setModificationDate(dummy);
        room2.setID(2L);
        room2.setSubject("subj2");
        room2.setMUCService(service);
        room2.setCreationDate(dummy);
        room2.setModificationDate(dummy);

        MultiUserChatProvider provider = new MongoMucProvider();

        provider.saveToDB(room1);
        provider.saveToDB(room2);

        Collection<LocalMUCRoom> rooms = provider.loadRoomsFromDB(service, dummy, null);

        assertEquals(2, rooms.size());
    }

}
