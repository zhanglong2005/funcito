/*
 * Copyright 2011-2013 Project Funcito Contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.funcito.guava;

import com.google.common.base.Predicate;

import org.funcito.*;
import org.funcito.functorbase.FunctorBase;
import org.funcito.functorfactory.FunctorFactory;
import org.funcito.internal.InvokableState;
import org.funcito.mode.Mode;
import org.funcito.mode.UntypedMode;
import org.funcito.mode.ValidateNullBoolean;

import java.lang.reflect.Method;

public class GuavaPredicate<T> implements Predicate<T> {

    private static Method altMethod;
    static {
        try {
            altMethod = FuncitoGuava.class.getMethod("predicateFor", Boolean.class, boolean.class);
        } catch (NoSuchMethodException e) { // ignored
        }
    }
    private FunctorBase<T,Boolean> functorBase;

    public GuavaPredicate(InvokableState state) {
        this(state, new ValidateNullBoolean(Predicate.class, altMethod));
    }

    public GuavaPredicate(InvokableState state, Mode<T,Boolean> mode) {
        functorBase = FunctorFactory.instance().makeFunctionalBase(state, mode);
    }

    public GuavaPredicate(InvokableState state, UntypedMode mode) {
        functorBase = FunctorFactory.instance().makeFunctionalBase(state, mode);
    }

    @Override
    public boolean apply(T from) {
        return functorBase.applyImpl(from);
    }

}
