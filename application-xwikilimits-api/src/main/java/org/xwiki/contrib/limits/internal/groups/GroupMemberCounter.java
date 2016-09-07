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
package org.xwiki.contrib.limits.internal.groups;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

/**
 * @version $Id: $
 */
@Component(roles = GroupMemberCounter.class)
@Singleton
public class GroupMemberCounter
{
    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private QueryManager queryManager;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    public long getUserCount(DocumentReference groupReference) throws Exception
    {
        try {
            Query query = queryManager.createQuery("SELECT COUNT(DISTINCT obj.member) FROM Document doc, " +
                    "doc.object(XWiki.XWikiGroups) AS obj WHERE doc.fullName = :groupDoc " +
                    "AND obj.member <> ''", Query.XWQL)
                        .setWiki(wikiDescriptorManager.getMainWikiId())
                        .bindValue("groupDoc", entityReferenceSerializer.serialize(groupReference));
            List<Long> results = query.execute();
            return results.get(0);
        } catch (QueryException e) {
            throw new Exception(String.format("Failed to get the number of member in the group [%s].",
                    groupReference.toString()), e);
        }
    }
}
