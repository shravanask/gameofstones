package com.shravan.gameofstones.resource;

import org.junit.Before;
import com.shravan.gameofstones.core.Mongodb;

public class TestFramework {

    @Before
    public void setup(){
        
        //make sure to use the test datastore
        Mongodb.IS_TEST = true;
        //clear the for old data
        Mongodb.getInstance().getJongo().getDatabase().dropDatabase();
    }
}
