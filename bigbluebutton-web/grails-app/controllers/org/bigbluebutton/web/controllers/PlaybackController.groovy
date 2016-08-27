package org.bigbluebutton.web.controllers

import java.security.MessageDigest
import java.util.ArrayList
import java.util.Map
import javax.servlet.http.Cookie

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang.StringUtils

import org.bigbluebutton.api.ApiErrors
import org.bigbluebutton.api.MeetingService
import org.bigbluebutton.api.OnetimeURLResourceTokenManager
import org.bigbluebutton.api.ParamsProcessorUtil
import org.bigbluebutton.api.domain.ResourceToken
import org.bigbluebutton.api.domain.Recording

class PlaybackController {

    MeetingService meetingService
    ParamsProcessorUtil paramsProcessorUtil
    OnetimeURLResourceTokenManager onetimeURLResourceTokenManager

    def presentation() {
        logParameters(params)
        String API_CALL = 'playback'

        String recordingID
        Recording recording
        String query_string = ""
        ApiErrors errors = new ApiErrors()

        if ( !params.containsKey('action') || !params.containsKey('version') || !params.containsKey('resource') || params.get('resource') == "" ) {
            errors.setError("generalError","The URL is incorrect");
            render(view:'error')

        } else if ( params.get("resource") == "playback.html") {
            if ( params.containsKey("token") ) {
                // It is token based
                ResourceToken resourceToken = onetimeURLResourceTokenManager.lookupResourceToken(params.get("token"))
                if ( resourceToken == null ) { // Not found
                    errors.setError("generalError","resource token was not found")
                } else if ( resourceToken.isExpired(onetimeURLResourceTokenManager.getTtl()) ) {
                    errors.setError("generalError","token has expired")
                } else if ( resourceToken.isUsed() && session[resourceToken.getTokenId()] == null  ) {
                  errors.setError("generalError","Access denied. Not the owner of the token")
                } else {
                    recordingID = resourceToken.getResourceId()
                    Map<String,Recording> recordings = meetingService.getRecordings(new ArrayList<String>([recordingID]), new ArrayList<String>()) // by default only published/unpublished
                    if ( recordings.isEmpty() || !recordings.containsKey(recordingID) ) {
                        errors.setError("generalError","the recording corresponding to this token was not found")
                    } else if ( (recording = recordings.get(recordingID)).getState() != Recording.STATE_PUBLISHED ) {
                        errors.setError("generalError","the recording corresponding to this token is not published")
                    } else {
                        // Use the token
                        resourceToken.setUsed();
                        session[resourceToken.getTokenId()] = true;
                    }
                }
            } else {
              if ( params.containsKey("meetingId") ) {
                recordingID = params.get("meetingId")
              } else if ( params.containsKey("meetingID") ) {
                recordingID = params.get("meetingID")
              } else if ( params.containsKey("meetingid") ) {
                recordingID = params.get("meetingid")
              } else {
                errors.setError("generalError","the request must include a valid token or a valid recording ID")
              }
              if (!errors.hasErrors()) {
                Map<String,Recording> recordings = meetingService.getRecordings(new ArrayList<String>([recordingID]), new ArrayList<String>()) // by default only published/unpublished
                if ( recordings.isEmpty() || !recordings.containsKey(recordingID) ) {
                    errors.setError("generalError","this recording was not found")
                } else {
                  recording = recordings.get(recordingID)
                  if ( recording.getState() != Recording.STATE_PUBLISHED ) {
                    errors.setError("generalError","this recording is not published")
                  } else {
                    String mode = recording.getMetadata("mode")
                    if (  mode != null && mode != "unprotected"  ) {
                      errors.setError("generalError","this recording is protected, token must be provided")
                    }
                  }
                }
              }
            }

            if (!errors.hasErrors()) {
                // Prepare query string
                query_string = "?meetingId=${recording.getId()}"
            }

        } else {
            // Prepare query string
            query_string = request.queryString != null? "?${request.queryString}": ""
        }

        if (errors.hasErrors()) {
          respondWithErrors(errors)
          return
        } else {
          // Prepare the static URL
          String static_resource = "/static-${params.controller}/${params.action}/${params.version}/${params.resource}"
          String static_url = "${grailsApplication.config.accessControlAllowOrigin}${static_resource}${query_string}"
          //log.debug(static_url)

          //Execute the redirect
          response.setHeader('X-Accel-Redirect', "${static_resource}")
          response.setHeader('X-Accel-Mapping', '')
          response.sendRedirect(static_url)
        }
    }

    private boolean preprocessRequest(API_CALL, Map<String,String> params_required) {
        ApiErrors errors = new ApiErrors()

        // Do we have a checksum? If none, complain.
        if (StringUtils.isEmpty(params.checksum)) {
            errors.missingParamError("checksum");
            respondWithErrors(errors)
            return false
        }

        // Do we have all required params? If any is missing, complain.
        for (Map.Entry<String, String> entry : params_required.entrySet()) {
            if (StringUtils.isEmpty(entry.getValue())) {
                errors.missingParamError(entry.getKey());
                respondWithErrors(errors)
                return false
            }
        }

        // Do we agree on the checksum? If not, complain.
        if (! paramsProcessorUtil.isChecksumSame(API_CALL, params.checksum, request.getQueryString())) {
            errors.checksumError()
            respondWithErrors(errors)
            return false
        }

        return true
    }

    private void respondWithErrors(errorList) {
        ArrayList errors = errorList.getErrors();
        Iterator itr = errors.iterator();
        while (itr.hasNext()){
          String[] error = (String[]) itr.next();
          log.debug error[0] + ": " + error[1] 
        }
        render(view: "error", model: ['errors': errors])
    }


    private void logParameters(Object params, boolean debug = false) {
        def divider = "----------------------------------"
        Map<String, String> ordered_params = new LinkedHashMap<String, String>(params)
        ordered_params = ordered_params.sort {it.key}
        if( debug ) log.debug divider else log.info divider
        for( param in ordered_params ) {
            if( debug ) {
                log.debug "${param.getKey()}=${param.getValue()}"
            } else {
                log.info "${param.getKey()}=${param.getValue()}"
            }
        }
        if( debug ) log.debug divider else log.info divider
    }
}
