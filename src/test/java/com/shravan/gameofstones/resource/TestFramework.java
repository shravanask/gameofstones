package com.shravan.gameofstones.resource;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import com.mongodb.DB;
import com.shravan.gameofstones.core.Mongodb;

public class TestFramework {

    //Used to check if mongo is running
    private Mongodb mongodb = null;

    @Before
    public void setup() {

        //make sure to use the test datastore
        Mongodb.IS_TEST = true;
        //try to connect to mongo and clear the for old data
        mongodb = Mongodb.getInstance();
        if (mongodb != null) {
            DB database = mongodb.getJongo().getDatabase();
            if (database != null) {
                database.dropDatabase();
            }
        }
        String message = "Is mongo server running??.";
        Assert.assertThat(message, mongodb, Matchers.notNullValue());
    }

    @After
    public void tearDown() {

        //make sure to reset the database
        Mongodb.IS_TEST = false;
        //clear the for old data
        if (mongodb != null) {
            mongodb.getJongo().getDatabase().dropDatabase();
        }
    }
}
