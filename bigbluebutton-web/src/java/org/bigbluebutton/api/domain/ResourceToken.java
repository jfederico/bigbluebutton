/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
* 
* Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 3.0 of the License, or (at your option) any later
* version.
* 
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
*
*/

package org.bigbluebutton.api.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceToken {
  private static Logger log = LoggerFactory.getLogger(ResourceToken.class);
  
  private String  tokenId;
  private String  resourceId;
  private long    createTime;
  private long    useTime;
  
  public ResourceToken(String resourceId) {
    this.tokenId = UUID.randomUUID().toString();
    this.resourceId = resourceId;
    this.createTime = System.currentTimeMillis();
  }

  public String getTokenId() {
    return tokenId;
  }

  public String getResourceId() {
    return resourceId;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public boolean isExpired(int ttl) {
    long now = System.currentTimeMillis();
    return (now - ttl * 1000) >= this.createTime;
  }
  
  public boolean isUsed() {
    return (this.useTime != 0);
  }
  
  public void setUsed() {
    this.useTime = System.currentTimeMillis();
  }
}