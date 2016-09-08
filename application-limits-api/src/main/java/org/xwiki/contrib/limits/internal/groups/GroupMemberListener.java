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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.contrib.limits.XWikiLimitsConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.CancelableEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.text.StringUtils;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.plugin.rightsmanager.ReferenceUserIterator;
import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id: $
 */
@Component
@Named("XWikiLimitsApplication_GroupMemberListener")
@Singleton
public class GroupMemberListener implements EventListener
{
    private static final DocumentReference GROUP_CLASS =
            new DocumentReference("xwiki", "XWiki", "XWikiGroups");

    private static final List<Event> EVENTS = Arrays.<Event>asList(new DocumentUpdatingEvent());

    @Inject
    private GroupMemberCounter groupMemberCounter;

    @Inject
    private XWikiLimitsConfiguration limitsConfiguration;

    @Inject
    @Named("explicit")
    private DocumentReferenceResolver<String> explicitDocumentReferenceResolver;

    @Inject
    private Logger logger;

    @Inject
    private Provider<Execution> executionProvider;

    @Override
    public String getName()
    {
        return "XWiki Limits Application - Groups Member Listener";
    }

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument document = (XWikiDocument) source;

        try {
            DocumentReference documentReference = document.getDocumentReference();
            Number limit = limitsConfiguration.getGroupsLimits().get(documentReference);
            if (limit != null) {
                // New user count is computed by parsing the received document
                long count = getUserCount(document);
                // Meanwhile current count is computed by watching the database
                long oldCount = groupMemberCounter.getUserCount(documentReference);
                maybeCancelUpdate(event, documentReference, oldCount, count, limit.intValue());
            }
        } catch (Exception e) {
            logger.error("Failed to check if the xwiki network group limits are respected.", e);
        }
    }

    private void maybeCancelUpdate(Event event, DocumentReference documentReference, long oldCount, long count,
            long limit)
    {
        // It's ok to save the document that have more users than allowed if it decreases the number of members
        // (use-case: the group already exist but the limit is decreased afterwards. The user must be able to save the
        // group to remove some users, even one by one - that's how works the UI)
        if (count > limit && count > oldCount) {
            logger.warn("Forbid the addition of a user [{}] in the group [{}] because the group limit" +
                    " has been reached [{}/{}].", documentReference, count, limit);
            if (event instanceof CancelableEvent) {
                CancelableEvent cancelableEvent = (CancelableEvent) event;
                cancelableEvent.cancel(String.format(
                        "The limit of number of users in the group [%s] has been reached [%d/%d].",
                        documentReference, count, limit));
            } else {
                logger.error("Failed to cancel the event [{}].", event);
            }
        }
    }

    private long getUserCount(XWikiDocument document)
    {
        // Hashset is used to avoid counting twice the same member if multiple objects have the same value.
        HashSet<DocumentReference> members = new HashSet<>();
        for (BaseObject obj : document.getXObjects(GROUP_CLASS)) {
            if (obj == null) {
                continue;
            }
            String member = obj.getStringValue("member");
            if (StringUtils.isNotBlank(member)) {
                // The member could be... an other group!
                // It could happen if the user is trying to cheat us.
                DocumentReference groupReference = explicitDocumentReferenceResolver.resolve(member);
                ReferenceUserIterator referenceUserIterator = new ReferenceUserIterator(groupReference,
                        explicitDocumentReferenceResolver, executionProvider.get());
                while (referenceUserIterator.hasNext()) {
                    members.add(referenceUserIterator.next());
                }
            }
        }
        return members.size();
    }
}
