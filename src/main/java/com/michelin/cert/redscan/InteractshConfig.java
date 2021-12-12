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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * InteractSh Configuration class.
 *
 * @author Maxime ESCOURBIAC
 */
@Configuration
public class InteractshConfig {

  @Value("${interactsh.domain}")
  private String domain;

  /**
   * Default constructor.
   */
  public InteractshConfig() {
  }

  /**
   * InteractSh listener domain.
   *
   * @return InteractSh listener domain.
   */
  public String getDomain() {
    return domain;
  }

  /**
   * InteractSh listener domain.
   *
   * @param domain InteractSh listener domain.
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

}
