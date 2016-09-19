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
package org.xwiki.contrib.limits;

import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.stability.Unstable;

/**
 * Handle the configuration of the XWiki Limits Application.
 *
 * @version $Id: $
 */
@Unstable
@Role
public interface LimitsConfiguration
{
    /**
     * Reload the configuration.
     * @throws Exception if something bad happens
     */
    void reload() throws Exception;

    /**
     * @return the maximum number of users that the XWiki instance is allowed to handle
     */
    int getTotalNumberOfUsersLimit();

    /**
     * @return the maximum number of wikis that the XWiki instance is allowed to handle
     */
    int getWikisNumberLimit();

    /**
     * @return an unmodifiable map of group references associated to the number of users that groups are allowed
     * to contain
     */
    Map<DocumentReference, Number> getGroupsLimits();

    /**
     * @return an unmodifiable map of custom limits (that could a number or a date) that the developer is responsible
     * to implement
     * @since 1.2
     */
    Map<String, Object> getCustomLimits();
}
