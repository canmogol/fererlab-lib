package com.fererlab.action;

import com.fererlab.cache.Cache;
import com.fererlab.dto.*;
import com.fererlab.map.AuthenticationAuthorizationMap;
import com.fererlab.map.CacheMap;
import com.fererlab.map.ExecutionMap;
import com.fererlab.map.MimeTypeMap;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * acm | 1/16/13
 */
public class ActionHandler {

    private ExecutionMap executionMap = new ExecutionMap();
    private AuthenticationAuthorizationMap authenticationAuthorizationMap = new AuthenticationAuthorizationMap();
    private MimeTypeMap mimeTypeMap = new MimeTypeMap();
    private CacheMap cacheMap = new CacheMap();

    public ActionHandler(URL executionMapFile, URL authenticationAuthorizationMapFile, URL mimeTypeMapFile, URL cacheMapFile) {
        executionMap.readUriExecutionMap(executionMapFile);
        authenticationAuthorizationMap.readAuthenticationAuthorizationMap(authenticationAuthorizationMapFile);
        mimeTypeMap.readMimeTypeMap(mimeTypeMapFile);
        cacheMap.readCacheMap(cacheMapFile);
    }

    public Response runAction(final Request request) {

        // prepare method and action class
        Method method;
        Class<?> actionClass;

        // get the request Method(GET, POST etc.) and URI
        String requestMethod = request.getParams().get(RequestKeys.REQUEST_METHOD.getValue()).getValue().toString();
        String requestURI = request.getParams().get(RequestKeys.URI.getValue()).getValue().toString();

        // URI starting with /_/ indicates it is a resource but not an action
        if (requestURI.startsWith("/_/") && requestURI.lastIndexOf("..") == -1) {

            Map<byte[], String> contentAndExtension = Cache.getContentIfCached(requestURI);
            if (contentAndExtension == null) {
                // request URI is either one of these; xsl, css, js, image, file,
                FileContentHandler fileContentHandler = new FileContentHandler();
                byte[] content = new byte[0];
                try {
                    content = fileContentHandler.getContent(fileContentHandler.getContentPath(), requestURI);
                } catch (FileNotFoundException e) {
                    return new Response(
                            new ParamMap<String, Param<String, Object>>(),
                            request.getSession(),
                            Status.STATUS_NOT_FOUND,
                            ""
                    );
                }
                String extension = fileContentHandler.getFileExtension();

                contentAndExtension = new HashMap<byte[], String>();
                contentAndExtension.put(content, extension);
                Cache.put(requestURI, contentAndExtension);
            }
            Map.Entry<byte[], String> entry = contentAndExtension.entrySet().iterator().next();

            Response response = new Response(
                    new ParamMap<String, Param<String, Object>>(),
                    request.getSession(),
                    Status.STATUS_OK,
                    entry.getKey()
            );
            response.getHeaders().put(
                    ResponseKeys.RESPONSE_TYPE.getValue(),
                    new Param<String, Object>(
                            ResponseKeys.RESPONSE_TYPE.getValue(),
                            mimeTypeMap.get(entry.getValue())
                    )
            );
            return response;
        }

        // remove the forward slash if there is any
        if (requestURI.length() > 1 && requestURI.endsWith("/")) {
            requestURI = requestURI.substring(0, requestURI.length() - 1);
        }

        // define the className and methodName here
        String className = null;
        String methodName = null;
        String templateName = null;

        // first check for the exact match
        // requestMethod    ->      GET
        if (executionMap.containsKey(requestMethod)) {

            // uriExecutionMap contains all the URI -> execution mapping for this request method
            Map<String, Param<String, String>> uriExecutionMap = executionMap.get(requestMethod);

            // requestURI           /welcome        or       /welcome/
            if (uriExecutionMap.containsKey(requestURI) || uriExecutionMap.containsKey(requestURI + "/")) {
                //   com.sample.app.action.MainAction, welcome
                Param<String, String> executionParam = uriExecutionMap.get(requestURI);
                //   com.sample.app.action.MainAction
                className = executionParam.getKey();
                //   welcome
                methodName = executionParam.getValue();
                //   welcome
                templateName = executionParam.getValueSecondary();
            } else if (requestURI.startsWith("/*/")) {
                for (String uri : uriExecutionMap.keySet()) {
                    //  requestURI      /*/all/Product
                    //  uri             /*/all
                    if (requestURI.startsWith(uri)) {
                        //   com.sample.app.action.MainAction, welcome
                        Param<String, String> executionParam = uriExecutionMap.get(uri);
                        //   com.sample.app.action.MainAction
                        className = executionParam.getKey();
                        //   welcome
                        methodName = executionParam.getValue();
                        //   welcome
                        templateName = executionParam.getValueSecondary();
                        // found the class/method
                        break;
                    }
                }
            }
        }

        // if className not found, check the '*' method
        if (className == null) {
            if (executionMap.containsKey("*")) {

                // uriExecutionMap contains all the URI -> execution mapping for '*' request method
                Map<String, Param<String, String>> uriExecutionMap = executionMap.get("*");

                if (uriExecutionMap.containsKey(requestURI) || uriExecutionMap.containsKey(requestURI + "/")) {
                    //   com.sample.app.action.MainAction, welcome
                    Param<String, String> executionParam = uriExecutionMap.get(requestURI);
                    //   com.sample.app.action.MainAction
                    className = executionParam.getKey();
                    //   welcome
                    methodName = executionParam.getValue();
                    //   welcome
                    templateName = executionParam.getValueSecondary();
                } else if (requestURI.startsWith("/*/")) {
                    for (String uri : uriExecutionMap.keySet()) {
                        //  requestURI      /*/all/Product
                        //  uri             /*/all
                        if (requestURI.startsWith(uri)) {
                            //   com.sample.app.action.MainAction, welcome
                            Param<String, String> executionParam = uriExecutionMap.get(uri);
                            //   com.sample.app.action.MainAction
                            className = executionParam.getKey();
                            //   welcome
                            methodName = executionParam.getValue();
                            //   welcome
                            templateName = executionParam.getValueSecondary();
                        }
                    }
                }

                // if still className not found set it to default which is [GET] '/'
                if (className == null) {
                    requestURI = "/";
                    // get the default
                    Param<String, String> executionParam = uriExecutionMap.get(requestURI);
                    //   com.sample.app.action.MainAction
                    className = executionParam.getKey();
                    //   welcome
                    methodName = executionParam.getValue();
                    //   welcome
                    templateName = executionParam.getValueSecondary();
                }
            }

        }


        // check the AuthenticationAuthorizationMap contains requestMethod
        if (authenticationAuthorizationMap.containsKey(requestMethod)
                || authenticationAuthorizationMap.containsKey("*")) {

            // find the user's group names
            String[] groupNamesCommaSeparated = new String[]{"admin"};
            if (request.getSession().containsKey(SessionKeys.GROUP_NAMES.getValue())) {
                groupNamesCommaSeparated = ((String) request.getSession().get(SessionKeys.GROUP_NAMES.getValue())).split(",");
            }

            // authorization flag for user's group
            boolean userAuthorized = false;

            // for this http request method, like GET, POST or PUT
            Map<String, List<String>> uriGroupNames = authenticationAuthorizationMap.get(requestMethod);

            // check this requested uri has any authentication/authorization
            if (uriGroupNames.containsKey(requestURI)) {

                // the user does not have any groups but this uri needs some
                // return STATUS_UNAUTHORIZED and redirect
                if (groupNamesCommaSeparated == null) {
                    ParamMap<String, Param<String, Object>> stringParamParamMap = new ParamMap<String, Param<String, Object>>();
                    Object hostName = request.getHeaders().getValue(RequestKeys.HOST_NAME.getValue());
                    Object hostPort = request.getHeaders().getValue(RequestKeys.HOST_PORT.getValue());
                    Object applicationUri = request.getParams().getValue(RequestKeys.APPLICATION_URI.getValue());
                    String redirectUrl = hostName + ":" + (hostPort != null ? hostPort : "") + "/" + applicationUri;
                    stringParamParamMap.addParam(new Param<String, Object>("Refresh", "0; url=http://" + redirectUrl));
                    return new Response(
                            stringParamParamMap,
                            request.getSession(),
                            Status.STATUS_UNAUTHORIZED,
                            ""
                    );
                }

                // authorized groups for this uri
                List<String> authorizedGroups = uriGroupNames.get(requestURI);

                // user has at least one group, it means user is authenticated
                // if the authorizedGroups contains (*) it means any authenticated client may request this uri
                if (authorizedGroups.contains("*")) {
                    userAuthorized = true;
                }

                // find the required group names
                else {
                    for (String userGroupName : groupNamesCommaSeparated) {
                        if (authorizedGroups.contains(userGroupName)) {
                            userAuthorized = true;
                            break;
                        }
                    }
                }


                // if the user is not authorized return STATUS_UNAUTHORIZED and redirect
                if (!userAuthorized) {
                    return new Response(
                            new ParamMap<String, Param<String, Object>>() {{
                                Object hostName = request.getParams().getValue(RequestKeys.HOST_NAME.getValue());
                                Object hostPort = request.getParams().getValue(RequestKeys.HOST_PORT.getValue());
                                Object applicationUri = request.getParams().getValue(RequestKeys.APPLICATION_URI.getValue());
                                String redirectUrl = hostName + ":" + (hostPort != null ? hostPort : "") + "/" + applicationUri;
                                addParam(new Param<String, Object>("Refresh", "0; url=http://" + redirectUrl));
                            }},
                            request.getSession(),
                            Status.STATUS_UNAUTHORIZED,
                            ""
                    );
                }
            }


            // check for the [*] all http method request map
            if (!userAuthorized) {

                // [*] http request method
                uriGroupNames = authenticationAuthorizationMap.get("*");

                // check this requested uri has any authentication/authorization
                if (uriGroupNames.containsKey(requestURI)) {

                    // the user does not have any groups but this uri needs some
                    // return STATUS_UNAUTHORIZED and redirect
                    if (groupNamesCommaSeparated == null) {
                        return new Response(
                                new ParamMap<String, Param<String, Object>>() {{
                                    Object hostName = request.getParams().getValue(RequestKeys.HOST_NAME.getValue());
                                    Object hostPort = request.getParams().getValue(RequestKeys.HOST_PORT.getValue());
                                    Object applicationUri = request.getParams().getValue(RequestKeys.APPLICATION_URI.getValue());
                                    String redirectUrl = hostName + ":" + (hostPort != null ? hostPort : "") + "" + applicationUri;
                                    addParam(new Param<String, Object>("Refresh", "0; url=http://" + redirectUrl));
                                }},
                                request.getSession(),
                                Status.STATUS_UNAUTHORIZED,
                                ""
                        );
                    }

                    // find the required group names
                    List<String> authorizedGroups = uriGroupNames.get(requestURI);
                    for (String userGroupName : groupNamesCommaSeparated) {
                        if (authorizedGroups.contains(userGroupName)) {
                            userAuthorized = true;
                            break;
                        }
                    }

                    // if the user is not authorized return STATUS_UNAUTHORIZED and redirect
                    if (!userAuthorized) {
                        return new Response(
                                new ParamMap<String, Param<String, Object>>() {{
                                    Object hostName = request.getParams().getValue(RequestKeys.HOST_NAME.getValue());
                                    Object hostPort = request.getParams().getValue(RequestKeys.HOST_PORT.getValue());
                                    Object applicationUri = request.getParams().getValue(RequestKeys.APPLICATION_URI.getValue());
                                    String redirectUrl = hostName + ":" + (hostPort != null ? hostPort : "") + "/" + applicationUri;
                                    addParam(new Param<String, Object>("Refresh", "0; url=http://" + redirectUrl));
                                }},
                                request.getSession(),
                                Status.STATUS_UNAUTHORIZED,
                                ""
                        );
                    }
                }
            }
        }

        if (className != null) {
            // set Class and Method
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                actionClass = Class.forName(className, true, classLoader);
                method = actionClass.getMethod(methodName, Request.class);
                // add template to the request if template exists
                if (templateName != null) {
                    request.getParams().addParam(new Param<String, Object>(RequestKeys.RESPONSE_TEMPLATE.getValue(), templateName));
                }
                // return the response
                return (Response) method.invoke(actionClass.newInstance(), request);
            } catch (Exception e) {
                e.printStackTrace();
                new Response(
                        new ParamMap<String, Param<String, Object>>(),
                        request.getSession(),
                        Status.STATUS_SERVICE_UNAVAILABLE,
                        e.getMessage()
                );
            }
        }

        // something went wrong, return an error message
        return new Response(
                new ParamMap<String, Param<String, Object>>(),
                request.getSession(),
                Status.STATUS_SERVICE_UNAVAILABLE,
                ""
        );

    }

}
