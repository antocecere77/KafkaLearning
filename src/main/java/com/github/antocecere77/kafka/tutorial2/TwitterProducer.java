package com.github.antocecere77.kafka.tutorial2;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    String consumerKey = "6UNv36RppT1uPOhZ8tOQ4mdKQ";
    String consumerSecret = "WOQevpQcrPJQ2Dw0VAIvPsSVGvnyc02B9J1joEm9hZSBg2fyuz";
    String token = "2919693094-aqJ3lqtAmr2o5uO6EgGKhm8QETi6TL57I6DtSbe";
    String secret = "pZTuaVDw3HyT97URMHsKEZMbioowUfnUE3qlmtUaKZSBi";

    Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {

        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);
        //crete twitter client
        Client client = createTwitterClient(msgQueue);
        client.connect();

        //create kafka producer

        //loop to send tweets to kafka
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }
            if(msg!=null) {
                logger.info(msg);
            }
        }

        logger.info("End of application");
    }

    private Client createTwitterClient(BlockingQueue<String> msgQueue) {

        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        List<String> terms = Lists.newArrayList("bitcoin");
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();
        // Attempts to establish a connection.

        return hosebirdClient;
    }
}
