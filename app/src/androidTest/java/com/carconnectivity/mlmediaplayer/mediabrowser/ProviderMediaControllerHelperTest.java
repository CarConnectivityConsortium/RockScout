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
 * Car Connectivity Consortium products ("Feedback").
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee's copyrights to copy,
 * reproduce, modify, create derivative works and directly or indirectly
 * distribute, make available and communicate to public the Feedback
 * in or in connection to any CCC products, software and/or services.
 */

package com.carconnectivity.mlmediaplayer.mediabrowser;

import android.media.session.PlaybackState;
import android.test.AndroidTestCase;

import com.carconnectivity.mlmediaplayer.mediabrowser.model.SlotReservation;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.MediaButtonData;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.MediaButtonData.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class ProviderMediaControllerHelperTest extends AndroidTestCase {

    public void testCreation() {
        ProviderMediaControllerHelper test =
                new ProviderMediaControllerHelper(getContext(), getContext().getPackageName());
        assertNotNull(test);
    }

    public void testResolvePlaybackButtonNoState() {
        ProviderMediaControllerHelper test =
                new ProviderMediaControllerHelper(getContext(), getContext().getPackageName());
        assertNotNull(test);
        // Test default state (no params)
        PlaybackState.Builder builder = new PlaybackState.Builder();
        MediaButtonData result = test.resolvePlaybackButton(builder.build());
        assertEquals(result.getType(), Type.PLAY);
        // Test all actions added
        Long actions = PlaybackState.ACTION_PLAY;
        actions |= PlaybackState.ACTION_PAUSE;
        actions |= PlaybackState.ACTION_STOP;
        builder.setActions(actions);
        result = test.resolvePlaybackButton(builder.build());
        assertEquals(result.getType(), Type.PLAY);
    }

    public void testResolvePlaybackButtonStatePlayingNoActions() {
        ProviderMediaControllerHelper test =
                new ProviderMediaControllerHelper(getContext(), getContext().getPackageName());
        assertNotNull(test);
        PlaybackState.Builder builder = new PlaybackState.Builder();
        builder.setState(PlaybackState.STATE_PLAYING, 1000, 1.0f);
        MediaButtonData result = test.resolvePlaybackButton(builder.build());
        assertEquals(result.getType(), Type.PAUSE);
    }

    public void testResolvePlaybackButtonStatePlayingWithActions() {
        ProviderMediaControllerHelper test =
                new ProviderMediaControllerHelper(getContext(), getContext().getPackageName());
        assertNotNull(test);
        PlaybackState.Builder builder = new PlaybackState.Builder();
        builder.setState(PlaybackState.STATE_PLAYING, 1000, 1.0f);

        Long actions = PlaybackState.ACTION_PLAY;
        builder.setActions(actions);
        MediaButtonData result = test.resolvePlaybackButton(builder.build());
        assertEquals(result.getType(), Type.PAUSE);

        actions = PlaybackState.ACTION_PAUSE;
        builder.setActions(actions);
        result = test.resolvePlaybackButton(builder.build());
        assertEquals(result.getType(), Type.PAUSE);

        actions = PlaybackState.ACTION_STOP;
        builder.setActions(actions);
        result = test.resolvePlaybackButton(builder.build());
        assertEquals(result.getType(), Type.STOP);

        actions = PlaybackState.ACTION_PAUSE;
        actions |= PlaybackState.ACTION_STOP;
        builder.setActions(actions);
        result = test.resolvePlaybackButton(builder.build());
        assertEquals(result.getType(), Type.STOP);

        actions = PlaybackState.ACTION_PLAY;
        actions |= PlaybackState.ACTION_STOP;
        builder.setActions(actions);
        result = test.resolvePlaybackButton(builder.build());
        assertEquals(result.getType(), Type.STOP);

        actions = PlaybackState.ACTION_PAUSE;
        actions |= PlaybackState.ACTION_PLAY;
        builder.setActions(actions);
        result = test.resolvePlaybackButton(builder.build());
        assertEquals(result.getType(), Type.PAUSE);
    }

    private class TestSubCase {
        public String name;
        public EnumSet<SlotReservation> slotReservations;
        public long[] generalActions;
        public int customActionsCount;
        public List<Type> expectedResult;

        public TestSubCase(String name,
                           EnumSet<SlotReservation> slotReservations,
                           long[] generalActions,
                           int customActionsCount,
                           Type[] expectedResult) {
            this.name = name;
            this.slotReservations = slotReservations;
            this.generalActions = generalActions;
            this.customActionsCount = customActionsCount;
            this.expectedResult = Arrays.asList(expectedResult);
        }
    }

    private final TestSubCase[] TEST_SUB_CASES = new TestSubCase[] {
            // All slots reserved cases
            new TestSubCase("All slots, no actions",
                    EnumSet.allOf(SlotReservation.class),
                    new long [] {},
                    0,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.EMPTY}),

            new TestSubCase("All slots, all reserved actions",                                              // name of sub case - present in log when assert fails
                    EnumSet.allOf(SlotReservation.class),                                                   // media session extras reserved slots
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT}, // actions in playback state
                    0,                                                                                      // number of custom actions
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT}),                     // expected result as list of button types

            new TestSubCase("All slots, only skip to previous reserved action",
                    EnumSet.allOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    0,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.EMPTY}),

            new TestSubCase("All slots, only skip to next reserved action",
                    EnumSet.allOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.SKIP_TO_NEXT}),

            new TestSubCase("All slots, all reserved actions, 2 custom actions",
                    EnumSet.allOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    2,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT, Type.CUSTOM, Type.CUSTOM}),

            new TestSubCase("All slots, only skip to previous reserved action, 2 custom actions",
                    EnumSet.allOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    2,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.EMPTY, Type.CUSTOM, Type.CUSTOM}),

            new TestSubCase("All slots, only skip to next reserved action, 2 custom actions",
                    EnumSet.allOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    2,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.SKIP_TO_NEXT, Type.CUSTOM, Type.CUSTOM}),

            // No slots reserved cases
            new TestSubCase("No slots, no actions",
                    EnumSet.noneOf(SlotReservation.class),
                    new long [] {},
                    0,
                    new Type[] {Type.EMPTY, Type.EMPTY, Type.EMPTY}),


            new TestSubCase("No slots, 2 general actions",
                    EnumSet.noneOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.EMPTY, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT}),

            new TestSubCase("No slots, 2 custom actions",
                    EnumSet.noneOf(SlotReservation.class),
                    new long [] {},
                    2,
                    new Type[] {Type.CUSTOM, Type.CUSTOM, Type.EMPTY}),

            new TestSubCase("No slots, 2 general actions, 2 custom actions",
                    EnumSet.noneOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    2,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT, Type.CUSTOM}),

            new TestSubCase("No slots, 1 general action, 1 custom action",
                    EnumSet.noneOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    1,
                    new Type[] {Type.CUSTOM, Type.EMPTY, Type.SKIP_TO_NEXT}),

            // QUEUE slot reserved cases
            new TestSubCase("QUEUE slot, no actions",
                    EnumSet.of(SlotReservation.QUEUE),
                    new long [] {},
                    0,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.EMPTY}),

            new TestSubCase("QUEUE slot, 2 general actions",
                    EnumSet.of(SlotReservation.QUEUE),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT}),

            new TestSubCase("QUEUE slot, 2 custom actions",
                    EnumSet.of(SlotReservation.QUEUE),
                    new long [] {},
                    2,
                    new Type[] {Type.QUEUE, Type.CUSTOM, Type.CUSTOM}),

            new TestSubCase("QUEUE slot, 2 general actions, 2 custom actions",
                    EnumSet.of(SlotReservation.QUEUE),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    2,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT, Type.CUSTOM, Type.CUSTOM}),

            // SKIP_TO_PREVIOUS slot reserved cases
            new TestSubCase("SKIP_TO_PREVIOUS slot, 1 reserved action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    0,
                    new Type[] {Type.EMPTY, Type.SKIP_TO_PREVIOUS, Type.EMPTY}),

            new TestSubCase("SKIP_TO_PREVIOUS slot, 1 reserved action, 1 general action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.EMPTY, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_PREVIOUS slot, 1 reserved action, 1 custom action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    1,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.EMPTY}),

            new TestSubCase("SKIP_TO_PREVIOUS slot, 1 reserved action, 2 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    2,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.CUSTOM}),

            new TestSubCase("SKIP_TO_PREVIOUS slot, no reserved actions, 1 general action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.EMPTY, Type.EMPTY, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_PREVIOUS slot, no reserved actions, 2 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV),
                    new long [] {},
                    2,
                    new Type[] {Type.CUSTOM, Type.EMPTY, Type.CUSTOM}),

            new TestSubCase("SKIP_TO_PREVIOUS slot, no reserved actions, 1 general action, 1 custom action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    1,
                    new Type[] {Type.CUSTOM, Type.EMPTY, Type.SKIP_TO_NEXT}),

            // SKIP_TO_NEXT slot reserved cases
            new TestSubCase("SKIP_TO_NEXT slot, 1 reserved action",
                    EnumSet.of(SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.EMPTY, Type.EMPTY, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_NEXT slot, 1 reserved action, 1 general action",
                    EnumSet.of(SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.EMPTY, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_NEXT slot, 1 reserved action, 1 custom action",
                    EnumSet.of(SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    1,
                    new Type[] {Type.CUSTOM, Type.EMPTY, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_NEXT slot, 1 reserved action, 2 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    2,
                    new Type[] {Type.CUSTOM, Type.CUSTOM, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_NEXT slot, no reserved actions, 1 general action",
                    EnumSet.of(SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    0,
                    new Type[] {Type.EMPTY, Type.SKIP_TO_PREVIOUS, Type.EMPTY}),

            new TestSubCase("SKIP_TO_NEXT slot, no reserved actions, 2 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_NEXT),
                    new long [] {},
                    2,
                    new Type[] {Type.CUSTOM, Type.CUSTOM, Type.EMPTY}),

            new TestSubCase("SKIP_TO_NEXT slot, no reserved actions, 1 general action, 1 custom action",
                    EnumSet.of(SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    1,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.EMPTY}),

            new TestSubCase("SKIP_TO_NEXT slot, no reserved actions, 1 general action, 2 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    2,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.EMPTY, Type.CUSTOM}),

            new TestSubCase("SKIP_TO_NEXT slot, no reserved actions, 3 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_NEXT),
                    new long [] {},
                    3,
                    new Type[] {Type.CUSTOM, Type.CUSTOM, Type.EMPTY, Type.CUSTOM}),

            // QUEUE and SKIP_TO_PREVIOUS slot reserved cases
            new TestSubCase("QUEUE and SKIP_TO_PREVIOUS slot, 1 reserved action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    0,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.EMPTY}),

            new TestSubCase("QUEUE and SKIP_TO_PREVIOUS slot, 1 reserved action, 1 general action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT}),

            new TestSubCase("QUEUE and SKIP_TO_PREVIOUS slot, 1 reserved action, 1 custom action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    1,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.CUSTOM}),

            new TestSubCase("QUEUE and SKIP_TO_PREVIOUS slot, 1 reserved action, 1 general action, 1 custom action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    1,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT, Type.CUSTOM}),

            new TestSubCase("QUEUE and SKIP_TO_PREVIOUS slot, no reserved action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_PREV),
                    new long [] {},
                    0,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.EMPTY}),

            new TestSubCase("QUEUE and SKIP_TO_PREVIOUS slot, no reserved action, 1 general action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.SKIP_TO_NEXT}),

            new TestSubCase("QUEUE and SKIP_TO_PREVIOUS slot, no reserved action, 1 custom action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_PREV),
                    new long [] {},
                    1,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.CUSTOM}),

            new TestSubCase("QUEUE and SKIP_TO_PREVIOUS slot, no reserved action, 1 general action, 1 custom action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_PREV),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    1,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.SKIP_TO_NEXT, Type.CUSTOM}),

            // QUEUE and SKIP_TO_NEXT slot reserved cases
            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, 1 reserved action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.SKIP_TO_NEXT}),

            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, 1 reserved action, 1 general action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT}),

            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, 1 reserved action, 1 custom action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    1,
                    new Type[] {Type.QUEUE, Type.CUSTOM, Type.SKIP_TO_NEXT}),

            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, 1 reserved action, 2 custom actions",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    2,
                    new Type[] {Type.QUEUE, Type.CUSTOM, Type.SKIP_TO_NEXT, Type.CUSTOM}),

            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, 1 reserved action, 1 custom action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    1,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT, Type.CUSTOM}),

            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, no reserved action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {},
                    0,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.EMPTY}),

            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, no reserved action, 1 general action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    0,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.EMPTY}),

            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, no reserved action, 1 custom action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {},
                    1,
                    new Type[] {Type.QUEUE, Type.CUSTOM, Type.EMPTY}),

            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, no reserved action, 1 general action, 1 custom action",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    1,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.EMPTY, Type.CUSTOM}),

            new TestSubCase("QUEUE and SKIP_TO_NEXT slot, no reserved action, 2 custom actions",
                    EnumSet.of(SlotReservation.QUEUE, SlotReservation.SKIP_TO_NEXT),
                    new long [] {},
                    2,
                    new Type[] {Type.QUEUE, Type.CUSTOM, Type.EMPTY, Type.CUSTOM}),

            // SKIP_TO_PREV and SKIP_TO_NEXT slot reserved cases
            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, no reserved actions",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {},
                    0,
                    new Type[] {Type.EMPTY, Type.EMPTY, Type.EMPTY}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, no reserved actions, 1 custom action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {},
                    1,
                    new Type[] {Type.CUSTOM, Type.EMPTY, Type.EMPTY}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, no reserved actions, 2 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {},
                    2,
                    new Type[] {Type.CUSTOM, Type.EMPTY, Type.EMPTY, Type.CUSTOM}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, all reserved actions",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.EMPTY, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, all reserved actions, 1 custom action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    1,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, all reserved actions, 2 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    2,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT, Type.CUSTOM}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, SKIP_TO_PREV reserved action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    0,
                    new Type[] {Type.EMPTY, Type.SKIP_TO_PREVIOUS, Type.EMPTY}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, SKIP_TO_PREV reserved action, 1 custom action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    1,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.EMPTY}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, SKIP_TO_PREV reserved action, 2 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS},
                    2,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.EMPTY, Type.CUSTOM}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, SKIP_TO_NEXT reserved action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    0,
                    new Type[] {Type.EMPTY, Type.EMPTY, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, SKIP_TO_NEXT reserved action, 1 custom action",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    1,
                    new Type[] {Type.CUSTOM, Type.EMPTY, Type.SKIP_TO_NEXT}),

            new TestSubCase("SKIP_TO_PREV and SKIP_TO_NEXT slot, SKIP_TO_NEXT reserved action, 2 custom actions",
                    EnumSet.of(SlotReservation.SKIP_TO_PREV, SlotReservation.SKIP_TO_NEXT),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    2,
                    new Type[] {Type.CUSTOM, Type.EMPTY, Type.SKIP_TO_NEXT, Type.CUSTOM}),

            // Max actions cases
            new TestSubCase("no slots, no reserved actions, 100 custom actions",
                    EnumSet.noneOf(SlotReservation.class),
                    new long [] {},
                    100,
                    new Type[] {Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM}),

            new TestSubCase("no slots, 1 reserved action, 100 custom actions",
                    EnumSet.noneOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    100,
                    new Type[] {Type.CUSTOM, Type.CUSTOM, Type.SKIP_TO_NEXT, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM}),

            new TestSubCase("no slots, 2 reserved action, 100 custom actions",
                    EnumSet.noneOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    100,
                    new Type[] {Type.CUSTOM, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM}),

            new TestSubCase("all slots, no reserved actions, 100 custom actions",
                    EnumSet.allOf(SlotReservation.class),
                    new long [] {},
                    100,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.EMPTY, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM}),

            new TestSubCase("all slots, 1 reserved action, 100 custom actions",
                    EnumSet.allOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_NEXT},
                    100,
                    new Type[] {Type.QUEUE, Type.EMPTY, Type.SKIP_TO_NEXT, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM}),

            new TestSubCase("all slots, 2 reserved action, 100 custom actions",
                    EnumSet.allOf(SlotReservation.class),
                    new long [] {PlaybackState.ACTION_SKIP_TO_PREVIOUS, PlaybackState.ACTION_SKIP_TO_NEXT},
                    100,
                    new Type[] {Type.QUEUE, Type.SKIP_TO_PREVIOUS, Type.SKIP_TO_NEXT, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM, Type.CUSTOM}),

    };

    public void testResolveMediaButtons() {
        ProviderMediaControllerHelper test =
                new ProviderMediaControllerHelper(getContext(), getContext().getPackageName());
        assertNotNull(test);
        // Input: reservations, general actions, custom actions
        // Output: media button list
        for (TestSubCase subCase : TEST_SUB_CASES) {
            long actions = 0;
            for (long action : subCase.generalActions) {
                actions |= action;
            }
            PlaybackState.Builder builder = new PlaybackState.Builder();
            builder.setActions(actions);
            for (int i=0; i<subCase.customActionsCount; i++) {
                builder.addCustomAction("action" + i, "name" + 1, 0);
            }
            List<Type> result = new ArrayList<>();
            List<MediaButtonData> mediaButtonList = test.resolveMediaButtons(subCase.slotReservations, builder.build());
            for (MediaButtonData button : mediaButtonList) {
                result.add(button.getType());
            }
            assertEquals(subCase.name, subCase.expectedResult.size(), result.size());
            for (int i=0; i<subCase.expectedResult.size(); i++) {
                assertEquals(subCase.name + " on position " + i, subCase.expectedResult.get(i), result.get(i));
            }
        }
    }
}
