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

package com.carconnectivity.mlmediaplayer.utils.breadcrumbs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Stack;

/**
 * Created by belickim on 08/06/15.
 */
public final class BreadCrumbs {
    private final Stack<NavigatorLevel> mStack;

    public BreadCrumbs(String topLevelName) {
        mStack = new Stack<>();
        final NavigatorLevel root = NavigatorLevel.createRootLevel(topLevelName);
        mStack.push(root);
    }

    private BreadCrumbs(Stack<NavigatorLevel> stack) {
        mStack = stack;
    }

    /** Brings bread crumbs to the state after creation. */
    public void reset() {
        /* pop until you find a root element: */
        while (mStack.peek().isRoot == false)
            mStack.pop();
    }

    /** Create new instance form JSON string. */
    public static BreadCrumbs fromJson(String json) {
        if (json == null) {
            throw new IllegalArgumentException("Json string cannot be null.");
        }
        if (json.isEmpty()) {
            throw new IllegalArgumentException("Json string cannot be empty.");
        }

        final Gson gson = new Gson();
        final Type collectionType = new TypeToken<Stack<NavigatorLevel>>() {
        }.getType();
        final Stack<NavigatorLevel> stack = gson.fromJson(json, collectionType);
        return new BreadCrumbs(stack);
    }

    /** Serialize to JSON string. */
    public String toJson() {
        final Gson gson = new Gson();
        return gson.toJson(mStack);
    }

    /** Push new level to the bread crumbs stack. */
    public void push(String displayName, String id) {
        if (displayName == null) {
            throw new IllegalArgumentException("Level cannot be null.");
        }
        if (id == null) {
            throw new IllegalArgumentException("Level cannot be null.");
        }
        if (mStack.isEmpty()) {
            throw new IllegalStateException("Stack cannot be empty.");
        }

        final String parentId = getTopItem().parent;
        final NavigatorLevel level = NavigatorLevel.createLevel(parentId, displayName, id);
        mStack.push(level);
    }

    /** Returns true if popping current level from bread crumbs stack can be performed. */
    public boolean canGoBack() {
        return getTopItem().isRoot == false;
    }

    /** Pops the current level from bread crumbs stack, the popped item is returned. */
    public NavigatorLevel goBack() {
        if (canGoBack() == false) {
            throw new IllegalStateException("Cannot go back, the current item is root.");
        }
        return  mStack.pop();
    }

    /** Returns current top item without changing the state. */
    public NavigatorLevel getTopItem() {
        if (mStack.size() == 0) {
            throw new IllegalStateException("The stack is empty.");
        }
        return mStack.peek();
    }
}
