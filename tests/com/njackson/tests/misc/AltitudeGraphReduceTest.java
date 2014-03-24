package com.njackson.tests.misc;

        import com.njackson.util.AltitudeGraphReduce;
        import junit.framework.Assert;
        import org.junit.After;
        import org.junit.Before;
        import org.junit.Test;

        import static junit.framework.TestCase.assertTrue;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.njackson.MainActivityTest \
 * com.njackson.tests/android.test.InstrumentationTestRunner
 */
public class AltitudeGraphReduceTest {

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
        AltitudeGraphReduce.getInstance().restData();
    }

    @Test
    public void testMinAltitude() {

        AltitudeGraphReduce alt = AltitudeGraphReduce.getInstance();
        int minAlt = 100;
        int maxAlt = 200;

        for (int n = minAlt; n != maxAlt + 10; n += 10) {
            alt.addAltitude(n, 0, 0);
        }

        assertTrue(
                "Min Altitude should be " + minAlt + " value: " + alt.getMin(),
                minAlt == alt.getMin());

    }

    @Test
    public void testMaxAltitude() {

        AltitudeGraphReduce alt = AltitudeGraphReduce.getInstance();
        int minAlt = 100;
        int maxAlt = 200;

        for (int n = minAlt; n != maxAlt + 10; n += 10) {
            alt.addAltitude(n, 0, 0);
        }

        assertTrue(
                "Max Altitude should be " + maxAlt + " value: " + alt.getMax(),
                maxAlt == alt.getMax());

    }

    @Test
    public void testCacheSize() throws InterruptedException {
        AltitudeGraphReduce alt = AltitudeGraphReduce.getInstance();
        int binInterval = 100;
        alt.setBinInterval(binInterval);

        int minAlt = 100;
        int maxAlt = 300;
        int elapsedTime = 0;

        for (int n = minAlt; n != maxAlt; n += 10) {
            alt.addAltitude(n, elapsedTime, 0);
            elapsedTime += binInterval;
        }

        int cacheSize = alt.getCache().size();
        int expectedSize = ((maxAlt - minAlt) / 10);
        assertTrue(
                "Cache should contain " + expectedSize + " items contains " + cacheSize,
                cacheSize == expectedSize);
    }

    @Test
    public void testBinData() throws InterruptedException {
        AltitudeGraphReduce alt = AltitudeGraphReduce.getInstance();
        int binInterval = 100;
        alt.setBinInterval(binInterval);

        int minAlt = 100;
        int maxAlt = 3000;
        int elapsedTime = 0;

        for (int n = minAlt; n != maxAlt; n += 10) {
            alt.addAltitude(n, elapsedTime, 0);
            elapsedTime += 100;
        }

        int[] graphData = alt.getGraphData();
        assertTrue("Graph contains 14 bins", graphData.length == 14);

    }
}
