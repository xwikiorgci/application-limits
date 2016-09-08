/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.limits.rest;

import java.io.StringWriter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.limits.XWikiLimitsConfiguration;
import org.xwiki.rest.XWikiRestComponent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @version $Id: $
 */
@Component
@Named("org.xwiki.contrib.limits.rest.LimitsResource")
@Singleton
@Path("/limits")
public class LimitsResource implements XWikiRestComponent
{
    @Inject
    private XWikiLimitsConfiguration configuration;

    @Inject
    private Logger logger;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLimits() throws Exception
    {
        return Response.ok().entity(toJson(configuration)).build();
    }

    @POST
    public Response reload()
    {
        try {
            configuration.reload();
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Failed to reload the configuration of the XWiki Limits Application.", e);
            return Response.serverError().build();
        }
    }

    private String toJson(Object object) throws Exception
    {
        StringWriter stringWriter = new StringWriter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(stringWriter, object);
        return stringWriter.toString();
    }
}
