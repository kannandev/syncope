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
package org.apache.syncope.client.console.wicket.markup.html.form.preview;

import static org.apache.syncope.client.console.wicket.markup.html.form.preview.AbstractBinaryPreviewer.LOG;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.core.MediaType;
import org.apache.syncope.client.console.annotations.BinaryPreview;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.io.IOUtils;

@BinaryPreview(mimeTypes = { "application/json", "application/xml" })
public class BinaryTextPreviewer extends AbstractBinaryPreviewer {

    private static final long serialVersionUID = 3808379310090668773L;

    public BinaryTextPreviewer(final String id, final String mimeType) {
        super(id, mimeType);
    }

    @Override
    public Component preview(final byte[] uploadedBytes) {

        Fragment fragment = new Fragment("preview", "noPreviewFragment", this);
        if (uploadedBytes.length > 0) {
            try {
                fragment = new Fragment("preview", "previewFragment", this);
                InputStream stream = new ByteArrayInputStream(uploadedBytes);
                TextArea<String> jsonEditor =
                        new TextArea<>("jsonEditorInfo", new Model<>(IOUtils.toString(stream)));
                jsonEditor.setMarkupId("jsonEditorInfo").setOutputMarkupPlaceholderTag(true);
                fragment.add(jsonEditor);
            } catch (IOException e) {
                LOG.error("Error evaluating text file", e);
            }
        }

        WebMarkupContainer previewContainer = new WebMarkupContainer("previewContainer");
        previewContainer.setOutputMarkupId(true);
        previewContainer.add(fragment);

        return this.addOrReplace(previewContainer);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        String options;
        switch (mimeType) {
            case MediaType.APPLICATION_JSON:
                options = "matchBrackets: true, autoCloseBrackets: true,";
                break;
            case MediaType.APPLICATION_XML:
                options = "autoCloseTags: true, mode: 'text/html',";
                break;
            default:
                options = "mode: 'text/html',";
        }

        response.render(OnLoadHeaderItem.forScript(
                "var editor = CodeMirror.fromTextArea(document.getElementById('jsonEditorInfo'), {"
                + "  readOnly: true, "
                + "  lineNumbers: true, "
                + "  lineWrapping: true, "
                + options
                + "  autoRefresh: true"
                + "});"
                + "editor.setSize('100%', 100)"));
    }
}