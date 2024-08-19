package io.github.poshjosh.ratelimiter.tests.client;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component
public class RandomUserAgents {

    // Source: https://github.com/monperrus/crawler-user-agents/blob/master/crawler-user-agents.json
    private static final String [] BOT_USER_AGENTS = {
            "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
            "Mozilla/5.0 (Windows Phone 8.1; ARM; Trident/7.0; Touch; rv:11.0; IEMobile/11.0; NOKIA; Lumia 530) like Gecko (compatible; adidxbot/2.0; +http://www.bing.com/bingbot.htm)",
            "Mozilla/5.0 (compatible; Yahoo! Slurp/3.0; http://help.yahoo.com/help/us/ysearch/slurp)",
            "WGETbot/1.0 (+http://wget.alanreed.org)",
            "Wget/1.14 (linux-gnu)",
            "LinkedInBot/1.0 (compatible; Mozilla/5.0; Jakarta Commons-HttpClient/3.1 +http://www.linkedin.com)",
            "Python-urllib/3.7",
            "python-requests/2.9.2",
            "Python/3.9 aiohttp/3.7.3",
            "2Bone_LinkChecker/1.0 libwww-perl/6.03",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/605.1.16 (KHTML, like Gecko; compatible; Friendly_Crawler/2.0) Chrome/120.0.6099.217 Safari/605.1.15/Nutch-1.20-SNAPSHOT",
            "phpcrawl",
            "adidxbot/1.1 (+http://search.msn.com/msnbot.htm)"

    };
    private static final Random random = new Random();
    private static final Map<String, String> ipToUserAgent = new HashMap<>();

    private RandomUserAgents() { }

    /**
     * Generate a random user agent string.
     * @param remoteAddress The IP address of the client
     * @param probabilityOfBot Value between 0 and 1 (inclusive), that determines
     *                         the probability of returning a bot user agent.
     * @return The random user agent string
     */
    public static String getOrGenerate(String remoteAddress, float probabilityOfBot) {
        String userAgent = ipToUserAgent.get(remoteAddress);
        if (userAgent != null) {
            return userAgent;
        }
        userAgent = generate(remoteAddress, probabilityOfBot);
        ipToUserAgent.put(remoteAddress, userAgent);
        return userAgent;
    }

    //@VisibleForTesting
    static void reset() {
        ipToUserAgent.clear();
    }

    private static String generate(String remoteAddress, float probabilityOfBot) {
        if (probabilityOfBot < 0 || probabilityOfBot > 1) {
            throw new IllegalArgumentException("probabilityOfBot = " + probabilityOfBot
                    + ". Must be between zero and one (inclusive)");
        }
        if (Math.random() < probabilityOfBot) {
            final int index = random.nextInt(BOT_USER_AGENTS.length);
            return BOT_USER_AGENTS[index];
        }
        return "RateLimiterTests/1.0 (Macintosh; Intel Mac OS X 10_15_7) MessageClientApplication/"
                + UUID.nameUUIDFromBytes(remoteAddress.getBytes());
    }

    //@VisibleForTesting
    static boolean isRegisteredBot(String userAgent) {
        for (String botUserAgent : BOT_USER_AGENTS) {
            if (userAgent.equals(botUserAgent)) {
                return true;
            }
        }
        return false;
    }
}
