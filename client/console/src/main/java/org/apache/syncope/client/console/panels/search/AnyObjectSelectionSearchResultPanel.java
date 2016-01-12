/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.panels.search;

import java.util.List;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.rest.AbstractAnyRestClient;
import org.apache.syncope.client.console.wizards.WizardMgtPanel;
import org.apache.syncope.client.console.wizards.any.AnyHandler;
import org.apache.syncope.common.lib.to.AnyObjectTO;
import org.apache.syncope.common.lib.to.AnyTypeClassTO;
import org.apache.wicket.PageReference;

public final class AnyObjectSelectionSearchResultPanel extends AnySelectionSearchResultPanel<AnyObjectTO> {

    private static final long serialVersionUID = -1100228004207271272L;

    public static final String[] USER_DEFAULT_SELECTION = { "key" };

    private AnyObjectSelectionSearchResultPanel(final String id,
            final AnyObjectSelectionSearchResultPanel.Builder builder) {
        super(id, builder);
    }

    @Override
    protected String paginatorRowsKey() {
        return Constants.PREF_ANYOBJECT_PAGINATOR_ROWS;
    }

    @Override
    protected String[] getDislayAttributes() {
        return USER_DEFAULT_SELECTION;
    }

    @Override
    public String getPrefDetailsView() {
        return String.format(Constants.PREF_ANY_DETAILS_VIEW, type);
    }

    @Override
    public String getPrefAttributesView() {
        return String.format(Constants.PREF_ANY_ATTRIBUTES_VIEW, type);
    }

    @Override
    public String getPrefDerivedAttributesView() {
        return String.format(Constants.PREF_ANY_DERIVED_ATTRIBUTES_VIEW, type);
    }

    public static final class Builder extends AnySelectionSearchResultPanel.Builder<AnyObjectTO> {

        private static final long serialVersionUID = 1L;

        public Builder(
                final List<AnyTypeClassTO> anyTypeClassTOs,
                final AbstractAnyRestClient<AnyObjectTO> restClient,
                final String type,
                final PageReference pageRef) {

            super(anyTypeClassTOs, restClient, type, pageRef);
            this.filtered = true;
            this.checkBoxEnabled = false;
        }

        @Override
        protected WizardMgtPanel<AnyHandler<AnyObjectTO>> newInstance(final String id) {
            return new AnyObjectSelectionSearchResultPanel(id, this);
        }
    }
}
