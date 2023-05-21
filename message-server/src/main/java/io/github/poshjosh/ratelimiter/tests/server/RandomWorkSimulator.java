package io.github.poshjosh.ratelimiter.tests.server;

public class RandomWorkSimulator {

    public static void main(String... args) {
        simulateSomeWork(50_000);
    }

    static void simulateSomeWork(int iterations) {
//        long tb4 = System.currentTimeMillis();
//        long mb4 = Usage.availableMemory();
        for(int i = 0; i < iterations; i++) {
            // 50k = 65 millis, 8MB
            java.util.UUID.randomUUID();
            // 500k = 250 millis, 65MB
            //java.util.UUID.randomUUID();
            // 500k = 175 millis, 17.5MB
            //Pattern.compile(".").matcher(Long.toHexString(System.currentTimeMillis())).matches();
            // 500k = 5 millis, 5 MB
            //new Object();
            // 500k = 110 millis, 11 MB
            //new Object().toString();
        }
//        System.out.println("Spent time: " + (System.currentTimeMillis() - tb4) +
//                ", memory: " + (mb4 - Usage.availableMemory()));
    }
}
