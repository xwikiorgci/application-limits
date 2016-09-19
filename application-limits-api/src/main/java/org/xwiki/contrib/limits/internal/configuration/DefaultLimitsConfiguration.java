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
package org.xwiki.contrib.limits.internal.configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.limits.LimitsConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.text.StringUtils;

/**
 * Default implementation of {@link LimitsConfiguration}.
 *
 * @version $Id: $
 */
@Component
@Singleton
public class DefaultLimitsConfiguration implements LimitsConfiguration, Initializable
{
    /**
     * Path of the config file. Windows is not supported and we don't care.
     * Visibility is protected and not final instead of private to allow the test class, located in the same package,
     * to overwrite the value.
     */
    protected static Path configFile = Paths.get("/", "etc", "xwiki", "limits.xml");

    private int numberOfUsers;

    private int numberOfWikis;

    private HashMap<DocumentReference, Number> groupLimits = new HashMap<>();

    private HashMap<String, Object> customLimits = new HashMap<>();

    @Override
    public void initialize() throws InitializationException
    {
        try {
            reload();
        } catch (Exception e) {
            throw new InitializationException("Failed to load the configuration of the Limits Application.", e);
        }
    }

    @Override
    public void reload() throws Exception
    {
        Element limitsElem = getLimitsElement();

        numberOfUsers = parseIntFromElement(limitsElem, "number-of-users");
        numberOfWikis = parseIntFromElement(limitsElem, "number-of-wikis");

        parseGroupLimits(limitsElem);

        parseCustomLimits(limitsElem);
    }

    private Document getXMLDocument() throws Exception
    {
        try {
            return (new SAXBuilder()).build(configFile.toFile());
        } catch (JDOMException | IOException e) {
            throw new Exception(
                    String.format(
                            "Failed to parse the configuration file for the Limits Application [%s].",
                            configFile),
                    e);
        }
    }

    private Element getLimitsElement() throws Exception
    {
        // The result cannot be null, otherwise an exception has already been thrown by getXMLDocument().
        return getXMLDocument().getRootElement();
    }


    private int parseIntFromElement(Element element, String childName) throws Exception
    {
        String value = element.getChildTextTrim(childName);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new Exception(
                    String.format("[%s] is not a valid number for the limit of [%s].", value, childName), e);
        }
    }

    private void parseGroupLimits(Element limitsElement) throws Exception
    {
        groupLimits.clear();

        Element groupsElem = limitsElement.getChild("groups");
        if (groupsElem != null) {
            for (Object child : groupsElem.getChildren("limit")) {
                if (child instanceof Element) {
                    Element childElem = (Element) child;
                    String group = StringUtils.trimToNull(childElem.getAttributeValue("group"));
                    if (group != null) {
                        DocumentReference groupReference = new DocumentReference("xwiki", "XWiki", group);
                        String value = childElem.getTextTrim();
                        try {
                            int limit = Integer.parseInt(value);
                            groupLimits.put(groupReference, limit);
                        } catch (NumberFormatException e) {
                            throw new Exception(String.format(
                                    "[%s] is not a valid number for the limit of [%s].", value, group), e);
                        }
                    }
                }
            }
        }
    }

    private void parseCustomLimits(Element limitsElem) throws Exception
    {
        customLimits.clear();

        Element customElem = limitsElem.getChild("custom");
        if (customElem != null) {
            for (Object child : customElem.getChildren("limit")) {
                if (child instanceof Element) {
                    Element childElem = (Element) child;
                    String limitName = StringUtils.trim(childElem.getAttributeValue("name"));
                    String limitType = childElem.getAttributeValue("type");
                    String value = childElem.getTextTrim();
                    if (StringUtils.isNotBlank(limitName)) {
                        if ("long".equals(limitType)) {
                            try {
                                Long limitValue = Long.parseLong(value);
                                customLimits.put(limitName, limitValue);
                            } catch (NumberFormatException e) {
                                throw new Exception(String.format(
                                        "[%s] is not a valid number for the limit [%s].", value, limitName), e);
                            }
                        } else if ("date".equals(limitType)) {
                            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            try {
                                Date date = parser.parse(value);
                                customLimits.put(limitName, date);
                            } catch (ParseException e) {
                                throw new Exception(String.format(
                                        "[%s] is a not a valid date for the limit [%s]. Supported format is "
                                                + "yyyy-MM-dd HH:mm.", value, limitName), e);
                            }
                        } else {
                            throw new Exception(
                                    String.format("Missing attribute \"type\" for the limit [%s].", limitName));
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getTotalNumberOfUsersLimit()
    {
        return numberOfUsers;
    }

    @Override
    public int getWikisNumberLimit()
    {
        return numberOfWikis;
    }

    @Override
    public Map<DocumentReference, Number> getGroupsLimits()
    {
        return Collections.unmodifiableMap(groupLimits);
    }

    @Override
    public Map<String, Object> getCustomLimits()
    {
        return Collections.unmodifiableMap(customLimits);
    }
}
