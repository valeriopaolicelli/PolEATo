package com.mad.poleato.LocationService;

public final class Constants {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 8;
    // Update frequency in milliseconds
    protected static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 5;
    // A fast frequency ceiling in milliseconds
    protected static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    protected static final float DISPLACEMENT = 30;

    protected static final int LOCATION_REQUEST_FREQUENCY = 5000;
    /**
     * Suppress default constructor for noninstantiability
     */
    private Constants() {
        throw new AssertionError();
    }
}
