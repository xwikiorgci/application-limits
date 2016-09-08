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
package org.xwiki.contrib.limits.internal.wikis;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.limits.XWikiLimitsConfiguration;
import org.xwiki.platform.wiki.creationjob.WikiCreationException;
import org.xwiki.platform.wiki.creationjob.WikiCreationRequest;
import org.xwiki.platform.wiki.creationjob.WikiCreationStep;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.manager.WikiManagerException;

/**
 * Add a step to the wiki creation job that verifies if the number of wikis is reached.
 * 
 * @version $Id: $
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
@Named("wikisLimit")
public class WikisLimitStep implements WikiCreationStep
{
    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private XWikiLimitsConfiguration configuration;

    @Override
    public void execute(WikiCreationRequest request) throws WikiCreationException
    {
        try {
            int limit = configuration.getWikisNumberLimit();
            if (wikiDescriptorManager.getAllIds().size() >= limit) {
                throw new WikiCreationException(
                        String.format("The number of wikis has reached the limit [%d].", limit));
            }
        } catch (WikiManagerException e) {
            throw new WikiCreationException("Failed to get the number of wikis.", e);
        }
    }

    @Override
    public int getOrder()
    {
        // First step to be executed
        return 500;
    }
}
