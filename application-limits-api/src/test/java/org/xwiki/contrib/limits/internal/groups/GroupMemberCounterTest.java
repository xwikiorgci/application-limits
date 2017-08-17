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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.limits.LimitsConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.observation.event.CancelableEvent;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @version $Id: $
 */
public class GroupMemberCounterTest
{
    @Rule
    public MockitoComponentMockingRule<GroupMemberListener> mocker =
            new MockitoComponentMockingRule<>(GroupMemberListener.class);

    private GroupMemberCounter groupMemberCounter;
    private LimitsConfiguration limitsConfiguration;
    private DocumentReferenceResolver<String> explicitDocumentReferenceResolver;
    private Provider<Execution> executionProvider;
    private Execution execution;
    private XWikiContext xcontext;
    private XWiki xwiki;

    /**
     * Group to test
     */
    private DocumentReference groupReference = new DocumentReference("xwiki", "XWiki", "SomeGroup");

    /**
     * User
     */
    private DocumentReference userA = new DocumentReference("xwiki", "XWiki", "UserA");

    /**
     * An other user
     */
    private DocumentReference userB = new DocumentReference("xwiki", "XWiki", "UserB");

    /**
     * Limits
     */
    private Map<DocumentReference, Number> groupLimits;

    /**
     * The event sent to the component.
     */
    private CancelableEvent event;

    @Before
    public void setUp() throws Exception
    {
        groupMemberCounter = mocker.getInstance(GroupMemberCounter.class);
        limitsConfiguration = mocker.getInstance(LimitsConfiguration.class);
        explicitDocumentReferenceResolver = mock(DocumentReferenceResolver.class);
        mocker.registerComponent(DocumentReferenceResolver.TYPE_STRING, "explicit", explicitDocumentReferenceResolver);
        executionProvider =
                mocker.registerMockComponent(new DefaultParameterizedType(null, Provider.class, Execution.class));
        execution = mock(Execution.class);
        when(executionProvider.get()).thenReturn(execution);

        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(execution.getContext()).thenReturn(executionContext);

        xcontext = mock(XWikiContext.class);
        when(executionContext.getProperty(XWikiContext.EXECUTIONCONTEXT_KEY)).thenReturn(xcontext);
        xwiki = mock(XWiki.class);
        when(xcontext.getWiki()).thenReturn(xwiki);

        groupLimits = new HashMap<>();
        when(limitsConfiguration.getGroupsLimits()).thenReturn(groupLimits);

        // Event
        event = mock(CancelableEvent.class);
    }

    /**
     * Create some mocks to fake a group document holding some member objects, that can work with ReferenceUserIterator.
     *
     * @param groupReference the reference of the group document to mock
     * @param users a list of users that the group document is holding (but not saved yet)
     * @param oldCount the old count of users (before the save action that is occuring)
     *
     * @return the mock document to be used by the component
     *
     * @throws Exception if something bad happens
     */
    private XWikiDocument mockGroup(DocumentReference groupReference, List<DocumentReference> users, long oldCount)
            throws Exception
    {
        // The list of objects that the group document is supposed to hold
        List<BaseObject> userObjs = new ArrayList<>(users.size());
        for (DocumentReference user : users) {
            BaseObject userObj = null;
            if (user != null) {
                userObj = mock(BaseObject.class);
                when(userObj.getStringValue("member")).thenReturn(user.toString());
                // Also mock the user
                mockUser(user);
            }
            userObjs.add(userObj);
        }

        // The old count
        when(groupMemberCounter.getUserCount(groupReference)).thenReturn(Long.valueOf(oldCount));

        // The document to return
        XWikiDocument doc = mock(XWikiDocument.class);
        when(doc.getDocumentReference()).thenReturn(groupReference);
        when(doc.getXObjects(new DocumentReference("xwiki", "XWiki", "XWikiGroups"))).thenReturn(
                userObjs);

        return doc;
    }

    /**
     * Mock a user document holding a XWiki.XWikiUsers object.
     * @param user the reference of the user
     * @throws XWikiException if an error occurs
     */
    private void mockUser(DocumentReference user) throws XWikiException
    {
        when(explicitDocumentReferenceResolver.resolve(user.toString())).thenReturn(user);

        XWikiDocument userDoc = mock(XWikiDocument.class);
        when(xwiki.getDocument(eq(user), eq(xcontext))).thenReturn(userDoc);

        LocalDocumentReference userClassReference = new LocalDocumentReference("XWiki", "XWikiUsers");
        BaseObject userDocObj = mock(BaseObject.class);
        when(userDoc.getXObject(eq(userClassReference))).thenReturn(userDocObj);
        when(userDoc.getDocumentReference()).thenReturn(user);
    }

    @Test
    public void onEvent_WhenLimitIsReached() throws Exception
    {
        // Limits
        groupLimits.put(groupReference, 1);

        // Content of the group
        XWikiDocument groupDoc = mockGroup(groupReference, Arrays.asList(userA, null, userB), 1);

        // Run the test
        mocker.getComponentUnderTest().onEvent(event, groupDoc, null);

        // Verify
        verify(event).cancel(
                "The limit of number of users in the group [xwiki:XWiki.SomeGroup] has been reached [2/1].");
    }

    @Test
    public void onEvent_WhenLimitIsNotReached() throws Exception
    {
        // Limits
        groupLimits.put(groupReference, 3);

        // Content of the group
        XWikiDocument groupDoc = mockGroup(groupReference, Arrays.asList(userA, userB), 1);

        // Run the test
        mocker.getComponentUnderTest().onEvent(event, groupDoc, null);

        // Verify
        verify(event, never()).cancel(anyString());
    }

    /**
     * In this test, the limit is reached, but the number of members inside the group decreases, which is already a
     * progress. This feature is needed to allow the administrator to use the UI to remove some members one by one.
     */
    @Test
    public void onEvent_WhenLimitIsReachedButNumberOfMemberDecreases() throws Exception
    {
        // Limits
        groupLimits.put(groupReference, 1);

        // Content of the group
        XWikiDocument groupDoc = mockGroup(groupReference, Arrays.asList(userA, userB), 3);

        // Run the test
        mocker.getComponentUnderTest().onEvent(event, groupDoc, null);

        // Verify
        verify(event, never()).cancel(anyString());
    }

}
