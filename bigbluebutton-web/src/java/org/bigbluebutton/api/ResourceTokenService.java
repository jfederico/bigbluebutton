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
package org.bigbluebutton.api;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.RandomStringUtils;
import org.bigbluebutton.api.domain.ResourceToken;
import org.bigbluebutton.web.services.ResourceTokenCleanupTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceTokenService {
    private static Logger log = LoggerFactory.getLogger(ResourceTokenService.class);

    private ResourceTokenCleanupTimerTask resourceTokenCleaner;

    //Attributes
    private int ttl;
    private final ConcurrentMap<String, ResourceToken> resourceTokens;

    //Methods
    public ResourceTokenService() {
        resourceTokens = new ConcurrentHashMap<String, ResourceToken>();
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getTtl() {
        return this.ttl;
    }

    public Map<String, ResourceToken> getAllResourceTokens() {
        return this.resourceTokens;
    }

    public ResourceToken lookupResourceToken(String tokenId){
      ResourceToken token = null;
      if ( this.resourceTokens.containsKey(tokenId) ) {
          token = this.resourceTokens.get(tokenId);
      }
      return token;
    }

    public ResourceToken createResourceToken(String resourceId) {
        ResourceToken token = new ResourceToken(resourceId);
        this.resourceTokens.put(token.getTokenId(), token);
        return token;
    }

    public void destroyResourceToken(String tokenId) {
        this.resourceTokens.remove(tokenId);
    }

    public void purgeResourceTokens() {
        for (AbstractMap.Entry<String, ResourceToken> entry : this.resourceTokens.entrySet()) {
            ResourceToken resourceToken = entry.getValue();
            if ( resourceToken.isExpired(this.ttl) ) {
                this.resourceTokens.remove(resourceToken.getTokenId());
                log.debug("Token expired [" + entry.getKey() + "] was removed");
            }
        }
    }

    public void setResourceTokenCleanupTimerTask(ResourceTokenCleanupTimerTask c) {
        resourceTokenCleaner = c;
        resourceTokenCleaner.setResourceTokenService(this);
        resourceTokenCleaner.start();
    }
}
