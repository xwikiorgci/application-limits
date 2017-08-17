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

import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.limits.LimitsConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id: $
 */
public class DefaultLimitsConfigurationTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultLimitsConfiguration> mocker =
            new MockitoComponentMockingRule<>(DefaultLimitsConfiguration.class);

    @Test
    public void test() throws Exception
    {
        DefaultLimitsConfiguration.configFile = Paths.get(getClass().getResource("/limits1.xml").toURI());
        LimitsConfiguration config = mocker.getComponentUnderTest();

        assertEquals(42, config.getTotalNumberOfUsersLimit());
        assertEquals(12, config.getWikisNumberLimit());

        Map<DocumentReference, Number> limits = config.getGroupsLimits();
        assertEquals(2, limits.size());
        assertEquals(21, limits.get(new DocumentReference("xwiki", "XWiki", "GroupA")));
        assertEquals(72, limits.get(new DocumentReference("xwiki", "XWiki", "GroupB")));

        Map<String, Object> customLimits = config.getCustomLimits();
        assertEquals(3, customLimits.size());
        assertEquals(new Date(1474296600000l), customLimits.get("time"));
        assertEquals(Long.valueOf(36), customLimits.get("number-of-applications"));
        assertEquals("Some string limit (do whatever you want with it)", customLimits.get("custom-string"));

        DefaultLimitsConfiguration.configFile = Paths.get(getClass().getResource("/limits2.xml").toURI());
        config.reload();

        assertEquals(202, config.getTotalNumberOfUsersLimit());
        assertEquals(89, config.getWikisNumberLimit());
        customLimits = config.getCustomLimits();
        assertEquals(0, customLimits.size());

        limits = config.getGroupsLimits();
        assertEquals(2, limits.size());
        assertEquals(722, limits.get(new DocumentReference("xwiki", "XWiki", "GroupA")));
        assertEquals(4, limits.get(new DocumentReference("xwiki", "XWiki", "GroupC")));
        customLimits = config.getCustomLimits();
        assertEquals(0, customLimits.size());
    }

    @Test
    public void testWhenConfigIsEmpty() throws Exception
    {
        DefaultLimitsConfiguration.configFile = Paths.get(getClass().getResource("/limitsError1.xml").toURI());

        InitializationException caught = null;
        try {
            mocker.getComponentUnderTest();
        } catch (ComponentLookupException e) {
            if (e.getCause() instanceof InitializationException) {
                caught = (InitializationException) e.getCause();
            }
        }

        assertNotNull(caught);
        assertEquals("Failed to load the configuration of the Limits Application.", caught.getMessage());
        assertEquals(String.format("Failed to parse the configuration file for the Limits Application [%s].",
                        DefaultLimitsConfiguration.configFile),
                caught.getCause().getMessage());
        assertEquals("Error on line -1: Premature end of file.", caught.getCause().getCause().getMessage());
    }

    @Test
    public void testWhenFieldIsMissing() throws Exception
    {
        DefaultLimitsConfiguration.configFile = Paths.get(getClass().getResource("/limitsError2.xml").toURI());

        InitializationException caught = null;
        try {
            mocker.getComponentUnderTest();
        } catch (ComponentLookupException e) {
            if (e.getCause() instanceof InitializationException) {
                caught = (InitializationException) e.getCause();
            }
        }

        assertNotNull(caught);
        assertEquals("Failed to load the configuration of the Limits Application.", caught.getMessage());
        assertEquals("[null] is not a valid number for the limit of [number-of-users].", caught.getCause().getMessage());
    }

    @Test
    public void testWhenCustomLimitIsUnparsableDate() throws Exception
    {
        DefaultLimitsConfiguration.configFile = Paths.get(getClass().getResource("/limitsError3.xml").toURI());

        InitializationException caught = null;
        try {
            mocker.getComponentUnderTest();
        } catch (ComponentLookupException e) {
            if (e.getCause() instanceof InitializationException) {
                caught = (InitializationException) e.getCause();
            }
        }

        assertNotNull(caught);
        assertEquals("Failed to load the configuration of the Limits Application.", caught.getMessage());
        assertEquals("[hello] is a not a valid date for the limit [time]. Supported format is yyyy-MM-dd HH:mm.",
                caught.getCause().getMessage());
    }

    @Test
    public void testWhenCustomLimitIsUnparsableLong() throws Exception
    {
        DefaultLimitsConfiguration.configFile = Paths.get(getClass().getResource("/limitsError4.xml").toURI());

        InitializationException caught = null;
        try {
            mocker.getComponentUnderTest();
        } catch (ComponentLookupException e) {
            if (e.getCause() instanceof InitializationException) {
                caught = (InitializationException) e.getCause();
            }
        }

        assertNotNull(caught);
        assertEquals("Failed to load the configuration of the Limits Application.", caught.getMessage());
        assertEquals("[hello] is not a valid number for the limit [whatever].",
                caught.getCause().getMessage());
    }

    @Test
    public void testWhenCustomLimitHasNoType() throws Exception
    {
        DefaultLimitsConfiguration.configFile = Paths.get(getClass().getResource("/limitsError5.xml").toURI());

        InitializationException caught = null;
        try {
            mocker.getComponentUnderTest();
        } catch (ComponentLookupException e) {
            if (e.getCause() instanceof InitializationException) {
                caught = (InitializationException) e.getCause();
            }
        }

        assertNotNull(caught);
        assertEquals("Failed to load the configuration of the Limits Application.", caught.getMessage());
        assertEquals("Missing attribute \"type\" for the limit [whatever].",
                caught.getCause().getMessage());
    }

}
