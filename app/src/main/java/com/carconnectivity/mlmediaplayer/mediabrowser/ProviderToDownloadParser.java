/*
 * Copyright Car Connectivity Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You may decide to give the Car Connectivity Consortium input, suggestions
 * or feedback of a technical nature which may be implemented on the
 * Car Connectivity Consortium products (“Feedback”).
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee’s copyrights to copy,
 * reproduce, modify, create derivative works and directly or indirectly
 * distribute, make available and communicate to public the Feedback
 * in or in connection to any CCC products, software and/or services.
 */

package com.carconnectivity.mlmediaplayer.mediabrowser;

import android.content.ComponentName;
import android.util.Log;
import android.util.Xml;

import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderToDownloadDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

/**
 * Created by sebastian.sokolowski on 17.02.16.
 */
public class ProviderToDownloadParser {
    private static final String TAG = ProviderToDownloadParser.class.getSimpleName();

    private static final String URL_ADDRESS_XML = "http://www.mirrorlink.com/img/mirrorlink/upload/documents/mirror_link_compatible_apps.xml";

    private static final String TAG_APP = "app";
    private static final String TAG_LABEL = "label";
    private static final String TAG_ICON_URL = "iconUrl";
    private static final String TAG_ID = "id";

    private static final String ns = null;
    private final Set<ComponentName> mediaBrowserPackages;

    public ProviderToDownloadParser(Set<ComponentName> mediaBrowserPackages) {
        this.mediaBrowserPackages = mediaBrowserPackages;
    }

    public void download() {
        InputStream in;
        try {
            in = getInputStreamFromUrl(URL_ADDRESS_XML);
            parse(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parse(InputStream in) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readFeed(parser);
            in.close();
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Something went wrong: ", e);

        }
    }

    private InputStream getInputStreamFromUrl(String urlAdress) throws IOException {
        URL url = new URL(urlAdress);
        URLConnection urlConnection = url.openConnection();
        return new BufferedInputStream(urlConnection.getInputStream());
    }

    private void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(TAG_APP)) {
                readProvider(parser);
            } else {
                skip(parser);
            }
        }
    }

    private void readProvider(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_APP);
        String label = null;
        String iconUrl = null;
        String id = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_LABEL)) {
                label = read(parser, TAG_LABEL);
            } else if (name.equals(TAG_ICON_URL)) {
                iconUrl = read(parser, TAG_ICON_URL);
            } else if (name.equals(TAG_ID)) {
                id = read(parser, TAG_ID);
            } else {
                skip(parser);
            }
        }

        if (label == null || label.isEmpty()) return;
        if (iconUrl == null || iconUrl.isEmpty()) return;
        if (id == null || id.isEmpty()) return;

        URL url = new URL(iconUrl);

        boolean exist = false;
        if (mediaBrowserPackages != null) {
            for (ComponentName componentName : mediaBrowserPackages
                    ) {
                if (componentName.getPackageName().equals(id)) {
                    exist = true;
                }
            }
        }
        if (!exist) {
            ProviderViewToDownload providerViewToDownload = new ProviderViewToDownload(label, id, url);
            RsEventBus.post(new ProviderToDownloadDiscoveredEvent(providerViewToDownload));
        }
    }

    private String read(XmlPullParser parser, String toParse) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, toParse);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, toParse);
        return title;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
