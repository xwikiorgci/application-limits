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
package org.xwiki.contrib.limits.internal.users;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.manager.WikiManagerException;

/**
 * @version $Id: $
 */
@Component(roles = UserCounter.class)
@Singleton
public class UserCounter
{
    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    public int getUserCount() throws Exception
    {
        try {
            int count = 0;
            for (String wikiId : wikiDescriptorManager.getAllIds()) {
                count += getUserCountOnWiki(wikiId);
            }
            logger.debug("User count [{}].", count);
            return count;
        } catch (WikiManagerException | QueryException e) {
            throw new Exception("Failed to get the user count.", e);
        }
    }

    private int getUserCountOnWiki(String wikiId) throws QueryException
    {
        Query query = queryManager.createQuery("SELECT COUNT(DISTINCT doc.fullName) FROM Document doc," +
                " doc.object(XWiki.XWikiUsers) AS obj",
                Query.XWQL).setWiki(wikiId);
        List<Long> results = query.execute();
        return results.get(0).intValue();
    }
}
