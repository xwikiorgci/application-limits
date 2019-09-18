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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.xpn.xwiki.objects.BaseObject;
import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatingEvent;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.limits.LimitsConfiguration;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.CancelableEvent;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Cancel the saving of a user is the number of users is superior to the limit fixed for the XWiki instance.
 *
 * @version $Id: $
 */
@Component
@Named("LimitsApplication_UserLimit")
@Singleton
public class UserListener implements EventListener
{
    private static final LocalDocumentReference USER_CLASS = new LocalDocumentReference("XWiki", "XWikiUsers");

    private static final String ACTIVE = "active";

    @Inject
    private UserCounter userCounter;

    @Inject
    private LimitsConfiguration limitConfiguration;

    @Inject
    private Logger logger;

    @Override
    public String getName()
    {
        return "XWiki Limits Application - User Limit";
    }

    @Override
    public List<Event> getEvents()
    {
        return Arrays.asList(new DocumentCreatingEvent(), new DocumentUpdatingEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument document = (XWikiDocument) source;

        // If there is no user object on the page, do nothing
        if (document.getXObject(USER_CLASS) == null) {
            return;
        }

        // If the event is not about activating a user (either by creating a new user or by changing the
        // "active" field of an existing user), we don't care (it is allowed to save some changes on existing users).
        if (!isActivatingUser(document)) {
            return;
        }

        try {
            long userCount = userCounter.getUserCount();
            long userLimit = limitConfiguration.getTotalNumberOfUsersLimit();
            if (userCount >= userLimit) {
                logger.warn("Forbid the creation of a new user [{}] because the user limit has been reached [{}/{}].",
                        document.getDocumentReference(), userCount, userLimit);
                if (event instanceof CancelableEvent) {
                    CancelableEvent cancelableEvent = (CancelableEvent) event;
                    cancelableEvent.cancel("The user limit has been reached.");
                } else {
                    // Should never happen
                    logger.error("Failed to cancel the event [{}].", event);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to limit the number of users", e);
        }
    }

    private boolean isActivatingUser(XWikiDocument document)
    {
        return isUserActive(document) && !isOldDocumentActive(document);
    }

    private boolean isUserActive(XWikiDocument document)
    {
        BaseObject object = document.getXObject(USER_CLASS);
        return object.getIntValue(ACTIVE, 1) == 1;
    }

    private boolean isOldDocumentActive(XWikiDocument document)
    {
        XWikiDocument originalDoc = document.getOriginalDocument();
        if (originalDoc == null) {
            // If there is no original document, it means we are creating a new document, so the user did not exist
            // before so it was not "active".
            return false;
        }
        BaseObject originalObj = originalDoc.getXObject(USER_CLASS);
        // It seems that even if the document is new, there is an original document object. So we also need to be sure
        // there is an user object too.
        if (originalObj == null) {
            // No object = no user
            return false;
        }
        return originalObj.getIntValue(ACTIVE, 1) == 1;
    }
}
