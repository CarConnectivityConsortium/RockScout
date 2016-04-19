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

package com.carconnectivity.mlmediaplayer.mediabrowser.model;

/**
 * Created by belickim on 06/06/15.
 */
public class TrackMetadata {
    public final String title;
    public final String artist;
    public final Long duration;
    public final String artUri;

    public TrackMetadata
            (String title, String artist, Long duration, String artUri) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.artUri = artUri;
    }

    public static TrackMetadata createEmpty() {
        return new TrackMetadata(null, null, 0L, null);
    }

    public boolean sameAsOther(TrackMetadata other) {
        if (other == null) return false;

        final boolean titleMatch = title != null && title.equals(other.title);
        final boolean artistMatch = artist != null && artist.equals(other.artist);
        final boolean durationMatch = duration != null && duration.equals(other.duration);
        final boolean artUriMatch = artUri != null && artUri.equals(other.artUri);

        return titleMatch && artistMatch && durationMatch && artUriMatch;
    }

    public boolean isTitleEmpty() {
        return title == null || title.isEmpty();
    }

    @Override
    public String toString() {
        return "MediaMetadata{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration=" + duration +
                ", artUri=" + artUri +
                '}';
    }
}
