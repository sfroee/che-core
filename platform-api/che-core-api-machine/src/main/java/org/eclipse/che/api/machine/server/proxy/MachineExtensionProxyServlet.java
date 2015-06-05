/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server.proxy;

import com.google.common.io.ByteStreams;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.impl.MachineImpl;
import org.eclipse.che.api.machine.shared.Server;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Routes requests to extension API hosted in machine
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class MachineExtensionProxyServlet extends HttpServlet {
    private static final Pattern EXTENSION_API_URI = Pattern.compile("/api/ext/(?<machineId>[^/]+)/.*");

    private final int extServicesPort;

    private final MachineManager machineManager;

    @Inject
    public MachineExtensionProxyServlet(@Named("machine.extension.api_port") int extServicesPort, MachineManager machineManager) {
        this.extServicesPort = extServicesPort;
        this.machineManager = machineManager;
    }

    // fixme secure request to another's machine

    // todo handle https to http

    // todo remove headers if it's name is in connection headers

    // fixme proxy should ensure that http 1.1 request contains hosts header

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpURLConnection conn = prepareProxyConnection(req);

            try {
                conn.connect();

                setResponse(resp, conn);
            } finally {
                conn.disconnect();
            }

        } catch (NotFoundException e) {
            resp.sendError(SC_NOT_FOUND, "Request can't be forwarded to machine. " + e.getLocalizedMessage());
        } catch (MachineException e) {
            resp.sendError(SC_INTERNAL_SERVER_ERROR, "Request can't be forwarded to machine. " + e.getLocalizedMessage());
        }
    }

    private HttpURLConnection prepareProxyConnection(HttpServletRequest req) throws NotFoundException, MachineException {
        String machineId = getMachineId(req);

        String extensionApiUrl = getExtensionApiUrl(machineId, req);
        try {
            final HttpURLConnection conn = (HttpURLConnection)new URL(extensionApiUrl).openConnection();

            conn.setRequestMethod(req.getMethod());

            copyHeaders(conn, req);

            if ("POST".equals(req.getMethod()) || "PUT".equals(req.getMethod()) || "DELETE".equals(req.getMethod())) {
                if (req.getInputStream() != null) {
                    conn.setDoOutput(true);

                    try (InputStream is = req.getInputStream()) {
                        ByteStreams.copy(is, conn.getOutputStream());
                    }
                }
            }

            return conn;
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage());
        }
    }

    private String getExtensionApiUrl(String machineId, HttpServletRequest req) throws NotFoundException, MachineException {
        final MachineImpl machine = machineManager.getMachine(machineId);
        final Server server = machine.getMetadata().getServers().get(Integer.toString(extServicesPort));
        final StringBuilder url = new StringBuilder("http://")
                .append(server.getAddress())
                .append(req.getRequestURI());
        if (req.getQueryString() != null) {
            url.append("?").append(req.getQueryString());
        }
        return url.toString();
    }

    private void setResponse(HttpServletResponse resp, HttpURLConnection conn) throws MachineException {
        try {
            resp.setStatus(conn.getResponseCode());

            InputStream responseStream = conn.getErrorStream();

            if (responseStream == null) {
                responseStream = conn.getInputStream();
            }

            // copy headers from proxy response to origin response
            for (Map.Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
                for (String headerValue : header.getValue()) {
                    resp.addHeader(header.getKey(), headerValue);
                }
            }

            // copy content of input or error stream from destination response to output stream of origin response
            try (OutputStream os = resp.getOutputStream()) {
                ByteStreams.copy(responseStream, os);
            }
        } catch (IOException e) {
            throw new MachineException(e.getLocalizedMessage());
        }
    }

    private String getMachineId(HttpServletRequest req) throws NotFoundException {
        final Matcher matcher = EXTENSION_API_URI.matcher(req.getRequestURI());
        if (matcher.matches()) {
            return matcher.group("machineId");
        }
        throw new NotFoundException("No machine id is found in request.");
    }

    private void copyHeaders(HttpURLConnection conn, HttpServletRequest request) {
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();

            if (!skipHeader(headerName)) {
                final Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    conn.setRequestProperty(headerName, headerValues.nextElement());
                }
            }
        }
    }

    /**
     * Checks if the header should not be copied by proxy.<br>
     * <a href="http://tools.ietf.org/html/rfc2616#section-13.5.1">RFC-2616 Section 13.5.1</a>
     *
     * @param headerName the header name to check.
     * @return {@code true} if the header should be skipped, false otherwise.
     */
    public static boolean skipHeader(final String headerName) {
        return headerName.equalsIgnoreCase("Connection") ||
               headerName.equalsIgnoreCase("Keep-Alive") ||
               headerName.equalsIgnoreCase("Proxy-Authentication") ||
               headerName.equalsIgnoreCase("Proxy-Authorization") ||
               headerName.equalsIgnoreCase("TE") ||
               headerName.equalsIgnoreCase("Trailers") ||
               headerName.equalsIgnoreCase("Transfer-Encoding") ||
               headerName.equalsIgnoreCase("Upgrade");
    }
}