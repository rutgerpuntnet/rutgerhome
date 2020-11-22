package net.rutger.home.service;

/**
 * This service handles the actual water supply using an electrical valve.
 * There are multiple implementations of this service
 * - The 'old' scenario where a separate arduino switches a relay that has the water valve connected to it
 * - The 'new' scenario where the relay is connected directly to the raspberry pi where this application is running
 * - A mock implementation that only logs it's actions, so we can run this application on a local machine in order to test.
 *
 */
public interface WaterValveService {

    void openLowerValve(long seconds);

    void openUpperValve(long seconds);

}
