package org.bigbluebutton.web.controllers

import java.security.MessageDigest
import java.util.Map
import javax.servlet.http.Cookie

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
//import org.apache.commons.httpclient.Cookie
import org.apache.commons.lang.StringUtils

import org.bigbluebutton.api.ApiErrors
import org.bigbluebutton.api.ParamsProcessorUtil;

class PlaybackController {

    ParamsProcessorUtil paramsProcessorUtil

    def presentation() {
        logParameters(params)
        //request.headerNames.each{
        //    log.debug(it)
        //}
        //log.debug("-------------------------------------")
        String API_CALL = 'playback'

        String serverURL = grailsApplication.config.accessControlAllowOrigin;
        String securitySalt = grailsApplication.config.securitySalt;

        if ( !params.containsKey('action') || !params.containsKey('version') || !params.containsKey('resource') ) {
            render(view:'error')
        } else {
            // When protected, read the authorization.xml and decide if the user will pass or not
            //File authorization = new File()
            // Look for the authorization.xml into "published/${meetingId}"
            // if file was found 
            //   if !params.containsKey(checksum) || ! validChecksum
            //       return error
            //   else proceed with validating the authorization

            // Prepare the static URL
            String static_resource = "/static-${params.controller}/${params.action}/${params.version}/${params.resource}"
            //String query_string = "?meetingId=${params.meetingId}"
            String query_string = request.queryString != null? "?${request.queryString}": ""

//
//            String secret_word = "secret";
//            Date now = new Date()
//            int now_timestamp = (now.getTime() / 1000)
//
//            Cookie md5_cookie = request.cookies.find{ 'md5' == it.name }
//            Cookie ttl_cookie = request.cookies.find{ 'ttl' == it.name }
//            if( md5_cookie == null || md5_cookie.value == "" || now_timestamp >= Integer.parseInt(ttl_cookie.value) ) {
//                log.debug("Creating new cookie");
//                // Calculate $arg_ttl
//                int ttl = now_timestamp + 3600
//
//                // Calculate $arg_md5
//                String message = secret_word+ttl
//                MessageDigest md = MessageDigest.getInstance("MD5"); 
//                String md5 = new String(Base64.encodeBase64(md.digest(message.getBytes("UTF-8"))));
//
//                md5_cookie = new Cookie("md5", ""+md5)
//                ttl_cookie = new Cookie("ttl", ""+ttl)
//            
//            } else {
//                log.debug("Reusing cookie");
//            }
//            log.debug("now: "+now_timestamp);
//            log.debug("cookie: "+ttl_cookie.value.toString());
//            log.debug(""+md5_cookie.value.toString());
//            response.addCookie(md5_cookie)
//            response.addCookie(ttl_cookie)
            
            //Add parameters for authentication
            String static_url = "${serverURL}${static_resource}"
            static_url += query_string
            //static_url += "&md5="+md5_cookie.value
            //static_url += "&ttl="+ttl_cookie.value

            //Execute the redirect
            log.debug(static_url)
            response.setHeader('X-Accel-Redirect', "${static_resource}")
            response.setHeader('X-Accel-Mapping', '')
            response.sendRedirect(static_url)
            //redirect(url: static_url)
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
        render(view:'error')
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
