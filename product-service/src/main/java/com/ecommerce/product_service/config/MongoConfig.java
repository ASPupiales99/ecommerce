package com.ecommerce.product_service.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

  @Value("${spring.mongodb.host}")
  private String host;

  @Value("${spring.mongodb.port}")
  private int port;

  @Value("${spring.mongodb.database}")
  private String database;

  @Value("${spring.mongodb.username}")
  private String username;

  @Value("${spring.mongodb.password}")
  private String password;

  @Value("${spring.mongodb.authentication-database}")
  private String authDatabase;

  @Override
  protected String getDatabaseName() {
    return database;
  }

  @Override
  @Bean
  public MongoClient mongoClient() {
    MongoCredential credential =
        MongoCredential.createCredential(username, authDatabase, password.toCharArray());

    String connectionString = String.format("mongodb://%s:%d", host, port);

    MongoClientSettings settings =
        MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .credential(credential)
            .build();

    return MongoClients.create(settings);
  }
}
