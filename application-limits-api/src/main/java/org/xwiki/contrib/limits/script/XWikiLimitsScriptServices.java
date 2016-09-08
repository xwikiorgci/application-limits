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
package org.xwiki.contrib.limits.script;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.limits.XWikiLimitsConfiguration;
import org.xwiki.contrib.limits.internal.users.UserCounter;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

/**
 * @version $Id: $
 */
@Component
@Singleton
@Named("limits")
public class XWikiLimitsScriptServices implements ScriptService
{
    @Inject
    private XWikiLimitsConfiguration limitsConfiguration;

    @Inject
    private UserCounter userCounter;

    public long getUserLimit() throws Exception
    {
        return limitsConfiguration.getTotalNumberOfUsersLimit();
    }

    public long getUserCount() throws Exception
    {
        return userCounter.getUserCount();
    }

    public Map<DocumentReference, Number> getGroupLimits() throws Exception
    {
        return limitsConfiguration.getGroupsLimits();
    }

    public void reloadConfiguration() throws Exception
    {
        limitsConfiguration.reload();
    }

}
